package com.codeit.batch.article.fetcher;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.codeit.batch.article.config.RssProperties;
import com.codeit.batch.article.dto.RssFeedItem;
import com.codeit.batch.article.dto.RssFeedResponse;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

@Component
public class RssArticleFetcher {

	private static final Logger log = LoggerFactory.getLogger(RssArticleFetcher.class);

	private final RestClient restClient;

	public RssArticleFetcher(RestClient.Builder builder) {
		this.restClient = builder
			.defaultHeaders(headers -> headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE))
			.build();
	}

	public RssFeedResponse fetch(RssProperties.FeedProperty feedProperty) {
		if (feedProperty == null || !StringUtils.hasText(feedProperty.url())) {
			throw new IllegalArgumentException("RSS feed URL must not be blank");
		}

		String xmlBody = retrieve(feedProperty.url());
		if (!StringUtils.hasText(xmlBody)) {
			throw new ArticleFetchException("RSS feed returned empty body: " + feedProperty.url());
		}

		try (StringReader reader = new StringReader(xmlBody)) {
			SyndFeed syndFeed = new SyndFeedInput().build(reader);

			List<RssFeedItem> items = syndFeed.getEntries().stream()
				.filter(Objects::nonNull)
				.map(entry -> {
					Instant publishedAt = entry.getPublishedDate() != null
						? entry.getPublishedDate().toInstant()
						: null;

					String description = Optional.ofNullable(entry.getDescription())
						.map(SyndContent::getValue)
						.map(this::clean)
						.map(String::trim)
						.orElse("");

					if (!StringUtils.hasText(description) && entry.getContents() != null) {
						for (SyndContent content : entry.getContents()) {
							if (content == null || !StringUtils.hasText(content.getValue())) {
								continue;
							}
							String cleaned = clean(content.getValue()).trim();
							if (StringUtils.hasText(cleaned)) {
								description = cleaned;
								break;
							}
						}
					}


					if (StringUtils.hasText(description)) {
						description = description.trim();
					}

					return new RssFeedItem(
						entry.getTitle(),
						StringUtils.hasText(entry.getLink()) ? entry.getLink() : entry.getUri(),
						description,
						publishedAt
					);
				})
				.toList();

			return new RssFeedResponse(
				feedProperty.name(),
				syndFeed.getTitle(),
				items
			);
		} catch (FeedException e) {
			throw new ArticleFetchException("Failed to parse RSS feed: " + feedProperty.url(), e);
		}
	}

	private String retrieve(String url) {
		try {
			return restClient
				.get()
				.uri(url)
				.accept(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML)
				.retrieve()
				.body(String.class);
		} catch (RestClientException ex) {
			log.warn("Failed to fetch RSS feed from {}", url, ex);
			throw new ArticleFetchException("Failed to retrieve RSS feed: " + url, ex);
		}
	}

	private String clean(String value) {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		return Jsoup.clean(value, "", Safelist.none(), new Document.OutputSettings().charset(StandardCharsets.UTF_8));
	}
}
