package com.tahs.infrastructure.scheduler;

import com.tahs.application.usecase.IngestionService;
import com.tahs.config.AppConfig;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BookDownloadScheduler {

    private final IngestionService ingestionService;
    private final AppConfig appConfig;
    private final ScheduledExecutorService scheduler;

    public BookDownloadScheduler(IngestionService ingestionService, AppConfig appConfig) {
        this.ingestionService = ingestionService;
        this.appConfig = appConfig;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        long interval = appConfig.downloadIntervalSeconds();
        System.out.println("[SCHEDULER] Starting background task every " + interval + " seconds.");

        scheduler.scheduleAtFixedRate(this::runDownloadTask, 0, interval, TimeUnit.SECONDS);
    }

    private void runDownloadTask() {
        try {
            System.out.println("[SCHEDULER] Triggering autonomous download...");
            Integer bookId = ingestionService.ingestNextRandom(LocalDateTime.now());
            System.out.println("[API] Received ingestion request for book " + bookId);

            if (ingestionService.existsInDatalake(bookId)) {
                System.out.println("[API] Book " + bookId + " already exists in datalake. Skipping download.");
                return;
            }

            boolean ok = ingestionService.downloadBookToStaging(bookId);
            if (!ok) {
                System.out.println("[API] Download failed or invalid book.");
                return;
            }

            boolean datalakeOk = ingestionService.moveToDatalake(bookId, LocalDateTime.now());
            if (!datalakeOk) {
                System.out.println("[API] Failed to move files to datalake.");
            }
        } catch (Exception e) {
            System.err.println("[SCHEDULER] Error in download task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        scheduler.shutdown();
    }
}
