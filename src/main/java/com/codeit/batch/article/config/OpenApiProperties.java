package com.codeit.batch.article.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monew.article-ingestion.openapi")
public record OpenApiProperties(
	String baseUrl,
	String apiKey,
	String apiSecret
) {

}
