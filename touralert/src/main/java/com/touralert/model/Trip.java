package com.touralert.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trips")

@EntityListeners(AuditingEntityListener.class)
public class Trip {

    @org.hibernate.annotations.SQLDelete(sql = "UPDATE trips SET is_deleted = true WHERE id=?")
@org.hibernate.annotations.SQLRestriction("is_deleted = false")


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destination;
    private String startLocation;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // e.g., "PLANNED", "ONGOING", "COMPLETED"
    private boolean isDeleted = false;

    public boolean isDeleted() { return isDeleted; }
public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @CreatedDate
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(nullable = false)
private LocalDateTime updatedAt;

    // LINK TO USER: Many trips belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User traveler;

    public Trip() {}

    public Trip(String destination, String startLocation, LocalDate startDate, LocalDate endDate, User traveler) {
        this.destination = destination;
        this.startLocation = startLocation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.traveler = traveler;
        this.status = "PLANNED";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getTraveler() { return traveler; }
    public void setTraveler(User traveler) { this.traveler = traveler; }

    public LocalDateTime getCreatedAt() { return createdAt; }
public LocalDateTime getUpdatedAt() { return updatedAt; }

}