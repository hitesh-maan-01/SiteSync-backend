package com.sitesync.milestone.repository;

import com.sitesync.milestone.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

    List<Milestone> findByProjectIdOrderBySequenceOrderAsc(UUID projectId);

    /**
     * Counts milestones with a lower sequenceOrder than the given one
     * that are still PENDING — used by assertSequenceUnblocked to enforce
     * the strict sequential progression rule.
     */
    @Query("""
        SELECT COUNT(m) FROM Milestone m
        WHERE m.project.id = :projectId
        AND m.sequenceOrder < :sequenceOrder
        AND m.status = 'PENDING'
        """)
    long countBlockingPendingMilestones(
            @Param("projectId") UUID projectId,
            @Param("sequenceOrder") int sequenceOrder);
}