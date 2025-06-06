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
import reactor.kafka.receiver.ReceiverRecord
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class NewsService(
    private val consumer: ReactiveKafkaConsumerTemplate<String, NewsToReview>,
    private val kafkaService: KafkaService,
    private val articleRepository: ArticleRepository
) {
    private val pendingNews = ConcurrentHashMap<String, Pair<NewsToReview, ReceiverRecord<String, NewsToReview>>>()
    private val newsBuffer = Sinks.many().multicast().onBackpressureBuffer<NewsToReview>()
    private var currentNews: NewsToReview? = null

    init {
        println("Initializing NewsService...")
        consumer.receive()
            .doOnNext { record ->
                val news = record.value()
                println("Received news from Kafka: ${news.newsId} - ${news.newsTitle}")
                pendingNews[news.newsId] = Pair(news, record)
                
                if (currentNews == null) {
                    currentNews = news
                    newsBuffer.tryEmitNext(news)
                    println("Set current news to: ${news.newsId}")
                }
            }
            .doOnError { error ->
                println("Error in Kafka consumer: ${error.message}")
                error.printStackTrace()
            }
            .subscribe()
        println("NewsService initialized and Kafka consumer started")
    }

    fun getNextNewsReactive(forceRefresh: Boolean = false): Mono<NewsToReview> {
        println("Getting next news, current news: ${currentNews?.newsId}, pending news count: ${pendingNews.size}, forceRefresh: $forceRefresh")
        
        // Если запрошено принудительное обновление, сбрасываем текущую новость
        if (forceRefresh) {
            println("Force refresh requested, clearing current news")
            currentNews = null
        }

        currentNews?.let {
            println("Returning existing current news with ID: '${it.newsId}'")
            return Mono.just(it)
        }

        // Получаем следующую новость, если мы ожидаем новость
        if (pendingNews.isNotEmpty()) {
            val nextNewsId = pendingNews.keys.firstOrNull()
            if (nextNewsId != null) {
                val (news, _) = pendingNews[nextNewsId]!!
                currentNews = news
                println("Set current news from pending with ID: '${news.newsId}'")
                return Mono.just(news)
            }
        }

        return newsBuffer.asFlux()
            .next()
            .doOnNext { news ->
                currentNews = news
                println("Set current news from buffer with ID: '${news.newsId}'")
            }
            .switchIfEmpty(
                Mono.fromCallable {
                    println("No news available")
                    null
                }.then(Mono.empty())
            )
    }

    fun reviewNewsReactive(newsId: String, isAccepted: Boolean, comment: String): Mono<Void> {
        println("Starting review process for newsId: '$newsId', isAccepted: $isAccepted")
        println("Current pending news keys: ${pendingNews.keys.joinToString()}")
        
        // Проверяем, есть ли новость с таким ID напрямую
        var newsData = pendingNews.remove(newsId)
        
        // Если не нашли напрямую, пробуем найти по текущей новости
        if (newsData == null && currentNews?.newsId != null) {
            println("Direct match not found, checking if current news ID matches")
            if (currentNews?.newsId == newsId) {
                println("Current news ID matches, using it")
                newsData = pendingNews.remove(currentNews?.newsId)
            }
        }

        return if (newsData != null) {
            val (news, record) = newsData
            println("Found news data for newsId: '$newsId'")

            if (currentNews?.newsId == newsId) {
                currentNews = null
                getNextAvailableNews()
                println("Cleared current news and getting next available news")
            }

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
                    authorName = news.authorName,
                    publishTime = LocalDateTime.now()
                )

                chain = chain.then(kafkaService.sendNewsToPublic(newsToPublic))
                    .then(mono { 
                        println("Saving article to database: ${article.newsId}")
                        articleRepository.save(article)
                    }.doOnSuccess {
                        println("Successfully saved article to database: ${article.newsId}")
                    }.doOnError { error ->
                        println("Error saving article to database: ${error.message}")
                        error.printStackTrace()
                    }.then())
            }

            // Подтверждаем сообщение Acknowledge the message only after successful processing
            chain.doOnSuccess { 
                record.receiverOffset().acknowledge()
                println("Successfully acknowledged message: $newsId")
            }
            .doOnError { error ->
                println("Error processing news '$newsId': ${error.message}")
                error.printStackTrace()
            }
            .then()
        } else {
            println("Warning: News with ID '$newsId' not found in pending news")
            println("Available news IDs: ${pendingNews.keys.joinToString()}")
            Mono.empty()
        }
    }

    private fun getNextAvailableNews() {
        pendingNews.values.firstOrNull { (news, _) ->
            news.newsId != currentNews?.newsId 
        }?.let { (news, _) ->
            currentNews = news
            newsBuffer.tryEmitNext(news)
        }
    }

}
