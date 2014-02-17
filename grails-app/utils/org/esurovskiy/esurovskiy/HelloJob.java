package org.esurovskiy.esurovskiy;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * Created by hacker13ua on 28.01.14.
 */
public class HelloJob implements Job 
{
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException 
    {
        System.out.println("0000" + new Date());
    }
}
