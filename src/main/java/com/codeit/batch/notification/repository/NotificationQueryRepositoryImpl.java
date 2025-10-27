package com.codeit.batch.notification.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.batch.notification.domain.Notification;
import com.codeit.batch.notification.domain.QNotification;
import com.codeit.batch.user.domain.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

	private final JPAQueryFactory queryFactory;
	private static final QNotification n = QNotification.notification;
	private static final QUser u = QUser.user;

	@Autowired
	private EntityManager em;

	@Override
	@Transactional
	public long deleteNotifications(Instant now) {
		Instant stoargePeriod = now.minusSeconds(7 * 24 * 3600);

		long count = queryFactory
			.delete(n)
			.where(
				n.confirmed.eq(true)
					.and(n.updatedAt.lt(stoargePeriod))
			)
			.execute();
		em.clear();

		return count;
	}

	@Override
	public List<Notification> checkDelNotifi(Instant now) {
		Instant stoargePeriod = now.minusSeconds(7 * 24 * 3600);

		return queryFactory
			.select(n)
			.from(n)
			.where(n.confirmed.eq(true)
				.and(n.updatedAt.lt(stoargePeriod)))
			.fetch();
	}
}
