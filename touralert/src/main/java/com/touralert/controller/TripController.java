package com.touralert.controller;

import com.touralert.dto.TripAlertReport;
import com.touralert.model.Incident;
import com.touralert.model.Trip;
import com.touralert.repository.IncidentRepository;
import com.touralert.repository.TripRepository;
import com.touralert.repository.UserRepository;
import com.touralert.service.JourneyGuidanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.touralert.dto.TripRiskAnalysis;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private JourneyGuidanceService journeyGuidanceService;

    // 1. Endpoint to plan a new trip
    // URL: POST http://localhost:8080/api/trips?userId=1
    @PostMapping
    public String planTrip(@RequestBody Trip trip, @RequestParam Long userId) {
        return userRepository.findById(userId).map(user -> {
            trip.setTraveler(user);
            trip.setStatus("PLANNED");
            tripRepository.save(trip);
            return "Trip to " + trip.getDestination() + " planned successfully for " + user.getUsername() + "!";
        }).orElse("Error: User profile not found. Cannot schedule trip.");
    }

    // 2. Endpoint to fetch all trips planned by a specific user
    // URL: GET http://localhost:8080/api/trips/user/1
    @GetMapping("/user/{userId}")
    public List<Trip> getTripsByUser(@PathVariable Long userId) {
        return tripRepository.findByTravelerId(userId);
    }

    // 3. CHECK TRIP SAFETY: Automatically cross-reference active hazards for a trip
    // URL: GET http://localhost:8080/api/trips/{tripId}/alerts
    @GetMapping("/{tripId}/alerts")
    public TripAlertReport checkTripSafety(@PathVariable Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        List<Incident> matchingHazards = incidentRepository.findAll().stream()
                .filter(incident -> incident.getRouteOrLocation().toLowerCase().contains(trip.getDestination().toLowerCase()))
                .toList();

        return new TripAlertReport(trip, matchingHazards);
    }

    // 4. UPDATE TRIP STATUS (Start, Complete, or Cancel Trip)
    // URL: PUT http://localhost:8080/api/trips/1/status?status=ONGOING
    @PutMapping("/{tripId}/status")
    public String updateTripStatus(@PathVariable Long tripId, @RequestParam String status) {
        return tripRepository.findById(tripId).map(trip -> {
            String upperStatus = status.toUpperCase();
            
            if (!upperStatus.equals("PLANNED") && !upperStatus.equals("ONGOING") && 
                !upperStatus.equals("COMPLETED") && !upperStatus.equals("CANCELLED")) {
                throw new RuntimeException("Invalid trip status. Choose from: PLANNED, ONGOING, COMPLETED, CANCELLED");
            }
            
            trip.setStatus(upperStatus);
            tripRepository.save(trip);
            return "Trip destination " + trip.getDestination() + " status updated to: " + upperStatus;
        }).orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));
    }

    // 5. GET ALL TRIPS (PAGINATED & SORTED BY CREATED DATE)
    // URL Example: GET http://localhost:8080/api/trips/paginated?page=0&size=5
    @GetMapping("/paginated")
    public Page<Trip> getAllTripsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return tripRepository.findAll(pageable);
    }

    // 6. SOFT DELETE A TRIP
    // URL: DELETE http://localhost:8080/api/trips/1
    @DeleteMapping("/{id}")
    public String deleteTrip(@PathVariable Long id) {
        return tripRepository.findById(id).map(trip -> {
            tripRepository.delete(trip);
            return "Trip to " + trip.getDestination() + " was deleted successfully.";
        }).orElseThrow(() -> new RuntimeException("Trip not found with ID: " + id));
    }

    // 7. LIVE LOCATION GEOCONTEXT RADAR SCANNER
    // URL: GET http://localhost:8080/api/trips/radar?currentLat=18.25&currentLng=83.05
    @GetMapping("/radar")
    public List<String> scanNearbyHazards(
            @RequestParam double currentLat, 
            @RequestParam double currentLng) {
        
        List<String> warnings = new ArrayList<>();
        List<Incident> activeHazards = incidentRepository.findByStatusNotIgnoreCase("RESOLVED");
        
        for (Incident hazard : activeHazards) {
            double latDiff = hazard.getLatitude() - currentLat;
            double lngDiff = hazard.getLongitude() - currentLng;
            double distance = Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
            
            if (distance <= 0.1) {
                warnings.add("CRITICAL PROXIMITY ALERT: " + hazard.getType() + 
                             " detected ahead at " + hazard.getRouteOrLocation() + 
                             ". Description: " + hazard.getDescription());
            }
        }
        
        if (warnings.isEmpty()) {
            warnings.add("Route clear. No active hazards detected within your immediate vicinity.");
        }
        
        return warnings;
    }


    // 8. GET ADVANCED TRIP RISK ASSESSMENT
    // URL: GET http://localhost:8080/api/trips/1/risk-assessment
    @GetMapping("/{tripId}/risk-assessment")
    public TripRiskAnalysis getTripRiskAssessment(@PathVariable Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        List<Incident> activeHazards = incidentRepository.findByStatusNotIgnoreCase("RESOLVED").stream()
                .filter(incident -> isRelevantToTrip(trip, incident))
                .toList();

        TripRiskAnalysis analysis = new TripRiskAnalysis(trip, activeHazards);
        analysis.setRecommendation(journeyGuidanceService.buildSuggestion(
                activeHazards.isEmpty() ? "SAFE" : activeHazards.get(0).getType(),
                trip.getDestination(),
                trip.getStartLocation()
        ));
        return analysis;
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
}
