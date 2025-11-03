package com.codeit.batch.article.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
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
public class InterestNotificationPublisher implements StepExecutionListener {

	private final InterestSubscriptionRepository interestSubscriptionRepository;
	private final NotificationRepository notificationRepository;
	private final InterestRepository interestRepository;
	private final ArticleInterestAggregationListener aggregationListener;

	private static final String RESOURCE_TYPE = "INTEREST";

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		Map<UUID, Integer> counts = new HashMap<>(aggregationListener.getInterestArticleCount());
		if (counts.isEmpty()) {
			return ExitStatus.COMPLETED;
		}

		for (UUID uuid : counts.keySet()) {
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

			int count = counts.get(uuid);

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

		aggregationListener.getInterestArticleCount().clear();

		return ExitStatus.COMPLETED;
	}
}
