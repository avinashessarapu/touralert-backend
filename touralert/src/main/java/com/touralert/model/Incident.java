package com.touralert.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String routeOrLocation;
    private double latitude;
    private double longitude;

    private String imageUrl;

    private String type;
    private String description;
    private String status; // e.g., "PENDING", "VERIFIED", "RESOLVED"
    private LocalDateTime reportedAt;

    public double getLatitude() { return latitude; }
public void setLatitude(double latitude) { this.latitude = latitude; }
public double getLongitude() { return longitude; }
public void setLongitude(double longitude) { this.longitude = longitude; }

public String getImageUrl() { return imageUrl; }
public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // LINK TO THE USER: Many incidents can belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter;

    

    public Incident() {}

    public Incident(String routeOrLocation, String type, String description, User reporter) {
        this.routeOrLocation = routeOrLocation;
        this.type = type;
        this.status = "PENDING";
        this.description = description;
        this.reporter = reporter;
        this.reportedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRouteOrLocation() { return routeOrLocation; }
    public void setRouteOrLocation(String routeOrLocation) { this.routeOrLocation = routeOrLocation; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
}