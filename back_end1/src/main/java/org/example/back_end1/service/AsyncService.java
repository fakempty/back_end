package org.example.back_end1.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {

    @Async("taskExecutor")
    public CompletableFuture<String> sendEmailNotification() {
        System.out.println("Поточний потік для листа: " + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("Лист успішно надіслано!");
    }

    @Async("taskExecutor")
    public CompletableFuture<String> generatePdfReport() {
        System.out.println("Поточний потік для PDF: " + Thread.currentThread().getName());
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("PDF-звіт згенеровано!");
    }

    @Async("taskExecutor")
    public CompletableFuture<String> callExternalService() {
        System.out.println("Поточний потік для зовнішнього сервісу: " + Thread.currentThread().getName());
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("Дані від зовнішнього сервісу отримано!");
    }
}