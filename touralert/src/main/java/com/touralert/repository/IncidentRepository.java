package com.touralert.repository;

import com.touralert.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllByOrderByReportedAtDesc();
    List<Incident> findByStatusNotIgnoreCase(String status);
}