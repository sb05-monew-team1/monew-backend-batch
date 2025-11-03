package com.codeit.batch.article.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.batch.article.domain.InterestSubscription;

public interface InterestSubscriptionRepository extends JpaRepository<InterestSubscription, UUID> {
	List<InterestSubscription> findByInterest_Id(UUID interestId);
}
