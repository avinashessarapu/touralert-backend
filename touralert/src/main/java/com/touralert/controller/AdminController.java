package com.touralert.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.touralert.repository.IncidentRepository;
import com.touralert.model.Incident;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IncidentRepository incidentRepository;

    // Simple scaffold: submit incident to government portal (stub)
    @PostMapping("/incidents/{id}/submit-gov")
    public Map<String, String> submitToGovernment(@PathVariable Long id) {
        Incident incident = incidentRepository.findById(id).orElse(null);
        if (incident == null) throw new RuntimeException("Incident not found");
        // In production, integrate with government API (OAuth, signed payload, etc.)
        String govUrl = "https://gov.example.org/verify?incidentId=" + id;
        // mark as submitted (note: do not change status here)
        return Map.of("status", "submitted", "govUrl", govUrl);
    }
}
