package com.codeit.batch.article.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsItem(
	@JsonProperty("title") String title,
	@JsonProperty("originallink") String originalLink,
	@JsonProperty("link") String link,
	@JsonProperty("description") String description,
	@JsonProperty("pubDate") String publishedAt
) {
}
