package com.codeit.batch.notification.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.codeit.batch.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public long deleteNotification() {
		Instant now = Instant.now();
		return notificationRepository.deleteNotifications(now);
	}
}
