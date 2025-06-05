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
    fun getNextNews(): Mono<NewsToReview> {
        return newsService.getNextNewsReactive()
    }

    @PostMapping("/review")
    fun reviewNews(
        @RequestParam newsId: String,
        @RequestParam isAccepted: Boolean,
        @RequestParam comment: String
    ): Mono<Void> {
        return newsService.reviewNewsReactive(newsId, isAccepted, comment)
    }
}