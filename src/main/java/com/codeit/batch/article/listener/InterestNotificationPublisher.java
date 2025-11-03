package com.codeit.batch.article.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.domain.Interest;
import com.codeit.batch.article.domain.InterestSubscription;
import com.codeit.batch.article.repository.InterestRepository;
import com.codeit.batch.article.repository.InterestSubscriptionRepository;
import com.codeit.batch.notification.domain.Notification;
import com.codeit.batch.notification.repository.NotificationRepository;
import com.codeit.batch.user.domain.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InterestNotificationPublisher implements JobExecutionListener {

	private final InterestSubscriptionRepository interestSubscriptionRepository;
	private final NotificationRepository notificationRepository;
	private final InterestRepository interestRepository;

	private static final String RESOURCE_TYPE = "INTEREST";

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// no-op
	}

	@Override
	@SuppressWarnings("unchecked")
	public void afterJob(JobExecution jobExecution) {
		ExecutionContext jobContext = jobExecution.getExecutionContext();
		Map<UUID, Integer> counts =
			(Map<UUID, Integer>)jobContext.remove(ArticleInterestAggregationListener.CONTEXT_KEY);
		if (counts == null || counts.isEmpty()) {
			return;
		}

		for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
			UUID uuid = entry.getKey();
			List<Notification> notifications = new ArrayList<>();
			Interest interest = interestRepository.findById(uuid).orElse(null);
			if (interest == null) {
				continue;
			}

			List<User> users = interestSubscriptionRepository.findByInterest_Id(uuid).stream()
				.map(InterestSubscription::getUser)
				.toList();

			if (users.isEmpty()) {
				continue;
			}

			int count = entry.getValue();
			if (count == 0) {
				continue;
			}

			String content = String.format("[%s]에 관련된 기사가 %d건 등록되었습니다.", interest.getName(), count);
			for (User user : users) {
				notifications.add(Notification.builder()
					.user(user)
					.confirmed(false)
					.content(content)
					.resourceType(RESOURCE_TYPE)
					.resourceId(uuid)
					.build());
			}
			notificationRepository.saveAll(notifications);
		}
	}
}
