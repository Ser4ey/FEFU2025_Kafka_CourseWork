package org.example.newsreview.service

import kotlinx.coroutines.reactor.mono
import org.example.newsreview.model.Article
import org.example.newsreview.model.NewsReviewed
import org.example.newsreview.model.NewsToPublic
import org.example.newsreview.model.NewsToReview
import org.example.newsreview.repository.ArticleRepository
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class NewsService(
    private val consumer: ReactiveKafkaConsumerTemplate<String, NewsToReview>,
    private val kafkaService: KafkaService,
    private val articleRepository: ArticleRepository
) {
    private val pendingNews = ConcurrentHashMap<String, NewsToReview>()
    private val newsBuffer = Sinks.many().multicast().onBackpressureBuffer<NewsToReview>()

    init {
        // Start consuming messages and buffer them
        consumer.receiveAutoAck()
            .map { it.value() }
            .doOnNext { news ->
                pendingNews[news.newsId] = news
                newsBuffer.tryEmitNext(news)
            }
            .subscribe()
    }

    fun getNextNewsReactive(): Mono<NewsToReview> {
        return newsBuffer.asFlux()
            .next()
            .switchIfEmpty(Mono.empty())
    }

    fun reviewNewsReactive(newsId: String, isAccepted: Boolean, comment: String): Mono<Void> {
        val news = pendingNews.remove(newsId)

        return if (news != null) {
            val reviewed = NewsReviewed(
                newsId = news.newsId,
                authorTelegramId = news.authorTelegramId,
                newsTitle = news.newsTitle,
                isAccepted = isAccepted,
                redactorComment = comment
            )

            var chain = kafkaService.sendNewsReviewed(reviewed)

            if (isAccepted) {
                val newsToPublic = NewsToPublic(
                    newsId = news.newsId,
                    authorName = news.authorName,
                    newsTitle = news.newsTitle,
                    newsContent = news.newsContent
                )

                val article = Article(
                    newsId = news.newsId,
                    title = news.newsTitle,
                    content = news.newsContent,
                    authorName = news.authorTelegramId.toString(),
                    publishTime = LocalDateTime.now()
                )

                chain = chain.then(kafkaService.sendNewsToPublic(newsToPublic))
                    .then(mono { articleRepository.save(article) }.then())
            }

            chain
        } else {
            Mono.empty()
        }
    }

    // Blocking versions for backward compatibility if needed
    suspend fun getNextNews(): NewsToReview? {
        return getNextNewsReactive().block()
    }

    suspend fun reviewNews(news: NewsToReview, isAccepted: Boolean, comment: String) {
        reviewNewsReactive(news.newsId, isAccepted, comment).block()
    }
}