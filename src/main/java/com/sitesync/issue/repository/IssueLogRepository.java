package com.sitesync.issue.repository;

import com.sitesync.issue.entity.IssueLog;
import com.sitesync.issue.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueLogRepository extends JpaRepository<IssueLog, UUID> {
    List<IssueLog> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
    long countByProjectIdAndStatus(UUID projectId, IssueStatus status);
}