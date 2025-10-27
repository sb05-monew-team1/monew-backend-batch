package com.codeit.batch.scheduler;

import java.time.Instant;

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
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class MonewBatchScheduler {

	private final JobLauncher jobLauncher;
	private final Job notificationDeleteJob;

	@Scheduled(cron = "0 0 1 * * *")
	public void runNotificationDeleteJob() {
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addString("runAt", Instant.now().toString())
				.toJobParameters();

			System.out.println("스케쥴러 동작 시작");

			JobExecution execution = jobLauncher.run(notificationDeleteJob, jobParameters);
			log.info("[Scheduler] notificationDeleteJob started: id={}, status={}",
				execution.getJobId(), execution.getStatus());
		} catch (Exception e) {
			log.error("[Scheduler] notificationDeleteJob failed", e);
		}
	}
}
