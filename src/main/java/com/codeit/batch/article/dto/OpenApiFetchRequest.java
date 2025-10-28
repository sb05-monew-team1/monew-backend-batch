package com.codeit.batch.article.dto;

import org.springframework.util.StringUtils;

/**
 * Encapsulates the query parameters that can be sent to the Naver OpenAPI.
 */
public record OpenApiFetchRequest(
	String query,
	Integer display,
	Integer start,
	Sort sort
) {

	private static final int DEFAULT_DISPLAY = 10;
	private static final int DEFAULT_START = 1;

	public OpenApiFetchRequest {
		if (!StringUtils.hasText(query)) {
			throw new IllegalArgumentException("query must not be blank");
		}
		if (display < 10 || display > 100) {
			throw new IllegalArgumentException("display must be between 10 and 100");
		}
		if (start < 1 || start > 1000) {
			throw new IllegalArgumentException("start must be between 1 and 1000");
		}
		sort = sort == null ? Sort.SIM : sort;
	}

	/**
	 * Creates a request that uses the default pagination (display=50, start=1) and similarity sorting.
	 */
	public static OpenApiFetchRequest latest(String query) {
		return new OpenApiFetchRequest(query, DEFAULT_DISPLAY, DEFAULT_START, Sort.SIM);
	}

	public enum Sort {
		SIM("sim"),
		DATE("date");

		private final String paramValue;

		Sort(String paramValue) {
			this.paramValue = paramValue;
		}

		public String paramValue() {
			return paramValue;
		}
	}
}

