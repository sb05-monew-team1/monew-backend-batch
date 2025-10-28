package com.codeit.batch.article.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleInterest {

	@EmbeddedId
	private ArticleInterestId id = new ArticleInterestId();

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("articleId")
	@JoinColumn(name = "article_id", nullable = false)
	private Article article;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("interestId")
	@JoinColumn(name = "interest_id", nullable = false)
	private Interest interest;

	public ArticleInterest(Article article, Interest interest) {
		this.article = article;
		this.interest = interest;
	}
}

