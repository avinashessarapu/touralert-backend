package com.touralert.controller;

import com.touralert.model.*;
import com.touralert.repository.*;
import com.touralert.service.JourneyGuidanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private JourneyGuidanceService journeyGuidanceService;

    // Trigger a live engine scan and return unread notifications for a user
    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        List<Trip> userTrips = tripRepository.findByTravelerId(userId);
        List<Incident> activeIncidents = incidentRepository.findByStatusNotIgnoreCase("RESOLVED");

        for (Trip trip : userTrips) {
            if (trip == null || trip.getDestination() == null) {
                continue;
            }

            boolean isJourneyActive = "PLANNED".equalsIgnoreCase(trip.getStatus()) || "ONGOING".equalsIgnoreCase(trip.getStatus());
            if (!isJourneyActive) {
                continue;
            }

            for (Incident incident : activeIncidents) {
                if (incident == null || incident.getRouteOrLocation() == null) {
                    continue;
                }

                if (!isRelevantToTrip(trip, incident)) {
                    continue;
                }

                String typeKeyword = incident.getType() == null ? "HAZARD" : incident.getType();
                boolean alreadyNotified = notificationRepository
                    .existsByRecipientIdAndRelatedTripIdAndMessageContaining(userId, trip.getId(), typeKeyword);

                if (!alreadyNotified) {
                    String alertMessage = journeyGuidanceService.buildAlertMessage(incident, trip.getDestination());
                    Notification alert = new Notification(alertMessage, trip.getTraveler(), trip);
                    notificationRepository.save(alert);
                }
            }
        }

        return notificationRepository.findByRecipientIdAndIsReadFalse(userId);
    }

    private boolean isRelevantToTrip(Trip trip, Incident incident) {
        String destination = trip.getDestination() == null ? "" : trip.getDestination().toLowerCase();
        String startLocation = trip.getStartLocation() == null ? "" : trip.getStartLocation().toLowerCase();
        String location = incident.getRouteOrLocation() == null ? "" : incident.getRouteOrLocation().toLowerCase();

        return !destination.isEmpty() && (
            location.contains(destination) ||
            destination.contains(location) ||
            location.contains(startLocation) ||
            startLocation.contains(location)
        );
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