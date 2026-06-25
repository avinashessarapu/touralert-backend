package com.touralert.service;

import com.touralert.repository.AuditLogRepository;
import com.touralert.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DatabaseCleanerService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // Cron expression: Automatically fires at midnight every single day
    // For testing/demonstration, "0 */5 * * * *" means every 5 minutes
    @Scheduled(cron = "0 0 0 * * *")
    public void flushLegacySystemLogs() {
        System.out.println("====== STARTING AUTOMATED DATABASE MAINTENANCE WORKER ======");
        
        // Calculate cutoff threshold: e.g., auto-purge read notifications older than 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        
        // In production, you would invoke custom repository deletion mappings:
        // notificationRepository.deleteByIsReadTrueAndGeneratedAtBefore(cutoffDate);
        
        System.out.println("Automated system log maintenance cycle finished cleanly.");
    }
}