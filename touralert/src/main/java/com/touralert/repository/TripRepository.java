package com.touralert.repository;

import com.touralert.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    // Custom query method: Spring will automatically find all trips 
    // where the traveler's ID matches the one passed in!
    List<Trip> findByTravelerId(Long userId);
}