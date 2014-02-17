package org.esurovskiy

class RemoteHost
{
    static hasMany = [postgresStorages: PostgresStorage, hbaseStorages: HbaseStorage, localFileSystemStorages: LocalFileSystemStorage]
    String ipAddress;
    String login;
    String password;
    String cronExpression;
    Integer sshPort;
    static constraints = {
        ipAddress(blank: false);
        login(blank: false);
        password(blank: false);
        cronExpression(nullable: true);
        sshPort(blank:false, size: 1..65535);
    }
}
