package com.touralert.controller;

import com.touralert.dto.TripAlertReport;
import com.touralert.model.Incident;
import com.touralert.model.Trip;
import com.touralert.model.User;
import com.touralert.repository.IncidentRepository;
import com.touralert.repository.TripRepository;
import com.touralert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

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
@Autowired
private IncidentRepository incidentRepository;
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

        // Business logic: Find incidents where the reported location contains or matches the destination name
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
            
            // Basic validation check for acceptable states
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
        
        // Create page request configuration: Sort by 'createdAt' field descending (newest first)
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return tripRepository.findAll(pageable);
    }
    

}