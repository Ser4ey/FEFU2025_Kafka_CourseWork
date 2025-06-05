package org.example.newsreview.controller

import org.example.newsreview.model.NewsToReview
import org.example.newsreview.service.NewsService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.result.view.Rendering
import reactor.core.publisher.Mono

@Controller
class WebController(private val newsService: NewsService) {

    @GetMapping("/")
    fun index(model: Model): Mono<Rendering> {
        return newsService.getNextNewsReactive()
            .map { news ->
                Rendering.view("index")
                    .modelAttribute("news", news)
                    .modelAttribute("hasNews", true)
                    .build()
            }
            .onErrorResume { error ->
                Mono.just(
                    Rendering.view("index")
                        .modelAttribute("hasNews", false)
                        .modelAttribute("errorMessage", "Нет новостей для рецензирования")
                        .build()
                )
            }
    }

    @PostMapping("/review")
    fun reviewNews(
        @RequestParam newsId: String,
        @RequestParam isAccepted: Boolean,
        @RequestParam comment: String
    ): Mono<Rendering> {
        return newsService.reviewNewsReactive(newsId, isAccepted, comment)
            .then(Mono.just(Rendering.redirectTo("/").build()))
    }
}