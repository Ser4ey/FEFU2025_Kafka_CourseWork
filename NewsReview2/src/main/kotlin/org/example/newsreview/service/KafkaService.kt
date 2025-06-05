package org.example.newsreview.service

import org.example.newsreview.model.NewsReviewed
import org.example.newsreview.model.NewsToPublic
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaService(
    private val producer: ReactiveKafkaProducerTemplate<String, Any>
) {
    fun sendNewsReviewed(news: NewsReviewed): Mono<Void> {
        return producer.send("news-reviewed", news.newsId, news)
            .then()
    }

    fun sendNewsToPublic(news: NewsToPublic): Mono<Void> {
        return producer.send("news-to-public", news.newsId, news)
            .then()
    }
}