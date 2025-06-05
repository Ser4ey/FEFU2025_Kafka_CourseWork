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
        println("Sending news reviewed: ${news.newsId} - accepted: ${news.isAccepted}")
        return producer.send("news-reviewed", news.newsId, news)
            .doOnSuccess { 
                println("Successfully sent news reviewed: ${news.newsId}")
            }
            .doOnError { error ->
                println("Error sending news reviewed: ${error.message}")
                error.printStackTrace()
            }
            .then()
    }

    fun sendNewsToPublic(news: NewsToPublic): Mono<Void> {
        println("Sending news to public: ${news.newsId}")
        return producer.send("news-to-public", news.newsId, news)
            .doOnSuccess { 
                println("Successfully sent news to public: ${news.newsId}")
            }
            .doOnError { error ->
                println("Error sending news to public: ${error.message}")
                error.printStackTrace()
            }
            .then()
    }
}