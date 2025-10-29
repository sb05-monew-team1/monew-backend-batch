package com.codeit.batch.article.scheduler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class ArticleScheduler {

	private final JobLauncher jobLauncher;
	private final Job articleIngestionJob;
	private final Job articleBackupJob;

	@Scheduled(cron = "${monew.article-ingestion.schedule.cron}", zone = "Asia/Seoul")
	public void runArticleIngestionJob() {
		try {
			JobParameters params = new JobParametersBuilder()
				.addString("runAt", Instant.now().toString())
				.toJobParameters();

			JobExecution execution = jobLauncher.run(articleIngestionJob, params);
			log.info("[Scheduler] 기사 수집 배치 실행: id={}, status={}",
				execution.getJobId(), execution.getStatus());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Scheduled(cron = "${monew.article-backup.schedule.cron}", zone = "Asia/Seoul")
	public void runArticleBackupJob() {
		ZoneId zone = ZoneId.of("Asia/Seoul");
		LocalDate targetDate = LocalDate.now(zone).minusDays(1);

		Instant from = targetDate.atStartOfDay(zone).toInstant();
		Instant to = targetDate.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant();

		try {
			JobParameters params = new JobParametersBuilder()
				.addString("from", from.toString())
				.addString("to", to.toString())
				.addString("backupDate", targetDate.toString())
				.addString("pageSize", "200")
				.addLong("runId", System.currentTimeMillis())
				.toJobParameters();
			JobExecution execution = jobLauncher.run(articleBackupJob, params);
			log.info("[Scheduler] 기사 백업 배치 실행 id={}, status={}",
				execution.getJobId(), execution.getStatus());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
