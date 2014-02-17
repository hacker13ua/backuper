package org.esurovskiy

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * Created by hacker13ua on 12.02.14.
 */
class BackupJob implements Job
{
    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        System.out.println(jobExecutionContext.getMergedJobDataMap().get("text") + new Date());
    }
}
