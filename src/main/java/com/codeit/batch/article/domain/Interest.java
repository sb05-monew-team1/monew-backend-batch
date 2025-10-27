package com.codeit.batch.article.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(name = "interests")
@AllArgsConstructor
@NoArgsConstructor
public class Interest extends BaseUpdatableDomain {

	@Column(name = "name")
	private String name;

	@Column(name = "subscriber_count")
	private Long subscriberCount;

	@Builder.Default
	@OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InterestKeyword> keywords = new ArrayList<>();
}
