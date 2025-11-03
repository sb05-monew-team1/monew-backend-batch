package com.codeit.batch.article.config;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.dto.ArticleCandidate;
import com.codeit.batch.article.listener.ArticleInterestAggregationListener;
import com.codeit.batch.article.listener.InterestNotificationPublisher;
import com.codeit.batch.article.processor.ArticleProcessor;
import com.codeit.batch.article.reader.OpenApiArticleReader;
import com.codeit.batch.article.reader.RssArticleReader;
import com.codeit.batch.article.writer.ArticleWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ArticleIngestionJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job articleIngestionJob(
		Step openApiArticleIngestionStep,
		Step rssArticleIngestionStep,
		InterestNotificationPublisher interestNotificationPublisher
	) {
		return new JobBuilder("articleIngestionJob", jobRepository)
			.listener(interestNotificationPublisher)
			.start(openApiArticleIngestionStep)
			.next(rssArticleIngestionStep)
			.build();
	}

	@Bean
	public Step openApiArticleIngestionStep(
		OpenApiArticleReader openApiArticleReader,
		ArticleProcessor articleProcessor,
		ArticleWriter articleWriter,
		ArticleInterestAggregationListener articleInterestAggregationListener
	) {
		return new StepBuilder("openApiArticleIngestionStep", jobRepository)
			.<ArticleCandidate, Article>chunk(50, transactionManager)
			.reader(openApiArticleReader)
			.processor(articleProcessor)
			.writer(articleWriter)
			.listener((ItemWriteListener<? super Article>)articleInterestAggregationListener)
			.listener((StepExecutionListener)articleInterestAggregationListener)
			.build();
	}

	@Bean
	public Step rssArticleIngestionStep(
		RssArticleReader rssArticleReader,
		ArticleProcessor articleProcessor,
		ArticleWriter articleWriter,
		ArticleInterestAggregationListener articleInterestAggregationListener
	) {
		return new StepBuilder("rssArticleIngestionStep", jobRepository)
			.<ArticleCandidate, Article>chunk(50, transactionManager)
			.reader(rssArticleReader)
			.processor(articleProcessor)
			.writer(articleWriter)
			.listener((ItemWriteListener<? super Article>)articleInterestAggregationListener)
			.listener((StepExecutionListener)articleInterestAggregationListener)
			.build();
	}
}
