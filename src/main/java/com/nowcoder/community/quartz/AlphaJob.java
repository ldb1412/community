package com.nowcoder.community.quartz;/**
 * @author DB1412
 * @create 2023-04-01 11:36
 */

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *@ClassName AlphaJob
 *@Description TODO
 *@Author DB1412
 *@Date 2023-04-01 11:36
 */
public class AlphaJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(Thread.currentThread().getName() + ": execute a quartz job.");
    }
}
