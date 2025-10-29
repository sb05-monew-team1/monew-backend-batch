package com.codeit.batch.article.dto;

import java.util.List;

public record RssFeedResponse(
	String feedName,
	String title,
	List<RssFeedItem> items
) {
}

