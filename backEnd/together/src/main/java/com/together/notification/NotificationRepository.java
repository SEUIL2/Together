package com.together.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByReceiverIdAndIsReadFalse(Long userId);
    List<NotificationEntity> findByReceiverIdOrderByCreatedAtDesc(Long userId);
}
