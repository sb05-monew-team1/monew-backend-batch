package com.codeit.batch.article.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monew.article-ingestion.rss")
public record RssProperties(
	List<FeedProperty> feeds
) {
	public record FeedProperty(String name, String url) {
	}
}
