package org.example.newsreview.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("articles")
data class Article(
    @Id
    val id: Long? = null,
    val newsId: String,
    val title: String,
    val content: String,
    val authorName: String,
    val publishTime: LocalDateTime
)