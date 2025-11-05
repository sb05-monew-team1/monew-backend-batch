package com.codeit.batch.common.metrics;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("management.metrics.export.cloudwatch")
public class CloudWatchMetricsProperties {

	private boolean enabled = true;

	private String namespace = "monew/batch";

	private Duration step = Duration.ofMinutes(1);

	private Integer batchSize = 20;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Duration getStep() {
		return step;
	}

	public void setStep(Duration step) {
		this.step = step;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}
}

