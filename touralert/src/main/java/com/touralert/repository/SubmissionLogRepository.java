package com.touralert.repository;

import com.touralert.model.SubmissionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionLogRepository extends JpaRepository<SubmissionLog, Long> {
    List<SubmissionLog> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}
