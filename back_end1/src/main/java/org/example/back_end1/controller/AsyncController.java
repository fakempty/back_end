package org.example.back_end1.controller;

import org.example.back_end1.service.AsyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/async")
public class AsyncController {

    private final AsyncService asyncService;

    public AsyncController(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @GetMapping("/run")
    public CompletableFuture<String> runAsyncTasks() {
        CompletableFuture<String> emailTask = asyncService.sendEmailNotification()
                .exceptionally(ex -> "Помилка при надсиланні листа: " + ex.getMessage());

        CompletableFuture<String> pdfTask = asyncService.generatePdfReport()
                .exceptionally(ex -> "Помилка при генерації PDF: " + ex.getMessage());

        return emailTask.thenCombine(pdfTask, (emailResult, pdfResult) -> 
            "Результат виконання: [" + emailResult + "] | [" + pdfResult + "]"
        );
    }
}