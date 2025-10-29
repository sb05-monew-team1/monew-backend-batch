package com.codeit.batch.article.processor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class ArticleProcessor implements ItemProcessor<ArticleCandidate, Article> {

	private final ArticleRepository articleRepository;
	private final InterestRepository interestRepository;

	private final Map<String, Article> cachedArticles = new HashMap<>();
	private List<Interest> interests;

	@Override
	public Article process(@NonNull ArticleCandidate candidate) {
		if (interests == null) {
			interests = interestRepository.findAll();
		}

		String sourceUrl = candidate.link();
		if (!StringUtils.hasText(sourceUrl)) {
			log.debug("Skip article without a usable source url");
			return null;
		}

		Article cached = cachedArticles.get(sourceUrl);
		if (cached == null) {
			Article article = articleRepository.findBySourceUrl(sourceUrl)
				.orElseGet(() -> buildArticle(candidate, sourceUrl));

			if (article == null) {
				return null;
			}
			cachedArticles.put(sourceUrl, article);
			cached = article;
		}
		boolean added = false;

		for (Interest interest : interests) {
			if (
				interest.getKeywords() == null && !StringUtils.hasText(interest.getName())
			) {
				continue;
			}
			if (containsAllKeywords(cached, interest)) {
				added |= attachInterest(cached, interest);
			}
		}
		return added ? cached : null;
	}

	private boolean containsAllKeywords(Article article, Interest interest) {
		String title = safeString(article.getTitle());
		String description = safeString(article.getSummary());
		String combined = (title + " " + description).toLowerCase(Locale.ROOT);

		List<String> keywords = interest.getKeywords().stream()
			.map(InterestKeyword::getKeyword)
			.filter(StringUtils::hasText)
			.map(k -> k.trim().toLowerCase(Locale.ROOT))
			.collect(Collectors.toCollection(ArrayList::new));

		if (StringUtils.hasText(interest.getName())) {
			Arrays.stream(interest.getName().split("\\s+"))
				.map(token -> token.trim().toLowerCase(Locale.ROOT))
				.filter(token -> !token.isEmpty())
				.forEach(keywords::add);
		}

		if (keywords.isEmpty()) {
			return false;
		}

		return keywords.stream().allMatch(combined::contains);
	}

	private Article buildArticle(ArticleCandidate candidate, String sourceUrl) {
		Instant publishDate = candidate.publishedAt();
		if (publishDate == null) {
			return null;
		}

		return Article.builder()
			.source(candidate.source())
			.sourceUrl(sourceUrl)
			.title(safeString(candidate.title()))
			.summary(safeString(candidate.summary()))
			.publishDate(publishDate)
			.build();
	}

	private String safeString(String value) {
		if (value == null) {
			return "";
		}
		return Jsoup.clean(value, Safelist.none());
	}

	private boolean attachInterest(Article article, Interest interest) {
		if (interest == null || interest.getId() == null) {
			log.warn("Skip linking interest because candidate has no valid interest information");
			return false;
		}

		Interest managedInterest = interestRepository.getReferenceById(interest.getId());

		return article.addInterestIfAbsent(managedInterest);
	}
}
