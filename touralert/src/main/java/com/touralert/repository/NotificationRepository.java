package com.touralert.repository;

import com.touralert.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Fetch only unread alerts for a specific user
    List<Notification> findByRecipientIdAndIsReadFalse(Long userId);

    // Check if the exact same warning has already been generated
    boolean existsByRecipientIdAndRelatedTripIdAndMessageContaining(Long userId, Long tripId, String keyword);
}