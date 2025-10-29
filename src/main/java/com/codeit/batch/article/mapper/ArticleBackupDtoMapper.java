package com.codeit.batch.article.mapper;

import org.mapstruct.Mapper;

import com.codeit.batch.article.domain.Article;
import com.codeit.batch.article.dto.ArticleBackupDto;

@Mapper(componentModel = "spring")
public interface ArticleBackupDtoMapper {

	ArticleBackupDto toDto(Article article);
}
