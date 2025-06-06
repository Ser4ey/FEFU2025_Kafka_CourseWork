package org.example.newsreview.model

data class NewsReviewed(
    val newsId: String,
    val authorTelegramId: Long,
    val newsTitle: String,
    val isAccepted: Boolean,
    val redactorComment: String
)
