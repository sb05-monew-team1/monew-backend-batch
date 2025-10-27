package com.codeit.batch.article.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinTable(
		name = "article_interests",
		joinColumns = @JoinColumn(name = "article_id"),
		inverseJoinColumns = @JoinColumn(name = "interest_id")
	)
	private List<Interest> interests = new ArrayList<>();
}
