package com.demo.be.repository;

import com.demo.be.model.ReportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportJobRepository extends JpaRepository<ReportJob, Long> {
    Optional<ReportJob> findByJobId(String jobId);
    List<ReportJob> findByRequestedByOrderByCreatedAtDesc(String requestedBy);
    List<ReportJob> findAllByOrderByCreatedAtDesc();
}
