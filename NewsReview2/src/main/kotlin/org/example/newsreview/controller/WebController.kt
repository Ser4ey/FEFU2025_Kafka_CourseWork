package org.example.newsreview.controller

import org.example.newsreview.model.NewsToReview
import org.example.newsreview.service.NewsService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@Controller
class WebController(private val newsService: NewsService) {

    @GetMapping("/")
    fun index(model: Model): Mono<String> {
        return newsService.getNextNewsReactive()
            .map { news ->
                model.addAttribute("news", news)
                model.addAttribute("hasNews", true)
                "index"
            }
            .switchIfEmpty(
                Mono.fromCallable {
                    model.addAttribute("hasNews", false)
                    model.addAttribute("errorMessage", "Нет новостей для рецензирования")
                    "index"
                }
            )
            .onErrorResume { error ->
                Mono.fromCallable {
                    model.addAttribute("hasNews", false)
                    model.addAttribute("errorMessage", "Ошибка при загрузке новостей: ${error.message}")
                    "index"
                }
            }
    }

    @PostMapping("/review")
    fun reviewNews(
        @RequestParam newsId: String,
        @RequestParam isAccepted: Boolean,
        @RequestParam comment: String
    ): Mono<String> {
        return newsService.reviewNewsReactive(newsId, isAccepted, comment)
            .then(Mono.just("redirect:/"))
    }
}