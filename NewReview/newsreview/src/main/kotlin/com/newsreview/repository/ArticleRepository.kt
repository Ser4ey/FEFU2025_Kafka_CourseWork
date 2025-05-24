package com.newsreview.repository

import com.newsreview.model.Article
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : ReactiveCrudRepository<Article, Long> {
    suspend fun findByNewsId(newsId: String): Article?
} 