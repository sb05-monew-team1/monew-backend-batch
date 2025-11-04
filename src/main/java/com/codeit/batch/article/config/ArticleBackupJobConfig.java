package com.codeit.batch.article.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.processor.ArticleBackupProcessor;
import com.codeit.batch.article.reader.ArticleBackupReader;
import com.codeit.batch.article.writer.ArticleBackupWriter;
import com.codeit.batch.common.metrics.BatchJobMetricsListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ArticleBackupJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final BatchJobMetricsListener batchJobMetricsListener;

	@Bean
	public Job articleBackupJob(
		Step articleBackupStep
	) {
		return new JobBuilder("articleBackupJob", jobRepository)
			.listener(batchJobMetricsListener)
			.start(articleBackupStep)
			.build();
	}

	@Bean
	public Step articleBackupStep(
		ArticleBackupReader reader,
		ArticleBackupProcessor processor,
		ArticleBackupWriter writer
	) {
		return new StepBuilder("articleBackupStep", jobRepository)
			.<Article, String>chunk(200, transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.build();

	}
}
