/**
 * Created by hacker13ua on 12.02.14.
 */
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.*;

import org.apache.log4j.Logger;
import org.esurovskiy.BackupProcessorService;
import org.esurovskiy.RemoteHost;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class BackupJobScheduler
{
    private static Logger log = Logger.getLogger(BackupJobScheduler.class);
    private static BackupJobScheduler JOB_SCHEDULER = new BackupJobScheduler();
    private Scheduler scheduler = null;

    public BackupJobScheduler()
    {
    }
    public static BackupJobScheduler getInstance()
    {
        return JOB_SCHEDULER;
    }

    public void startup()
    {
        try
        {
            // and start it off
            scheduler = StdSchedulerFactory.defaultScheduler;
            System.out.println("NAME: " + scheduler.schedulerName);
            scheduler.start();
            for (RemoteHost remoteHost : RemoteHost.findAllByCronExpressionIsNotNull())
            {
                // define the job and tie it to our HelloJob class
                JobDetail job = newJob(BackupProcessorService.class)
                        .withIdentity("backup_job_service_${remoteHost.id}", "group")
                        .build();
                job.jobDataMap["remoteHost"] = remoteHost;
                // Trigger a job that repeats every 20 seconds
                Trigger trigger = newTrigger()
                        .withIdentity("trigger", "group")
//                        .withSchedule(cronSchedule("0/20 * * * * ?"))
                        .withSchedule(cronSchedule(remoteHost.cronExpression))
                        .build();
                System.out.println("Starting Jobs");
                // Tell quartz to schedule the job using our trigger
                scheduler.scheduleJob(job, trigger);
            }

            scheduler.start();
        }
        catch (SchedulerException se)
        {
            se.printStackTrace();
        }
    }

    public void shutdown()
    {
        try
        {
            scheduler.shutdown();
        }
        catch (SchedulerException se)
        {
            se.printStackTrace();
        }
    }
}