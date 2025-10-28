package com.codeit.batch.article.reader;

import java.util.Collections;
import java.util.Iterator;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.domain.ArticleSource;
import com.codeit.batch.article.domain.Interest;
import com.codeit.batch.article.dto.ArticleCandidate;
import com.codeit.batch.article.dto.NewsItem;
import com.codeit.batch.article.dto.NewsResponse;
import com.codeit.batch.article.dto.OpenApiFetchRequest;
import com.codeit.batch.article.fetcher.OpenApiArticleFetcher;
import com.codeit.batch.article.repository.InterestRepository;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class OpenApiArticleReader implements ItemReader<ArticleCandidate> {
	private final InterestRepository interestRepository;
	private final OpenApiArticleFetcher fetcher;

	private Iterator<Interest> interestIterator;
	private Iterator<NewsItem> currentItems = Collections.emptyIterator();
	private Interest currentInterest;

	@Override
	public ArticleCandidate read() {
		initIteratorIfNeeded();

		while (!currentItems.hasNext() && interestIterator.hasNext()) {
			currentInterest = interestIterator.next();
			if (currentInterest.getKeywords().isEmpty()) {
				continue;
			}
			String query = buildQuery(currentInterest);
			NewsResponse response = fetcher
				.fetch(new OpenApiFetchRequest(query, 50, 1, OpenApiFetchRequest.Sort.SIM));
			currentItems = response.items() == null
				? Collections.emptyIterator()
				: response.items().iterator();
		}
		if (currentItems.hasNext()) {
			return new ArticleCandidate(currentItems.next(), currentInterest, ArticleSource.NAVER);
		}
		return null;
	}

	private String buildQuery(Interest interest) {
		return interest.getKeywords().stream()
			.map(k -> k.getKeyword().trim())
			.filter(s -> !s.isBlank())
			.reduce((a, b) -> a + " " + b)
			.orElseThrow(() -> new IllegalArgumentException(
				"interest " + interest.getId() + " has no keywords"));
	}

	private void initIteratorIfNeeded() {
		if (interestIterator == null) {
			interestIterator = interestRepository.findAll().iterator();
		}
	}
}
