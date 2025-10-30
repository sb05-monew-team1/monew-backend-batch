package com.codeit.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.codeit.batch.article.config.AwsProperties;
import com.codeit.batch.article.config.OpenApiProperties;
import com.codeit.batch.article.config.RssProperties;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableConfigurationProperties({
	OpenApiProperties.class,
	RssProperties.class,
	AwsProperties.class})
public class MonewBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonewBatchApplication.class, args);
	}

}
