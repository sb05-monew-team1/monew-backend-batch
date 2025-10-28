package com.codeit.batch.article.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsResponse(
	@JsonProperty("lastBuildDate") String lastBuildDate,
	@JsonProperty("total") int total,
	@JsonProperty("start") int start,
	@JsonProperty("display") int display,
	@JsonProperty("items") List<NewsItem> items
) {

}

