package com.codeit.batch.article.scheduler;

import java.time.Instant;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.codeit.batch.article.config.ScheduleProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class ArticleIngestionScheduler {

	private final JobLauncher jobLauncher;
	private final Job articleIngestionJob;
	private final ScheduleProperties scheduleProperties;

	@Scheduled(initialDelay = 5000, fixedDelay = 60000)
	public void runArticleIngestionJob() {
		try {
			JobParameters params = new JobParametersBuilder()
				.addString("runAt", Instant.now().toString())
				.toJobParameters();

			JobExecution execution = jobLauncher.run(articleIngestionJob, params);
			log.info("[Scheduler] systemStatsJob started: id={}, status={}",
				execution.getJobId(), execution.getStatus());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}
}
