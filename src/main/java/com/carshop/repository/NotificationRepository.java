package com.carshop.repository;

import com.carshop.entity.Notification;
import com.carshop.entity.NotificationStatus;
import com.carshop.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);

    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    Page<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'FAILED'")
    Long countFailedNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'PENDING'")
    Long countPendingNotifications();

    @Query("SELECT n FROM Notification n WHERE n.relatedEntityType = :relatedEntityType AND n.relatedEntityId = :relatedEntityId ORDER BY n.createdAt DESC")
    List<Notification> findByRelatedEntity(
            @Param("relatedEntityType") String relatedEntityType,
            @Param("relatedEntityId") Long relatedEntityId);

        boolean existsByTypeAndRelatedEntityTypeAndRelatedEntityIdAndCreatedAtBetween(
            NotificationType type,
            String relatedEntityType,
            Long relatedEntityId,
            LocalDateTime start,
            LocalDateTime end);
}
