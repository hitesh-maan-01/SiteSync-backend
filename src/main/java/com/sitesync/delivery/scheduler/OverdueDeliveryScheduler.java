package com.sitesync.delivery.scheduler;

import com.sitesync.auth.entity.User;
import com.sitesync.auth.repository.UserRepository;
import com.sitesync.delivery.entity.PurchaseOrder;
import com.sitesync.delivery.entity.PurchaseOrderStatus;
import com.sitesync.delivery.repository.PurchaseOrderRepository;
import com.sitesync.notification.entity.NotificationSeverity;
import com.sitesync.notification.service.PushNotificationService;
import com.sitesync.project.entity.Project;
import com.sitesync.project.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Runs every 15 minutes to flip overdue PENDING purchase orders to LATE
 * and notify the assigned supervisor so they can follow up with the vendor.
 *
 * @Scheduled requires @EnableScheduling on a @Configuration class or
 * the main @SpringBootApplication class — add it if not already present.
 */
@Component
public class OverdueDeliveryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(OverdueDeliveryScheduler.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    public OverdueDeliveryScheduler(
            PurchaseOrderRepository purchaseOrderRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            PushNotificationService pushNotificationService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.pushNotificationService = pushNotificationService;
    }

    /**
     * Runs every 15 minutes. Finds all PENDING orders whose expectedAt
     * has passed, marks them LATE, and notifies the supervisor assigned
     * to that project.
     *
     * fixedDelay (not fixedRate) ensures the next run only starts after
     * the previous one fully completes — avoids overlap if the DB query
     * is slow under load.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    @Transactional
    public void markOverdueDeliveries() {
        List<PurchaseOrder> overdue =
                purchaseOrderRepository.findByStatusAndExpectedAtBefore(
                        PurchaseOrderStatus.PENDING, Instant.now());

        if (overdue.isEmpty()) return;

        log.info("Marking {} purchase order(s) as LATE", overdue.size());

        for (PurchaseOrder order : overdue) {
            order.setStatus(PurchaseOrderStatus.LATE);
            purchaseOrderRepository.save(order);

            notifySupervisor(order);
        }
    }

    private void notifySupervisor(PurchaseOrder order) {
        try {
            Optional<Project> projectOpt =
                    projectRepository.findById(order.getProjectId());
            if (projectOpt.isEmpty()) return;

            Project project = projectOpt.get();
            if (project.getAssignedSupervisorId() == null) return;

            Optional<User> supervisorOpt =
                    userRepository.findById(project.getAssignedSupervisorId());
            if (supervisorOpt.isEmpty()) return;

            User supervisor = supervisorOpt.get();
            if (supervisor.getFcmToken() == null) return;

            String actionRoute =
                    "/supervisor-dash/deliveries/" + order.getProjectId();

            pushNotificationService.sendAndPersist(
                    supervisor.getFcmToken(),
                    supervisor.getId(),
                    "Delivery Overdue ⚠️",
                    "\"" + order.getMaterialName() + "\" was expected and"
                            + " hasn't arrived. Tap to follow up.",
                    NotificationSeverity.WARNING,
                    "materialDelay",
                    "View Deliveries",
                    actionRoute,
                    Map.of(
                            "projectId", order.getProjectId().toString(),
                            "purchaseOrderId", order.getId().toString()
                    )
            );
        } catch (Exception e) {
            // Non-fatal — scheduler must never fail due to a push error.
            log.warn("Failed to notify supervisor for order {}: {}",
                    order.getId(), e.getMessage());
        }
    }
}