package com.codeit.batch.common.metrics;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;

@Configuration
@EnableConfigurationProperties(CloudWatchMetricsProperties.class)
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

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "management.metrics.export.cloudwatch", name = "enabled", havingValue = "true")
	public CloudWatchConfig cloudWatchConfig(CloudWatchMetricsProperties properties) {
		return new PropertiesBackedCloudWatchConfig(properties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "management.metrics.export.cloudwatch", name = "enabled", havingValue = "true")
	public CloudWatchMeterRegistry cloudWatchMeterRegistry(
		CloudWatchConfig cloudWatchConfig,
		CloudWatchAsyncClient cloudWatchAsyncClient,
		Clock clock
	) {
		return new CloudWatchMeterRegistry(cloudWatchConfig, clock, cloudWatchAsyncClient);
	}

	private static final class PropertiesBackedCloudWatchConfig implements CloudWatchConfig {

		private final CloudWatchMetricsProperties properties;

		private PropertiesBackedCloudWatchConfig(CloudWatchMetricsProperties properties) {
			this.properties = properties;
		}

		@Override
		public boolean enabled() {
			return properties.isEnabled();
		}

		@Override
		public String namespace() {
			return properties.getNamespace();
		}

		@Override
		public Duration step() {
			return properties.getStep() != null ? properties.getStep() : CloudWatchConfig.super.step();
		}

		@Override
		public int batchSize() {
			return properties.getBatchSize() != null ? properties.getBatchSize() : CloudWatchConfig.super.batchSize();
		}

		@Override
		public String get(String key) {
			return null;
		}
	}
}
