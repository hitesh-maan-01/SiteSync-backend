package com.sitesync.delivery.repository;

import com.sitesync.delivery.entity.DeliverySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryScheduleRepository extends JpaRepository<DeliverySchedule, UUID> {

    // Used by OverdueDeliveryScheduler — find all PENDING deliveries past their date
    List<DeliverySchedule> findByStatusAndExpectedDateBefore(
            DeliverySchedule.Status status, LocalDate date);

    // All deliveries for a project
    List<DeliverySchedule> findAllByProjectIdOrderByExpectedDateAsc(UUID projectId);

    // All deliveries for a project filtered by status
    List<DeliverySchedule> findAllByProjectIdAndStatus(
            UUID projectId, DeliverySchedule.Status status);

    // Upcoming deliveries (today or future) for a project
    @Query("SELECT d FROM DeliverySchedule d WHERE d.projectId = :projectId " +
           "AND d.expectedDate >= :today ORDER BY d.expectedDate ASC")
    List<DeliverySchedule> findUpcomingByProjectId(
            @Param("projectId") UUID projectId,
            @Param("today") LocalDate today);

    // Count pending deliveries for a project (used in dashboard summary)
    long countByProjectIdAndStatus(UUID projectId, DeliverySchedule.Status status);
}