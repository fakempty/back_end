package org.example.back_end1.job;

import org.example.back_end1.service.QuartzTaskService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@Component
public class ReportNotificationJob implements Job {

    private final QuartzTaskService taskService;

    public ReportNotificationJob(QuartzTaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();

        String recipient = dataMap.getString("recipient");
        String reportType = dataMap.getString("reportType");

        taskService.executeReportNotification(recipient, reportType);
    }
}