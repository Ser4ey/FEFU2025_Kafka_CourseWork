package com.newsreview.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("articles")
data class Article(
    @Id
    val id: Long? = null,
    val newsId: String,
    val title: String,
    val content: String,
    val authorName: String
) 