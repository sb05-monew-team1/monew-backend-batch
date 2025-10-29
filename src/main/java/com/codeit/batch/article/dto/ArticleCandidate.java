package com.codeit.batch.article.dto;

import java.time.Instant;

import com.codeit.batch.article.domain.ArticleSource;

import lombok.Builder;

@Builder
public record ArticleCandidate(
	String title,
	String link,
	String summary,
	Instant publishedAt,
	ArticleSource source
) {
}
