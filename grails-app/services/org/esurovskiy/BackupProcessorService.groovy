package org.esurovskiy

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.logging.LogFactory
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * Created by hacker13ua on 06.02.14.
 */
class BackupProcessorService implements Job
{
    @SuppressWarnings("GroovyConstantNamingConvention")
    public final static String SERVER_URL= '192.168.2.71';
    @SuppressWarnings("GroovyConstantNamingConvention")
    private static final LOG = LogFactory.getLog(this)

    void makeFullHostBackup(final Serializable remoteHostId)
    {
        RemoteHost remoteHost = RemoteHost.get(remoteHostId);
        ChannelExec sshChannel;
        for (PostgresStorage storage in remoteHost.postgresStorages)
        {
            try
            {
                sshChannel = createRemoteChannel(remoteHost)
                final String backupFileName = "${remoteHost.ipAddress}-${storage.databaseName}".replaceAll(' ', '').replaceAll('\\.', '');
                final String backupCommand = "("+storage.getBackupCommand(backupFileName) + ") && { " + getSuccessFinishEventCommand(backupFileName, remoteHostId) + "; } || { " + getFailFinishEventCommand(backupFileName, remoteHostId) + "; }";
                sshChannel.command = backupCommand ;
                InputStream inputStream = sshChannel.inputStream;
                sshChannel.connect();
                println readResultFromSSHChannel(inputStream, sshChannel);
                closeChannelAndSession(sshChannel);
            }
            catch (Exception ex)
            {
                LOG.error("Something wrong")
            }
            finally
            {
                if (sshChannel)
                {
                    closeChannelAndSession(sshChannel);
                }
            }
        }

        for (HbaseStorage storage : remoteHost.hbaseStorages)
        {
            try
            {
                sshChannel = createRemoteChannel(remoteHost)
                final String backupFileName = "${remoteHost.ipAddress}-${storage.tableName}".replaceAll(' ', '').replaceAll('\\.', '');
                final String backupCommand = "("+storage.getBackupCommand(backupFileName) + ") && { " + getSuccessFinishEventCommand(backupFileName, remoteHostId) + "; } || { " + getFailFinishEventCommand(backupFileName, remoteHostId) + "; }";
                sshChannel.command = backupCommand ;
                InputStream inputStream = sshChannel.inputStream;
                sshChannel.connect();
                println readResultFromSSHChannel(inputStream, sshChannel);
                closeChannelAndSession(sshChannel);
                inputStream.close();
            }
            catch (Exception ex)
            {
                LOG.error("Something wrong")
            }
            finally
            {
                if (sshChannel)
                {
                    closeChannelAndSession(sshChannel);
                }
            }
        }
    }

    public def downloadBackupToServer(final Serializable remoteHostId, String fileName)
    {
        RemoteHost remoteHost = RemoteHost.get(remoteHostId);
        FileOutputStream fos=null;
        try
        {
            String prefix=null;
            String lfile = "/home/esurovskiy/grails_backup/${fileName}";
            if(new File(lfile).isDirectory())
            {
                prefix=lfile+File.separator;
            }
            JSch jsch=new JSch();
            Session session=jsch.getSession(remoteHost.login, remoteHost.ipAddress, remoteHost.sshPort);
            session.password = remoteHost.password;
            Properties config = new Properties();
            config["StrictHostKeyChecking"] = "no";
            session.config = config;
            session.connect();
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).command = "scp -f ./${fileName}";
//            channel.inputStream = null;
//            ((ChannelExec)channel).errStream = System.err;
            OutputStream outputStream=channel.getOutputStream();
            InputStream inputStream=channel.getInputStream();
            channel.connect();

            byte[] buf=new byte[1024];

            // send '\0'
            buf[0]=0; outputStream.write(buf, 0, 1); outputStream.flush();

            while(true){
                int c=checkAck(inputStream);
                if(c!='C'){
                    break;
                }

                // read '0644 '
                inputStream.read(buf, 0, 5);

                long filesize=0L;
                while(true){
                    if(inputStream.read(buf, 0, 1)<0){
                        // error
                        break;
                    }
                    if(buf[0]==' ')break;
                    filesize=filesize*10L+(long)(buf[0]-48);//48 is '0'/ in groovy no char
                }

                String file=null;
                for(int i=0;;i++){
                    inputStream.read(buf, i, 1);
                    if(buf[i]==(byte)0x0a){
                        file=new String(buf, 0, i);
                        break;
                    }
                }

                System.out.println("filesize="+filesize+", file="+file);

                // send '\0'
                buf[0]=0; outputStream.write(buf, 0, 1); outputStream.flush();

                // read a content of lfile
                fos=new FileOutputStream(prefix==null ? lfile : prefix+file);
                int foo;
                while(true)
                {
                    if(buf.length<filesize) foo=buf.length;
                    else foo=(int)filesize;
                    foo=inputStream.read(buf, 0, foo);
                    if(foo<0)
                    {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize-=foo;
                    if(filesize==0L) break;
                }
                fos.close();
                fos=null;

                if(checkAck(inputStream)!=0)
                {
                    return ;
                }

                // send '\0'
                buf[0]=0; outputStream.write(buf, 0, 1); outputStream.flush();
            }
            channel.disconnect();
            session.disconnect();


            return ;
        }
        catch(Exception e){
            System.out.println(e);
            try{if(fos!=null)fos.close();}catch(Exception ee){}
        }
    }

    private static void closeChannelAndSession(ChannelExec sshChannel)
    {
        LOG.info("Close session ${sshChannel.session.host}");
//        println "Close session ${sshChannel.session.host}";
        sshChannel.disconnect();
        sshChannel.session.disconnect()
    }

    private static String getSuccessFinishEventCommand(final String backupFileName, final Serializable hostId)
    {
        return "wget \"http://"+SERVER_URL+":8080/backuper-0.1/backup/successFinishEvent?fileName=${backupFileName}&hostId=${hostId}\""
    }

    private static String getFailFinishEventCommand(final String backupFileName, final Serializable hostId)
    {
        return "wget \"http://"+SERVER_URL+":8080/backuper-0.1/backup/failFinishEvent?fileName=${backupFileName}&hostId=${hostId}\""
    }

    @SuppressWarnings("GroovyBreak")
    private static String readResultFromSSHChannel(final InputStream inputStream, final ChannelExec sshChannel)
    {
        byte[] temporary =new byte[1024];
        StringBuilder result = new StringBuilder();
        while(true)
        {
            while(inputStream.available()>0)
            {
                int numberOfReadBytes=inputStream.read(temporary, 0, 1024);
                if(numberOfReadBytes <0 )break;
                result.append(new String(temporary, 0, numberOfReadBytes));
            }
            if(sshChannel.closed)
            {
                println "exit-status: ${sshChannel.exitStatus}"
                break;
            }
        }
        return result.toString();
    }

    private static ChannelExec createRemoteChannel(RemoteHost remoteHost)
    {
        JSch jsch=new JSch();
        Session session=jsch.getSession(remoteHost.login, remoteHost.ipAddress, remoteHost.sshPort);
        session.password = remoteHost.password;
        Properties config = new Properties();
        config["StrictHostKeyChecking"] = "no";
        session.config = config;
        session.connect();
        final Channel sshChannel = session.openChannel("exec");
        ((ChannelExec)sshChannel).errStream = System.err;
        sshChannel.inputStream = null;
        return ((ChannelExec)sshChannel);
    }

    void getRemoteSSHConnect(RemoteHost remoteHost)
    {

    }



           /* byte[] tmp=new byte[1024];
            while(true){
                while(stream.available()>0){
                    int i=stream.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.println(new String(tmp, 0, i));
                }
                if(channel.closed)
                {
                    System.out.println("exit-status: "+ channel.exitStatus);
                    break;
                }
                try
                {
                    Thread.sleep(1000);
                }
                catch(Exception ignored)
                {}
            }
            channel.disconnect();
            session.disconnect();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }*/
    private static int checkAck(InputStream inputStream) throws IOException
    {
        int b=inputStream.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(b==0) return b;
        if(b==-1) return b;

        if(b==1 || b==2){
            StringBuffer sb=new StringBuffer();
            int c=0;
            while(c!='\n')
            {
                c=inputStream.read();
                sb.append((char)c);
            }
            if(b==1){ // error
                System.out.print(sb.toString());
            }
            if(b==2){ // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        RemoteHost remoteHost = jobExecutionContext.jobDetail.jobDataMap["remoteHost"] as RemoteHost;
        makeFullHostBackup(remoteHost.id)
        println (remoteHost.ipAddress + new Date());
    }
}