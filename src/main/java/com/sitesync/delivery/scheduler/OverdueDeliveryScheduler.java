package com.sitesync.delivery.scheduler;

import com.sitesync.notification.entity.NotificationSeverity;
import com.sitesync.notification.service.PushNotificationService;
import com.sitesync.delivery.entity.DeliverySchedule;
import com.sitesync.project.entity.Project;
import com.sitesync.project.entity.ProjectSupervisor;
import com.sitesync.delivery.repository.DeliveryScheduleRepository;
import com.sitesync.project.repository.ProjectRepository;
import com.sitesync.project.repository.ProjectSupervisorRepository;
import com.sitesync.auth.repository.UserRepository; // adjust package if different
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class OverdueDeliveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueDeliveryScheduler.class);

    private final DeliveryScheduleRepository deliveryRepo;
    private final ProjectRepository projectRepo;
    private final ProjectSupervisorRepository supervisorRepo;
    private final UserRepository userRepo;
    private final PushNotificationService pushService;

    public OverdueDeliveryScheduler(
            DeliveryScheduleRepository deliveryRepo,
            ProjectRepository projectRepo,
            ProjectSupervisorRepository supervisorRepo,
            UserRepository userRepo,
            PushNotificationService pushService) {
        this.deliveryRepo   = deliveryRepo;
        this.projectRepo    = projectRepo;
        this.supervisorRepo = supervisorRepo;
        this.userRepo       = userRepo;
        this.pushService    = pushService;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    public void checkOverdueDeliveries() {
        log.info("[OverdueDeliveryScheduler] Starting...");

        LocalDate today = LocalDate.now();

        List<DeliverySchedule> overdueList =
                deliveryRepo.findByStatusAndExpectedDateBefore(
                        DeliverySchedule.Status.PENDING, today);

        if (overdueList.isEmpty()) {
            log.info("[OverdueDeliveryScheduler] No overdue deliveries.");
            return;
        }

        log.info("[OverdueDeliveryScheduler] {} overdue deliveries found.", overdueList.size());

        for (DeliverySchedule delivery : overdueList) {
            try {
                // 1. Get project
                Project project = projectRepo.findById(delivery.getProjectId())
                        .orElse(null);
                if (project == null) {
                    log.warn("Project not found for delivery={}", delivery.getId());
                    continue;
                }

                // 2. Get all supervisors for this project (M:N via EmbeddedId)
                List<ProjectSupervisor> assignments =
                        supervisorRepo.findByIdProjectId(project.getId());

                if (assignments.isEmpty()) {
                    log.warn("No supervisors assigned to project={}", project.getId());
                    continue;
                }

                String title = "⚠️ Overdue Delivery";
                String body  = String.format(
                        "Delivery of '%s' for project '%s' was due on %s and is still pending.",
                        delivery.getMaterialName(),
                        project.getName(),
                        delivery.getExpectedDate()
                );

                // 3. Notify each supervisor
                for (ProjectSupervisor assignment : assignments) {
                    UUID supervisorId = assignment.getUserId();
                    try {
                        // Fetch FCM token from user record
                        userRepo.findById(supervisorId).ifPresent(user -> {
                            String fcmToken = user.getFcmToken(); // adjust getter if different
                            if (fcmToken == null || fcmToken.isBlank()) {
                                log.debug("No FCM token for supervisor={}", supervisorId);
                                return;
                            }
                            pushService.sendAndPersist(
                                    fcmToken,
                                    supervisorId,
                                    title,
                                    body,
                                    NotificationSeverity.WARNING,
                                    "OVERDUE_DELIVERY",
                                    "View Deliveries",
                                    "/projects/" + project.getId() + "/deliveries",
                                    Map.of(
                                        "projectId",  project.getId().toString(),
                                        "deliveryId", delivery.getId().toString()
                                    )
                            );
                            log.info("Notified supervisor={} for delivery={}",
                                    supervisorId, delivery.getId());
                        });
                    } catch (Exception e) {
                        log.error("Failed to notify supervisor={}: {}", supervisorId, e.getMessage());
                    }
                }

                // 4. Mark OVERDUE so scheduler doesn't re-fire tomorrow
                delivery.setStatus(DeliverySchedule.Status.OVERDUE);
                deliveryRepo.save(delivery);

            } catch (Exception e) {
                log.error("Error processing delivery={}: {}", delivery.getId(), e.getMessage());
            }
        }

        log.info("[OverdueDeliveryScheduler] Done.");
    }
}