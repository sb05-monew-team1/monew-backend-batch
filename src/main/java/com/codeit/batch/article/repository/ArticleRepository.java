package com.codeit.batch.article.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.batch.article.domain.Article;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
	@EntityGraph(attributePaths = "interests")
	Optional<Article> findBySourceUrl(String sourceUrl);
}
