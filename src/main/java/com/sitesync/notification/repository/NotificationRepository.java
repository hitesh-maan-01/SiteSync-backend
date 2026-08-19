package com.sitesync.notification.repository;

import com.sitesync.notification.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<AppNotification, UUID> {

    List<AppNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("UPDATE AppNotification n SET n.isRead = true " +
           "WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") UUID id, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE AppNotification n SET n.isRead = true WHERE n.userId = :userId")
    int markAllAsRead(@Param("userId") UUID userId);

    long countByUserIdAndIsReadFalse(UUID userId);
}