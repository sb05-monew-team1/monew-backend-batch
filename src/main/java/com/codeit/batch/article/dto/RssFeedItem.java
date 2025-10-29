package com.codeit.batch.article.dto;

import java.time.Instant;

public record RssFeedItem(
	String title,
	String link,
	String summary,
	Instant publishedAt
) {
}

