package com.codeit.batch.notification.repository;

import java.time.Instant;
import java.util.List;

import com.codeit.batch.notification.domain.Notification;

public interface NotificationQueryRepository {

	long deleteNotifications(Instant now);

	List<Notification> checkDelNotifi(Instant now);
}
