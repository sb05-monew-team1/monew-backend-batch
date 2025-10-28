package com.codeit.batch.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.codeit.batch.notification.domain.Notification;
import com.codeit.batch.notification.repository.NotificationRepository;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationRepositoryTest {

	@Autowired
	private NotificationRepository notificationRepository;

	@Test
	@DisplayName("7일이 지난 확인된 알림 삭제")
	void deleteNotificationTest() {
		List<Notification> beforeDelete = notificationRepository.findAll();
		System.out.println("삭제 전 알림 리스트: " + beforeDelete);

		Instant now = Instant.now();
		notificationRepository.deleteNotifications(now);

		List<Notification> afterDelete = notificationRepository.findAll();
		System.out.println("삭제 후 알림 리스트: " + afterDelete);

		assertTrue(notificationRepository.checkDelNotifi(now).isEmpty());
	}
}
