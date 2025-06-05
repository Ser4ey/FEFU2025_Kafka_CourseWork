package org.example.newsreview.controller

import org.example.newsreview.model.NewsToReview
import org.example.newsreview.service.NewsService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/news")
class NewsController(
    private val newsService: NewsService
) {
    @GetMapping("/next")
    suspend fun getNextNews(): Mono<NewsToReview?> {
        return Mono.justOrEmpty(newsService.getNextNews())
    }

    @PostMapping("/review")
    suspend fun reviewNews(
        @RequestParam newsId: String,
        @RequestParam isAccepted: Boolean,
        @RequestParam comment: String
    ) {
        val news = newsService.getNextNews()
        if (news != null && news.newsId == newsId) {
            newsService.reviewNews(news, isAccepted, comment)
        }
    }
}