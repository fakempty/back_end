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
public class CleanupJob implements Job {

    private final QuartzTaskService taskService;

    public CleanupJob(QuartzTaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap(); // Об'єднує дані JobDetail та Trigger із пріоритетом тригера[cite: 7, 8]

        String targetTable = dataMap.getString("targetTable");
        int retentionDays = dataMap.getInt("retentionDays");

        if (targetTable == null) {
            throw new JobExecutionException("Параметр 'targetTable' відсутній у JobDataMap!");
        }

        taskService.executeCleanup(targetTable, retentionDays);
    }
}