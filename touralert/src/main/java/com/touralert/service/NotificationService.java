package com.touralert.service;

import com.touralert.model.Incident;
import com.touralert.model.Notification;
import com.touralert.model.Trip;
import com.touralert.repository.IncidentRepository;
import com.touralert.repository.NotificationRepository;
import com.touralert.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    // Asynchronous-ready business loop to broadcast newly verified hazards
    public void broadcastHazardAlert(Incident incident) {
        // Find all active trips heading to the location of this incident
        List<Trip> activeTrips = tripRepository.findAll(); // In production, filter by destination/status directly

        for (Trip trip : activeTrips) {
            if (trip.getDestination().toLowerCase().contains(incident.getRouteOrLocation().toLowerCase())) {
                
                String typeKeyword = incident.getType();
                
                // Duplicate check
                boolean alreadyNotified = notificationRepository
                    .existsByRecipientIdAndRelatedTripIdAndMessageContaining(
                        trip.getTraveler().getId(), trip.getId(), typeKeyword
                    );

                if (!alreadyNotified) {
                    String alertMessage = "CRITICAL UPDATE: A verified " + typeKeyword + 
                                          " has been logged on your route to " + trip.getDestination() + "!";
                    
                    Notification alert = new Notification(alertMessage, trip.getTraveler(), trip);
                    notificationRepository.save(alert);
                }
            }
        }
    }
}