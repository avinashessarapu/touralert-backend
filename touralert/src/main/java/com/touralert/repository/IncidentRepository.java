package com.touralert.repository;

import com.touralert.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    
    // Automatically generates: SELECT * FROM incidents WHERE LOWER(status) != LOWER(?1)
    List<Incident> findByStatusNotIgnoreCase(String status);
}