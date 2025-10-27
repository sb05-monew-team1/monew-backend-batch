package com.codeit.batch.article.domain;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseUpdatableDomain extends BaseDomain {

	@LastModifiedDate
	@Setter
	private Instant updatedAt;
}
