package org.example.newsreview.service

import org.example.newsreview.model.Article
import org.example.newsreview.model.NewsReviewed
import org.example.newsreview.model.NewsToPublic
import org.example.newsreview.model.NewsToReview
import org.example.newsreview.repository.ArticleRepository
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
            val newsToPublic = NewsToPublic(
                newsId = news.newsId,
                authorTelegramId = news.authorTelegramId,
                newsTitle = news.newsTitle,
                newsContent = news.newsContent
            )
            kafkaService.sendNewsToPublic(newsToPublic)

            // Save to database
            val article = Article(
                newsId = news.newsId,
                title = news.newsTitle,
                content = news.newsContent,
                authorName = news.authorTelegramId.toString(),
                publishTime = LocalDateTime.now()
            )
            articleRepository.save(article)
        }
    }
}