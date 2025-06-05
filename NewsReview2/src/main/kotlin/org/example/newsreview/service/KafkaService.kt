package org.example.newsreview.service

import com.newsreview.model.NewsReviewed
import com.newsreview.model.NewsToPublic
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service

@Service
class KafkaService(
    private val producer: ReactiveKafkaProducerTemplate<String, Any>
) {
    suspend fun sendNewsReviewed(news: NewsReviewed) {
        producer.send("news-reviewed", news.newsId, news)
    }

    suspend fun sendNewsToPublic(news: NewsToPublic) {
        producer.send("news-to-public", news.newsId, news)
    }
}