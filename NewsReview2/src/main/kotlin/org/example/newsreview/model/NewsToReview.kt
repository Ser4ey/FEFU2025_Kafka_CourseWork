package org.example.newsreview.model

data class NewsToReview(
    val newsId: String,
    val authorTelegramId: Long,
    val authorName: String,
    val newsTitle: String,
    val newsContent: String
)


