package com.touralert.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private LocalDateTime generatedAt;
    private boolean isRead;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip relatedTrip;

    public Notification() {}

    public Notification(String message, User recipient, Trip relatedTrip) {
        this.message = message;
        this.recipient = recipient;
        this.relatedTrip = relatedTrip;
        this.generatedAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public Trip getRelatedTrip() { return relatedTrip; }
    public void setRelatedTrip(Trip relatedTrip) { this.relatedTrip = relatedTrip; }
}