package org.example.back_end1.config;

import org.example.back_end1.job.CleanupJob;
import org.example.back_end1.job.ReportNotificationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfig {

    // --- 1. Завдання CleanupJob та його тригери (ДВА тригери для однієї задачі) ---
    @Bean
    public JobDetail cleanupJobDetail() {
        return JobBuilder.newJob(CleanupJob.class)
                .withIdentity("cleanupJob", "maintenanceGroup")
                .usingJobData("targetTable", "default_logs") // Значення за замовчуванням[cite: 7, 8]
                .usingJobData("retentionDays", 30)
                .storeDurably()
                .build();
    }

    // Тригер 1: Щоденний Cron-тригер із фіксованим часовим поясом[cite: 10]
    @Bean
    public Trigger cleanupDailyTrigger(JobDetail cleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(cleanupJobDetail)
                .withIdentity("cleanupDailyTrigger", "maintenanceGroup")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0)
                        .inTimeZone(TimeZone.getTimeZone("GMT"))) // Фіксований часовий пояс[cite: 10]
                .build();
    }

    // Тригер 2: Додатковий тригер із перевизначенням параметра через JobDataMap (Пріоритет тригера)[cite: 7, 8]
    @Bean
    public Trigger cleanupOverrideTrigger(JobDetail cleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(cleanupJobDetail)
                .withIdentity("cleanupOverrideTrigger", "maintenanceGroup")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(6)
                        .repeatForever())
                .usingJobData("targetTable", "audit_logs") // Перевизначає дефолтне значення з JobDetail[cite: 7, 8]
                .build();
    }

    // --- 2. Завдання ReportNotificationJob та його тригер ---
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
    public Trigger reportTrigger(JobDetail reportJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(reportJobDetail)
                .withIdentity("reportTrigger", "reportsGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?") // О 8:00 ранку щодня
                        .inTimeZone(TimeZone.getTimeZone("GMT")))
                .build();
    }
}