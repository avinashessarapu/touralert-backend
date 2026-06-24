package com.touralert.controller;

import com.touralert.model.*;
import com.touralert.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    // Trigger a live engine scan and return unread notifications for a user
    // Trigger an optimized, duplicate-aware live engine scan
    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        // 1. Find all ongoing or planned trips for this user
        List<Trip> userTrips = tripRepository.findByTravelerId(userId);
        
        // 2. Fetch all active hazards
        List<Incident> activeIncidents = incidentRepository.findByStatusNotIgnoreCase("RESOLVED");
        
        // 3. Scan and cleanly generate notifications
        for (Trip trip : userTrips) {
            for (Incident incident : activeIncidents) {
                if (incident.getRouteOrLocation().toLowerCase().contains(trip.getDestination().toLowerCase())) {
                    
                    String typeKeyword = incident.getType(); // e.g., "LANDSLIDE"
                    
                    // Business Rule Check: Does this specific alert already exist for this trip?
                    boolean alreadyNotified = notificationRepository
                        .existsByRecipientIdAndRelatedTripIdAndMessageContaining(userId, trip.getId(), typeKeyword);
                    
                    if (!alreadyNotified) {
                        String alertMessage = "WARNING: " + typeKeyword + " reported near your destination (" 
                                              + trip.getDestination() + "). Hazard details: " + incident.getDescription();
                        
                        Notification alert = new Notification(alertMessage, trip.getTraveler(), trip);
                        notificationRepository.save(alert);
                    }
                }
            }
        }

        // 4. Return all unread notifications currently sitting in the database for this user
        return notificationRepository.findByRecipientIdAndIsReadFalse(userId);
    }


    // 5. MARK NOTIFICATION AS READ (Dismiss alert)
    // URL: PATCH http://localhost:8080/api/notifications/dismiss/1
    @PatchMapping("/dismiss/{notificationId}")
    public String dismissNotification(@PathVariable Long notificationId) {
        return notificationRepository.findById(notificationId).map(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
            return "Notification marked as read successfully.";
        }).orElseThrow(() -> new RuntimeException("Notification record not found with ID: " + notificationId));
    }

    
}