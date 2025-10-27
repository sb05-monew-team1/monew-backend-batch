package com.codeit.batch.notification.tasklet;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.codeit.batch.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NotificationDeleteTasklet implements Tasklet, StepExecutionListener {

	private final NotificationService notificationService;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		log.info("[NotificationDeleteTasklet] beforeStep");
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		long deleteCount = notificationService.deleteNotification();
		log.info("[NotificationDeleteTasklet] Notification - deleteCount={}", deleteCount);
		return RepeatStatus.FINISHED;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("[NotificationDeleteTasklet] afterStep");
		return ExitStatus.COMPLETED;
	}
}
