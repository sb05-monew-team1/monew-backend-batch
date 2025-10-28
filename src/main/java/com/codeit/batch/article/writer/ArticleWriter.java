package com.codeit.batch.article.writer;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.repository.ArticleRepository;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ArticleWriter implements ItemWriter<Article> {
	private final ArticleRepository articleRepository;

	@Override
	public void write(@NonNull Chunk<? extends Article> chunk) throws Exception {
		articleRepository.saveAll(chunk);
	}
}
