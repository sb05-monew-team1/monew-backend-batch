package com.codeit.batch.article.fetcher;

/**
 * Signals failures when retrieving article data from external providers.
 */
public class ArticleFetchException extends RuntimeException {

	public ArticleFetchException(String message) {
		super(message);
	}

	public ArticleFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}

