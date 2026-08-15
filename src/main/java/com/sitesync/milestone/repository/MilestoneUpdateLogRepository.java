package com.sitesync.milestone.repository;

import com.sitesync.milestone.entity.MilestoneUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MilestoneUpdateLogRepository extends JpaRepository<MilestoneUpdateLog, UUID> {

    List<MilestoneUpdateLog> findByMilestoneIdOrderByLoggedAtDesc(UUID milestoneId);

    @Query("""
        SELECT COALESCE(SUM(l.progressAddedPercent), 0)
        FROM MilestoneUpdateLog l
        WHERE l.milestoneId = :milestoneId
        """)
    Double sumProgressForMilestone(@Param("milestoneId") UUID milestoneId);
}