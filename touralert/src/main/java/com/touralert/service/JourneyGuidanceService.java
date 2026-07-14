package com.touralert.service;

import com.touralert.model.Incident;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class JourneyGuidanceService {

    public String buildSuggestion(String hazardType, String destination, String origin) {
        String normalizedType = hazardType == null ? "hazard" : hazardType.toUpperCase(Locale.ROOT);
        String normalizedDestination = destination == null ? "your destination" : destination;
        String normalizedOrigin = origin == null ? "your current location" : origin;

        return switch (normalizedType) {
            case "LANDSLIDE" -> "Avoid the direct route to " + normalizedDestination + " from " + normalizedOrigin + ". Use an alternate valley road and delay departure until conditions improve.";
            case "FLOOD" -> "Reroute around low-lying areas near " + normalizedDestination + " and keep emergency supplies ready.";
            case "EARTHQUAKE" -> "Pause travel near " + normalizedDestination + " and seek sturdy shelter rather than continuing through open roads.";
            case "STRIKE" -> "Delay travel to " + normalizedDestination + " and confirm transport availability before departure.";
            default -> "Stay alert near " + normalizedDestination + " and consider delaying travel until the hazard is cleared.";
        };
    }

    public String buildAlertMessage(Incident incident, String destination) {
        if (incident == null) {
            return "A new hazard has been detected near your planned journey.";
        }

        String recommendation = buildSuggestion(incident.getType(), destination, "your current route");
        return "Travel warning: " + incident.getType() + " near " + destination + ". " + recommendation;
    }
}
