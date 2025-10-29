package com.codeit.batch.article.reader;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.codeit.batch.article.domain.ArticleSource;
import com.codeit.batch.article.domain.Interest;
import com.codeit.batch.article.domain.InterestKeyword;
import com.codeit.batch.article.dto.ArticleCandidate;
import com.codeit.batch.article.dto.NewsItem;
import com.codeit.batch.article.dto.NewsResponse;
import com.codeit.batch.article.dto.OpenApiFetchRequest;
import com.codeit.batch.article.fetcher.OpenApiArticleFetcher;
import com.codeit.batch.article.repository.InterestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class OpenApiArticleReader implements ItemReader<ArticleCandidate> {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

	private final InterestRepository interestRepository;
	private final OpenApiArticleFetcher fetcher;

	private Iterator<Interest> interestIterator;
	private Iterator<NewsItem> currentItems = Collections.emptyIterator();

	@Override
	public ArticleCandidate read() {
		initIteratorIfNeeded();

		while (!currentItems.hasNext() && interestIterator.hasNext()) {
			Interest currentInterest = interestIterator.next();
			String query = buildQuery(currentInterest);
			if (!StringUtils.hasText(query)) {
				continue;
			}
			NewsResponse response = fetcher
				.fetch(new OpenApiFetchRequest(query, 50, 1, OpenApiFetchRequest.Sort.DATE));
			currentItems = response.items() == null
				? Collections.emptyIterator()
				: response.items().iterator();
		}
		if (currentItems.hasNext()) {
			NewsItem item = currentItems.next();
			return ArticleCandidate.builder()
				.title(item.title())
				.link(resolveSourceUrl(item))
				.summary(item.description())
				.publishedAt(resolvePublishDate(item))
				.source(ArticleSource.NAVER)
				.build();
		}
		return null;
	}

	private String buildQuery(Interest interest) {
		String query = interest.getKeywords().stream()
			.map(InterestKeyword::getKeyword)
			.filter(StringUtils::hasText)
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.joining(" "));

		if (!StringUtils.hasText(query)) {
			log.warn("Skip interest {} because no usable keywords were found", interest.getId());
			return null;
		}
		return query;
	}

	private void initIteratorIfNeeded() {
		if (interestIterator == null) {
			interestIterator = interestRepository.findAll().iterator();
		}
	}

	private String resolveSourceUrl(NewsItem item) {
		String originalLink = item.originalLink();
		if (StringUtils.hasText(originalLink)) {
			return originalLink;
		}
		return item.link();
	}

	private Instant resolvePublishDate(NewsItem item) {
		String raw = item.publishedAt();
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		try {
			return ZonedDateTime.parse(raw, DATE_FORMATTER).toInstant();
		} catch (DateTimeParseException ex) {
			log.warn("Failed to parse publish date '{}'", raw, ex);
			return null;
		}
	}
}
