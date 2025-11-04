package com.codeit.batch.notification.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.codeit.batch.common.metrics.BatchJobMetricsListener;
import com.codeit.batch.notification.service.NotificationService;
import com.codeit.batch.notification.tasklet.NotificationDeleteTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class NotificationDeleteConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final NotificationService notificationService;
	private final BatchJobMetricsListener batchJobMetricsListener;

	@Bean
	public Job notificationDeleteJob(Step notificationDeleteStep) {
		return new JobBuilder("notificationDeleteJob", jobRepository)
			.listener(batchJobMetricsListener)
			.start(notificationDeleteStep)
			.build();
	}

	@Bean
	public Step notificationDeleteStep(NotificationDeleteTasklet tasklet) {
		return new StepBuilder("notificationDeleteStep", jobRepository)
			.tasklet(tasklet, transactionManager)
			.listener(tasklet)
			.build();
	}

	@Bean
	public NotificationDeleteTasklet notificationDeleteTasklet() {
		return new NotificationDeleteTasklet(notificationService);
	}
}
