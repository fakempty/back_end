package org.example.back_end1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuartzTaskService {

    @Transactional
    public void executeCleanup(String targetTable, int retentionDays) {
        // Логіка очищення (ідемпотентна: повторний запуск не зламає систему)
        System.out.println("[Quartz Service] Виконується очищення таблиці: " + targetTable + " (зберігати днів: " + retentionDays + ")");
    }

    @Transactional
    public void executeReportNotification(String recipient, String reportType) {
        // Логіка надсилання звіту
        System.out.println("[Quartz Service] Надсилання звіту (" + reportType + ") для отримувача: " + recipient);
    }
}