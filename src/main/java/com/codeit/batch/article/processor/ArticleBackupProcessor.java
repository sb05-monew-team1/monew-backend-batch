package com.codeit.batch.article.processor;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.dto.ArticleBackupDto;
import com.codeit.batch.article.mapper.ArticleBackupDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleBackupProcessor implements ItemProcessor<Article, String> {

	private final ArticleBackupDtoMapper mapper;
	private final ObjectMapper objectMapper;
	private ObjectWriter jsonWriter;

	@PostConstruct
	void initWriter() {
		this.jsonWriter = objectMapper.writer();
	}

	@Override
	public String process(Article item) throws Exception {
		ArticleBackupDto dto = mapper.toDto(item);
		return jsonWriter.writeValueAsString(dto);
	}
}
