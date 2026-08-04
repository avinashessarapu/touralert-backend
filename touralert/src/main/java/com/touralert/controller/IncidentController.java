package com.touralert.controller;

import com.touralert.model.Incident;
import com.touralert.repository.IncidentRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public IncidentController(IncidentRepository incidentRepository, SimpMessagingTemplate messagingTemplate) {
        this.incidentRepository = incidentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAllByOrderByReportedAtDesc();
    }

    @PostMapping
    public Incident saveIncident(@RequestBody Incident incident) {
        if (incident.getReportedAt() == null) {
            incident.setReportedAt(LocalDateTime.now());
        }
        if (incident.getStatus() == null || incident.getStatus().isBlank()) {
            incident.setStatus("OPEN");
        }
        Incident saved = incidentRepository.save(incident);
        messagingTemplate.convertAndSend("/topic/hazards", saved);
        return saved;
    }
}