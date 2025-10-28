package com.codeit.batch.article.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monew.article-ingestion.schedule")
public record ScheduleProperties(
	String cron
) {

}
