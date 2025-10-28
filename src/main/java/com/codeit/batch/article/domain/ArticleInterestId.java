package com.codeit.batch.article.domain;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArticleInterestId implements Serializable {

	@Column(name = "article_id", nullable = false)
	private UUID articleId;

	@Column(name = "interest_id", nullable = false)
	private UUID interestId;
}

