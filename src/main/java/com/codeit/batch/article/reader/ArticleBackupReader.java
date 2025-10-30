package com.codeit.batch.article.reader;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.domain.QArticle;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Querydsl 기반으로 기사 데이터를 범위 단위로 스트리밍하게 읽어 오는 리더.
 * 하루치 백업 같이 범위가 큰 작업에서도 메모리를 과도하게 사용하지 않는다.
 */
@Slf4j
@Component
@StepScope
public class ArticleBackupReader implements ItemStreamReader<Article> {

	private static final String OFFSET_KEY = "articleBackupReader.offset";
	private static final int DEFAULT_PAGE_SIZE = 200;

	private final JPAQueryFactory queryFactory;
	private final Instant from;
	private final Instant to;
	private final int pageSize;

	private Iterator<Article> currentBatch = Collections.emptyIterator();
	private long offset = 0L;
	private boolean exhausted;

	public ArticleBackupReader(
		JPAQueryFactory queryFactory,
		@Value("#{jobParameters['from']}") String fromParam,
		@Value("#{jobParameters['to']}") String toParam,
		@Value("#{jobParameters['pageSize']}") String pageSizeParam
	) {
		this.queryFactory = queryFactory;
		this.from = parseInstant(fromParam, null);
		this.to = parseInstant(toParam, null);
		this.pageSize = resolvePageSize(pageSizeParam);
		log.debug("ArticleBackupReader initialized from={} to={} pageSize={}", from, to, this.pageSize);
	}

	@Override
	public Article read() {
		if (exhausted) {
			return null;
		}

		if (!currentBatch.hasNext()) {
			List<Article> articles = fetchNextChunk();
			if (articles.isEmpty()) {
				exhausted = true;
				return null;
			}
			currentBatch = articles.iterator();
		}

		return currentBatch.next();
	}

	@Override
	public void open(@NonNull ExecutionContext executionContext) {
		offset = executionContext.getLong(OFFSET_KEY, offset);
	}

	@Override
	public void update(@NonNull ExecutionContext executionContext) {
		executionContext.putLong(OFFSET_KEY, offset);
	}

	@Override
	public void close() {
		currentBatch = Collections.emptyIterator();
	}

	private List<Article> fetchNextChunk() {
		QArticle article = QArticle.article;
		List<Article> items = queryFactory
			.selectFrom(article)
			.where(buildDatePredicate(article))
			.orderBy(article.collectedAt.asc(), article.id.asc())
			.offset(offset)
			.limit(pageSize)
			.fetch();

		offset += items.size();
		return items;
	}

	private BooleanExpression buildDatePredicate(QArticle article) {
		BooleanExpression predicate = null;
		if (from != null) {
			predicate = article.publishDate.goe(from);
		}
		if (to != null) {
			BooleanExpression toPredicate = article.publishDate.loe(to);
			predicate = predicate == null ? toPredicate : predicate.and(toPredicate);
		}
		return predicate;
	}

	private int resolvePageSize(String pageSizeParam) {
		if (pageSizeParam == null || pageSizeParam.isBlank()) {
			return DEFAULT_PAGE_SIZE;
		}
		try {
			int pageSize = Integer.parseInt(pageSizeParam.trim());
			if (pageSize <= 0) {
				return DEFAULT_PAGE_SIZE;
			}
			return pageSize;
		} catch (NumberFormatException ex) {
			return DEFAULT_PAGE_SIZE;
		}
	}

	private Instant parseInstant(String raw, Instant defaultValue) {
		if (!StringUtils.hasText(raw)) {
			return defaultValue;
		}
		try {
			return Instant.parse(raw.trim());
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException(
				String.format(Locale.ROOT, "Invalid instant value for backup job parameter: %s", raw), ex);
		}
	}
}
