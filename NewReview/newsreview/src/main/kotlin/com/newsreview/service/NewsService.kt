package com.newsreview.service

import com.newsreview.model.Article
import com.newsreview.model.NewsReviewed
import com.newsreview.model.NewsToPublic
import com.newsreview.model.NewsToReview
import com.newsreview.repository.ArticleRepository
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class NewsService(
    private val consumer: ReactiveKafkaConsumerTemplate<String, NewsToReview>,
    private val kafkaService: KafkaService,
    private val articleRepository: ArticleRepository
) {
    suspend fun getNextNews(): NewsToReview? {
        return consumer.receiveAutoAck()
            .map { it.value() }
            .next()
            .block()
    }

    suspend fun reviewNews(news: NewsToReview, isAccepted: Boolean, comment: String) {
        val reviewed = NewsReviewed(
            newsId = news.newsId,
            authorTelegramId = news.authorTelegramId,
            newsTitle = news.newsTitle,
            isAccepted = isAccepted,
            redactorComment = comment
        )
        
        kafkaService.sendNewsReviewed(reviewed)
        
        if (isAccepted) {
            val toPublic = NewsToPublic(
                newsId = news.newsId,
                authorName = news.authorName,
                newsTitle = news.newsTitle,
                newsContent = news.newsContent
            )
            kafkaService.sendNewsToPublic(toPublic)
            
            val article = Article(
                newsId = news.newsId,
                title = news.newsTitle,
                content = news.newsContent,
                authorName = news.authorName,
                publishTime = LocalDateTime.now()
            )
            articleRepository.save(article)
        }
    }
} 