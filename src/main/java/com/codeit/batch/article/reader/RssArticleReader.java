package com.codeit.batch.article.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.config.RssProperties;
import com.codeit.batch.article.domain.ArticleSource;
import com.codeit.batch.article.dto.ArticleCandidate;
import com.codeit.batch.article.dto.RssFeedItem;
import com.codeit.batch.article.dto.RssFeedResponse;
import com.codeit.batch.article.fetcher.RssArticleFetcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class RssArticleReader implements ItemReader<ArticleCandidate> {
	private final RssArticleFetcher fetcher;
	private final RssProperties property;

	private ArticleSource currentSource;
	private Iterator<RssProperties.FeedProperty> properties;
	private Iterator<RssFeedItem> currentItems = Collections.emptyIterator();

	@Override
	public ArticleCandidate read() {
		initIteratorIfNeeded();
		if (properties == null) {
			return null;
		}
		RssProperties.FeedProperty feedProperty;

		if (!currentItems.hasNext()) {
			if (!properties.hasNext()) {
				properties = null;
				return null;
			}
			feedProperty = properties.next();
			currentSource = getSource(feedProperty);
			RssFeedResponse response = fetcher.fetch(feedProperty);
			currentItems = response.items() == null
				? Collections.emptyIterator()
				: response.items().iterator();
		}

		if (currentItems.hasNext()) {
			RssFeedItem item = currentItems.next();
			return ArticleCandidate.builder()
				.title(item.title())
				.link(item.link())
				.summary(item.summary())
				.publishedAt(item.publishedAt())
				.source(currentSource)
				.build();
		}

		return null;
	}

	private void initIteratorIfNeeded() {
		if (properties == null) {
			if (property.feeds() == null || property.feeds().isEmpty()) {
				log.warn("No feeds configured");
				return;
			}
			List<RssProperties.FeedProperty> feeds = new ArrayList<>(property.feeds());

			feeds.removeIf(feed -> feed == null || feed.name() == null || feed.name().trim().isBlank());

			properties = feeds.iterator();
		}
	}

	private ArticleSource getSource(RssProperties.FeedProperty feedProperty) {
		return switch (feedProperty.name().toUpperCase()) {
			case "HANKYUNG" -> ArticleSource.HANKYUNG;
			case "CHOSUN" -> ArticleSource.CHOSUN;
			default -> ArticleSource.YEONHAP;
		};
	}
}
