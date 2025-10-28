package com.codeit.batch.article.processor;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.domain.Interest;
import com.codeit.batch.article.domain.InterestKeyword;
import com.codeit.batch.article.dto.ArticleCandidate;
import com.codeit.batch.article.repository.ArticleRepository;
import com.codeit.batch.article.repository.InterestRepository;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleProcessor implements ItemProcessor<ArticleCandidate, Article> {

	private static final Logger log = LoggerFactory.getLogger(ArticleProcessor.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

	private final ArticleRepository articleRepository;
	private final InterestRepository interestRepository;

	private final Map<String, Article> cachedArticles = new HashMap<>();

	@Override
	public Article process(@NonNull ArticleCandidate candidate) {
		if (candidate.newsItem() == null || candidate.interest() == null) {
			return null;
		}

		if (!containsAllKeywords(candidate)) {
			return null;
		}

		String sourceUrl = resolveSourceUrl(candidate);
		if (!StringUtils.hasText(sourceUrl)) {
			log.debug("Skip article without a usable source url for interest {}", candidate.interest().getId());
			return null;
		}

		Article cached = cachedArticles.get(sourceUrl);
		if (cached == null) {
			Article article = articleRepository.findBySourceUrl(sourceUrl)
				.orElseGet(() -> buildArticle(candidate, sourceUrl));

			if (article == null) {
				return null;
			}

			boolean added = attachInterest(article, candidate);
			cachedArticles.put(sourceUrl, article);
			return added ? article : null;
		}

		boolean added = attachInterest(cached, candidate);
		return added ? cached : null;
	}

	private boolean containsAllKeywords(ArticleCandidate candidate) {
		String title = safeString(candidate.newsItem().title());
		String description = safeString(candidate.newsItem().description());
		String combined = (title + " " + description).toLowerCase(Locale.ROOT);

		return candidate.interest()
			.getKeywords()
			.stream()
			.map(InterestKeyword::getKeyword)
			.filter(StringUtils::hasText)
			.map(keyword -> keyword.trim().toLowerCase(Locale.ROOT))
			.allMatch(combined::contains);
	}

	private Article buildArticle(ArticleCandidate candidate, String sourceUrl) {
		Instant publishDate = resolvePublishDate(candidate);
		if (publishDate == null) {
			return null;
		}

		return Article.builder()
			.source(candidate.source())
			.sourceUrl(sourceUrl)
			.title(safeString(candidate.newsItem().title()))
			.summary(safeString(candidate.newsItem().description()))
			.publishDate(publishDate)
			.build();
	}

	private Instant resolvePublishDate(ArticleCandidate candidate) {
		String raw = candidate.newsItem().publishedAt();
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		try {
			return ZonedDateTime.parse(raw, DATE_FORMATTER).toInstant();
		} catch (DateTimeParseException ex) {
			log.warn("Failed to parse publish date '{}' for interest {}", raw, candidate.interest().getId(), ex);
			return null;
		}
	}

	private String resolveSourceUrl(ArticleCandidate candidate) {
		String originalLink = candidate.newsItem().originalLink();
		if (StringUtils.hasText(originalLink)) {
			return originalLink;
		}
		return candidate.newsItem().link();
	}

	private String safeString(String value) {
		return value == null ? "" : value;
	}

	private boolean attachInterest(Article article, ArticleCandidate candidate) {
		if (candidate.interest() == null || candidate.interest().getId() == null) {
			log.warn("Skip linking interest because candidate has no valid interest information");
			return false;
		}

		Interest managedInterest = interestRepository.getReferenceById(candidate.interest().getId());

		return article.addInterestIfAbsent(managedInterest);
	}
}
