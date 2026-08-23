package com.sitesync.delivery.repository;

import com.sitesync.delivery.entity.PurchaseOrder;
import com.sitesync.delivery.entity.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByProjectIdOrderByExpectedAtDesc(UUID projectId);

    /** Used by OverdueDeliveryScheduler - every still-pending order whose expected time has already passed. */
    List<PurchaseOrder> findByStatusAndExpectedAtBefore(PurchaseOrderStatus status, Instant time);
    
}