package com.touralert.dto;

public class LocationRiskReport {
    private String location;
    private long totalHazards;
    private String safetyLevel; // e.g., "SAFE", "CAUTION", "HIGH RISK"

    public LocationRiskReport(String location, long totalHazards) {
        this.location = location;
        this.totalHazards = totalHazards;
        
        // Dynamic conditional logic based on hazard frequency
        if (totalHazards == 0) {
            this.safetyLevel = "SAFE";
        } else if (totalHazards <= 2) {
            this.safetyLevel = "CAUTION";
        } else {
            this.safetyLevel = "HIGH RISK";
        }
    }

    // Getters and Setters
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public long getTotalHazards() { return totalHazards; }
    public void setTotalHazards(long totalHazards) { this.totalHazards = totalHazards; }
    public String getSafetyLevel() { return safetyLevel; }
    public void setSafetyLevel(String safetyLevel) { this.safetyLevel = safetyLevel; }
}