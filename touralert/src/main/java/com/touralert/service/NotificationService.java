package com.touralert.service;

import com.touralert.model.Incident;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastHazardAlert(Incident incident) {
        System.out.println("!!! SYSTEM ALERT BROADCAST: " + incident.getType() + " at " + incident.getRouteOrLocation() + " !!!");
        
        // Push the incident object instantly to all clients subscribed to /topic/hazards
        messagingTemplate.convertAndSend("/topic/hazards", incident);
    }

    public void sendDirectSafetyWarning(String email, String message) {
        // Fallback or backup logging channels
        System.out.println("Dispatching emergency route warning email to: " + email + " -> Message: " + message);
    }
}