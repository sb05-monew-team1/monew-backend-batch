package com.codeit.batch.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.batch.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationQueryRepository {
}
