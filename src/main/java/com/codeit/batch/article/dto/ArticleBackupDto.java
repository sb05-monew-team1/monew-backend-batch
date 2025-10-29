package com.codeit.batch.article.dto;

import java.time.Instant;
import java.util.UUID;

import com.codeit.batch.article.domain.ArticleSource;

public record ArticleBackupDto(
	UUID id,
	ArticleSource source,
	String sourceUrl,
	String title,
	Instant publishDate,
	String summary,
	Long commentCount,
	Long viewCount,
	Instant collectedAt,
	Instant createdAt,
	Instant updatedAt,
	Instant deletedAt
) {
}
