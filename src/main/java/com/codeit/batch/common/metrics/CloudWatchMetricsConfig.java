package com.codeit.batch.common.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;

@Configuration
public class CloudWatchMetricsConfig {

	@Bean
	@ConditionalOnMissingBean
	public AwsCredentialsProvider cloudWatchAwsCredentialsProvider() {
		return DefaultCredentialsProvider.create();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "management.metrics.export.cloudwatch", name = "enabled", havingValue = "true")
	public CloudWatchAsyncClient cloudWatchAsyncClient(
		AwsCredentialsProvider awsCredentialsProvider,
		@Value("${management.metrics.export.cloudwatch.region:}") String cloudWatchRegion
	) {
		CloudWatchAsyncClientBuilder builder = CloudWatchAsyncClient.builder()
			.credentialsProvider(awsCredentialsProvider);

		if (StringUtils.hasText(cloudWatchRegion)) {
			builder.region(Region.of(cloudWatchRegion));
		}

		return builder.build();
	}
}
