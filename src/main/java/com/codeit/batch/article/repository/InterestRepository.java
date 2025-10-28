package com.codeit.batch.article.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.batch.article.domain.Interest;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

	@EntityGraph(attributePaths = "keywords")
	List<Interest> findAll();
}
