package com.codeit.batch.article.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.domain.ArticleInterest;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 각 청크에서 새로 연결된 관심사 ID 를 누적해 JobExecutionContext 로 전달하는 리스너.
 */
@Slf4j
@Component
@StepScope
public class ArticleInterestAggregationListener implements ItemWriteListener<Article>, StepExecutionListener {

	public static final String CONTEXT_KEY = "interestArticleCount";

	@Getter
	private final Map<UUID, Integer> interestArticleCount = new HashMap<>();

	@BeforeStep
	@Override
	public void beforeStep(@NonNull StepExecution stepExecution) {
		interestArticleCount.clear();
	}

	@Override
	public void beforeWrite(Chunk<? extends Article> items) {
		for (Article article : items) {
			if (article == null || article.getArticleInterests() == null) {
				continue;
			}

			article.getArticleInterests().stream()
				.filter(link -> link.getId() == null || link.getId().getInterestId() == null)
				.forEach(this::accumulate);
		}
	}

	private void accumulate(ArticleInterest link) {
		UUID interestId = link.getInterest() != null ? link.getInterest().getId() : null;
		if (interestId == null) {
			log.debug("Skip articleInterest without interest id");
			return;
		}
		interestArticleCount.merge(interestId, 1, Integer::sum);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (!interestArticleCount.isEmpty()) {
			@SuppressWarnings("unchecked")
			Map<UUID, Integer> jobCounts = (Map<UUID, Integer>)stepExecution.getJobExecution()
				.getExecutionContext()
				.get(CONTEXT_KEY);
			if (jobCounts == null) {
				jobCounts = new HashMap<>();
			}

			for (Map.Entry<UUID, Integer> entry : interestArticleCount.entrySet()) {
				jobCounts.merge(entry.getKey(), entry.getValue(), Integer::sum);
			}

			stepExecution.getJobExecution().getExecutionContext().put(CONTEXT_KEY, jobCounts);
			interestArticleCount.clear();
		}
		return stepExecution.getExitStatus();
	}

	@Override
	public void afterWrite(Chunk<? extends Article> items) {
		// no-op
	}

	@Override
	public void onWriteError(Exception exception, Chunk<? extends Article> items) {
		// no-op
	}
}
