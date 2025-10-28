package com.codeit.batch.article.dto;

import com.codeit.batch.article.domain.ArticleSource;
import com.codeit.batch.article.domain.Interest;

public record ArticleCandidate(
	NewsItem newsItem,
	Interest interest,
	ArticleSource source
) {
}
