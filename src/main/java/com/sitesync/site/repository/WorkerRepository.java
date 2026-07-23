package com.sitesync.site.repository;

import com.sitesync.site.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {
    List<Worker> findByProjectIdOrderByOnboardedAtAsc(UUID projectId);
}