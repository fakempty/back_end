package org.example.back_end1.config;

import org.example.back_end1.job.CleanupJob;
import org.example.back_end1.job.ReportNotificationJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail cleanupJobDetail() {
        return JobBuilder.newJob(CleanupJob.class)
                .withIdentity("cleanupJob", "maintenanceGroup")
                .usingJobData("targetTable", "default_logs")
                .usingJobData("retentionDays", 30)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger cleanupDailyTrigger(@Qualifier("cleanupJobDetail") JobDetail cleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(cleanupJobDetail)
                .withIdentity("cleanupDailyTrigger", "maintenanceGroup")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0)
                        .inTimeZone(TimeZone.getTimeZone("GMT")))
                .build();
    }

    @Bean
    public Trigger cleanupOverrideTrigger(@Qualifier("cleanupJobDetail") JobDetail cleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(cleanupJobDetail)
                .withIdentity("cleanupOverrideTrigger", "maintenanceGroup")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(6)
                        .repeatForever())
                .usingJobData("targetTable", "audit_logs")
                .build();
    }

    @Bean
    public JobDetail reportJobDetail() {
        return JobBuilder.newJob(ReportNotificationJob.class)
                .withIdentity("reportJob", "reportsGroup")
                .usingJobData("recipient", "admin@example.com")
                .usingJobData("reportType", "DAILY_SUMMARY")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger reportTrigger(@Qualifier("reportJobDetail") JobDetail reportJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(reportJobDetail)
                .withIdentity("reportTrigger", "reportsGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("GMT")))
                .build();
    }
}