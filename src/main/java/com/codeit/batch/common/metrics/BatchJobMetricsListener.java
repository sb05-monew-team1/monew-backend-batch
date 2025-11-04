package com.codeit.batch.common.metrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class BatchJobMetricsListener implements JobExecutionListener {

	private final MeterRegistry meterRegistry;
	private final ConcurrentMap<Long, Timer.Sample> jobSamples = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AtomicInteger> activeJobGauges = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AtomicLong> lastDurationGauges = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AtomicLong> lastCompletionGauges = new ConcurrentHashMap<>();

	public BatchJobMetricsListener(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		jobSamples.put(jobExecution.getId(), Timer.start(meterRegistry));
		activeJobGauge(jobExecution.getJobInstance().getJobName()).incrementAndGet();
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		String jobName = jobExecution.getJobInstance().getJobName();
		BatchStatus status = jobExecution.getStatus();

		activeJobGauge(jobName).updateAndGet(value -> value > 0 ? value - 1 : 0);

		Timer timer = Timer.builder("batch.job.duration")
			.description("Batch job execution duration")
			.tags("job", jobName, "status", status.name())
			.register(meterRegistry);
		Timer.Sample sample = jobSamples.remove(jobExecution.getId());
		if (sample != null) {
			sample.stop(timer);
		}

		long durationMillis = calculateDuration(jobExecution);
		lastDurationGauge(jobName).set(durationMillis);
		lastCompletionGauge(jobName).set(completedAt(jobExecution));

		Counter.builder("batch.job.execution.count")
			.description("Number of batch job executions grouped by status")
			.tags("job", jobName, "status", status.name())
			.register(meterRegistry)
			.increment();
	}

	private long calculateDuration(JobExecution jobExecution) {
		LocalDateTime startTime = jobExecution.getStartTime();
		LocalDateTime endTime = jobExecution.getEndTime();
		if (startTime == null || endTime == null) {
			return 0L;
		}

		return Duration.between(startTime, endTime).toMillis();
	}

	private long completedAt(JobExecution jobExecution) {
		LocalDateTime endTime = jobExecution.getEndTime();
		if (endTime == null) {
			return 0L;
		}

		return endTime
			.atZone(ZoneId.systemDefault())
			.toInstant()
			.toEpochMilli();
	}

	private AtomicInteger activeJobGauge(String jobName) {
		return activeJobGauges.computeIfAbsent(jobName, name -> {
			AtomicInteger holder = new AtomicInteger(0);
			Gauge.builder("batch.job.active.count", holder, AtomicInteger::doubleValue)
				.description("Number of running batch job executions")
				.tags("job", name)
				.register(meterRegistry);
			return holder;
		});
	}

	private AtomicLong lastDurationGauge(String jobName) {
		return lastDurationGauges.computeIfAbsent(jobName, name -> {
			AtomicLong holder = new AtomicLong(0);
			Gauge.builder("batch.job.last.duration", holder, AtomicLong::doubleValue)
				.description("Duration in milliseconds of the last finished batch job execution")
				.tags("job", name)
				.register(meterRegistry);
			return holder;
		});
	}

	private AtomicLong lastCompletionGauge(String jobName) {
		return lastCompletionGauges.computeIfAbsent(jobName, name -> {
			AtomicLong holder = new AtomicLong(0);
			Gauge.builder("batch.job.last.completed.epoch", holder, AtomicLong::doubleValue)
				.description("Epoch milliseconds when the batch job last finished")
				.tags("job", name)
				.register(meterRegistry);
			return holder;
		});
	}
}
