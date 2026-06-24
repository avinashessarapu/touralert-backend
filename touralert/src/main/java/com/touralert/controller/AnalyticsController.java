package com.touralert.controller;

import com.touralert.dto.LocationRiskReport;
import com.touralert.model.Incident;
import com.touralert.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private IncidentRepository incidentRepository;

    // 1. DYNAMIC LOCATION RISK REPORT
    // URL: GET http://localhost:8080/api/analytics/risk-matrix
    @GetMapping("/risk-matrix")
    public List<LocationRiskReport> getLocationRiskMatrix() {
        // Fetch all active incidents that are not resolved
        List<Incident> activeIncidents = incidentRepository.findByStatusNotIgnoreCase("RESOLVED");

        // Group active incidents by location name and count occurrences using Streams
        Map<String, Long> hazardsByLocation = activeIncidents.stream()
                .collect(Collectors.groupingBy(
                        incident -> incident.getRouteOrLocation().trim(),
                        Collectors.counting()
                ));

        // Map the aggregated results into clean DTO objects for the frontend chart/table
        return hazardsByLocation.entrySet().stream()
                .map(entry -> new LocationRiskReport(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}