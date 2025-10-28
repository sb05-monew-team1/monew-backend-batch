package com.codeit.batch.article.fetcher;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.codeit.batch.article.config.OpenApiProperties;
import com.codeit.batch.article.dto.NewsResponse;
import com.codeit.batch.article.dto.OpenApiFetchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OpenApiArticleFetcher {

	private static final Logger log = LoggerFactory.getLogger(OpenApiArticleFetcher.class);

	private final OpenApiProperties props;
	private final ObjectMapper om;
	private final RestClient restClient;

	public OpenApiArticleFetcher(OpenApiProperties props, ObjectMapper om, RestClient.Builder builder) {
		this.props = props;
		this.om = om;
		this.restClient = builder
			.defaultHeaders(headers -> {
				headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
				if (StringUtils.hasText(props.apiKey())) {
					headers.set("X-Naver-Client-Id", props.apiKey());
				}
				if (StringUtils.hasText(props.apiSecret())) {
					headers.set("X-Naver-Client-Secret", props.apiSecret());
				}
			})
			.build();
	}

	public NewsResponse fetch(OpenApiFetchRequest request) {
		URI targetUri = buildUri(request);
		try {
			String rawBody = restClient
				.get()
				.uri(targetUri)
				.retrieve()
				.body(String.class);

			if (!StringUtils.hasText(rawBody)) {
				throw new ArticleFetchException("OpenAPI returned an empty body for uri: " + targetUri);
			}

			return om.readValue(rawBody, NewsResponse.class);
		} catch (RestClientException e) {
			throw new ArticleFetchException("Failed to call OpenAPI for uri: " + targetUri, e);
		} catch (JsonProcessingException e) {
			throw new ArticleFetchException("Failed to deserialize OpenAPI response", e);
		}
	}

	public NewsResponse fetchLatest(String keyword) {
		NewsResponse response = fetch(OpenApiFetchRequest.latest(keyword));
		int itemCount = response.items() == null ? 0 : response.items().size();
		log.debug("Fetched {} articles for keyword '{}'", itemCount, keyword);
		return response;
	}

	private URI buildUri(OpenApiFetchRequest request) {
		if (!StringUtils.hasText(props.baseUrl())) {
			throw new IllegalStateException("OpenAPI base url is not configured");
		}

		return UriComponentsBuilder.fromUriString(props.baseUrl())
			.queryParam("query", request.query())
			.queryParam("display", request.display())
			.queryParam("start", request.start())
			.queryParam("sort", request.sort().paramValue())
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUri();
	}
}
