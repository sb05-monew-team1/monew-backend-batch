package com.codeit.batch.article.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.batch.common.base.BaseUpdatableDomain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "articles")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
public class Article extends BaseUpdatableDomain {

	@Column(nullable = false, updatable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private ArticleSource source;

	@Column(nullable = false, unique = true, updatable = false, length = 500)
	private String sourceUrl;

	@Column(nullable = false, updatable = false, length = 500)
	private String title;

	@Column(nullable = false, updatable = false)
	private Instant publishDate;

	@Column(updatable = false, length = 500)
	private String summary;

	@Builder.Default
	@OneToMany(mappedBy = "article", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	private List<ArticleInterest> articleInterests = new ArrayList<>();

	public boolean addInterestIfAbsent(Interest interest) {
		if (interest == null) {
			return false;
		}
		boolean exists = articleInterests.stream()
			.anyMatch(link -> link.getInterest().getId().equals(interest.getId()));
		if (exists) {
			return false;
		}
		articleInterests.add(new ArticleInterest(this, interest));
		return true;
	}
}
