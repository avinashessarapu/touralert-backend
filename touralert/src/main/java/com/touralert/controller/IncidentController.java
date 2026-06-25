package com.touralert.controller;

import com.touralert.model.AuditLog;
import com.touralert.model.Incident;
import com.touralert.repository.AuditLogRepository;
import com.touralert.repository.IncidentRepository;
import com.touralert.repository.UserRepository;
import com.touralert.service.FileStorageService;
import com.touralert.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FileStorageService fileStorageService; // Inject storage engine

    // 1. REPORT AN INCIDENT WITH IMAGE ATTACHMENT
    // URL: POST http://localhost:8080/api/incidents/upload?userId=1
    @PostMapping("/upload")
    public String reportIncidentWithImage(
            @RequestParam("type") String type,
            @RequestParam("description") String description,
            @RequestParam("routeOrLocation") String routeOrLocation,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        return userRepository.findById(userId).map(user -> {
            Incident incident = new Incident();
            incident.setType(type);
            incident.setDescription(description);
            incident.setRouteOrLocation(routeOrLocation);
            incident.setLatitude(latitude);
            incident.setLongitude(longitude);
            incident.setStatus("PENDING");
            incident.setReporter(user);
            incident.setReportedAt(java.time.LocalDateTime.now());

            // Process image file if present
            if (file != null && !file.isEmpty()) {
                String savedImageUrl = fileStorageService.storeFile(file);
                incident.setImageUrl(savedImageUrl);
            }

            incidentRepository.save(incident);
            return "Incident reported successfully with image by " + user.getUsername() + "!";
        }).orElse("Error: User profile not found. Cannot report incident.");
    }

    // 2. UPDATE INCIDENT STATUS (Role-Based Admin Protection + Broker Broadcast)
    @PutMapping("/{id}/status")
    public String updateIncidentStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            @RequestParam Long adminUserId) {
        
        boolean isAdmin = userRepository.existsByIdAndRole(adminUserId, "ADMIN");
        if (!isAdmin) {
            throw new RuntimeException("Access Denied: Only users with ADMIN privileges can update incident statuses.");
        }

        return incidentRepository.findById(id).map(incident -> {
            String oldStatus = incident.getStatus();
            String newStatus = status.toUpperCase();
            
            incident.setStatus(newStatus);
            incidentRepository.save(incident);

            if (newStatus.equals("VERIFIED")) {
                notificationService.broadcastHazardAlert(incident);
            }

            String details = "Incident ID " + id + " status modified from " + oldStatus + " to " + newStatus + " by Admin ID: " + adminUserId;
            AuditLog log = new AuditLog("INCIDENT_STATUS_UPDATE", details, "ADMIN_" + adminUserId);
            auditLogRepository.save(log);

            return "Incident status successfully updated to: " + newStatus;
        }).orElse("Error: Incident report not found.");
    }

    // 3. GET ACTIVE HAZARDS ONLY
    @GetMapping("/active")
    public List<Incident> getActiveIncidents() {
        return incidentRepository.findByStatusNotIgnoreCase("RESOLVED");
    }
}