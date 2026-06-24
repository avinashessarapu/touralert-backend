package com.touralert.dto;

import com.touralert.model.Incident;
import com.touralert.model.Trip;
import java.util.List;

public class TripAlertReport {
    private Trip trip;
    private List<Incident> activeHazards;

    public TripAlertReport(Trip trip, List<Incident> activeHazards) {
        this.trip = trip;
        this.activeHazards = activeHazards;
    }

    // Getters and Setters
    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }
    public List<Incident> getActiveHazards() { return activeHazards; }
    public void setActiveHazards(List<Incident> activeHazards) { this.activeHazards = activeHazards; }
}