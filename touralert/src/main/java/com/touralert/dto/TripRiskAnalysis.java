package com.touralert.dto;

import com.touralert.model.Trip;
import com.touralert.model.Incident;
import java.util.List;

public class TripRiskAnalysis {
    private Trip trip;
    private List<Incident> nearbyHazards;
    private int safetyScore; // Percentage rating: 0% (Dangerous) to 100% (Safe)
    private String riskAssessment; // "LOW", "MEDIUM", "HIGH"

    public TripRiskAnalysis(Trip trip, List<Incident> nearbyHazards) {
        this.trip = trip;
        this.nearbyHazards = nearbyHazards;
        calculateRisk();
    }

    private void calculateRisk() {
        int hazardCount = nearbyHazards.size();
        
        // Dynamic deduction system
        if (hazardCount == 0) {
            this.safetyScore = 100;
            this.riskAssessment = "LOW RISK";
        } else if (hazardCount <= 2) {
            this.safetyScore = 75;
            this.riskAssessment = "MEDIUM RISK (PROCEED WITH CAUTION)";
        } else {
            this.safetyScore = 30;
            this.riskAssessment = "HIGH RISK (ALERT)";
        }
    }

    // Getters
    public Trip getTrip() { return trip; }
    public List<Incident> getNearbyHazards() { return nearbyHazards; }
    public int getSafetyScore() { return safetyScore; }
    public String getRiskAssessment() { return riskAssessment; }
}