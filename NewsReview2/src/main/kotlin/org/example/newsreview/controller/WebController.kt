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
    fun index(model: Model, @RequestParam(required = false) error: String?): Mono<String> {
        // Add error message if present
        error?.let { 
            when (it) {
                "review_failed" -> model.addAttribute("errorMessage", "Произошла ошибка при обработке новости. Попробуйте еще раз.")
                "invalid_news_id" -> model.addAttribute("errorMessage", "Неверный идентификатор новости.")
                "invalid_acceptance_status" -> model.addAttribute("errorMessage", "Не указан статус принятия новости.")
                "invalid_comment" -> model.addAttribute("errorMessage", "Комментарий не может быть пустым.")
                else -> model.addAttribute("errorMessage", "Произошла неизвестная ошибка. Попробуйте еще раз.")
            }
            model.addAttribute("hasError", true)
        }
        
        return newsService.getNextNewsReactive()
            .map { news ->
                model.addAttribute("news", news)
                model.addAttribute("hasNews", true)
                "index"
            }
            .switchIfEmpty(
                Mono.fromCallable {
                    model.addAttribute("hasNews", false)
                    if (!model.containsAttribute("errorMessage")) {
                        model.addAttribute("errorMessage", "Нет новостей для рецензирования")
                    }
                    "index"
                }
            )
            .onErrorResume { error ->
                Mono.fromCallable {
                    model.addAttribute("hasNews", false)
                    model.addAttribute("errorMessage", "Ошибка при загрузке новостей: ${error.message}")
                    model.addAttribute("hasError", true)
                    "index"
                }
            }
    }

    @PostMapping("/review")
    fun reviewNews(
        @RequestParam newsId: String?,
        @RequestParam isAccepted: Boolean?,
        @RequestParam comment: String?
    ): Mono<String> {
        // Validate parameters
        if (newsId.isNullOrBlank()) {
            println("Error: newsId is null or blank")
            return Mono.just("redirect:/?error=invalid_news_id")
        }
        
        if (isAccepted == null) {
            println("Error: isAccepted is null")
            return Mono.just("redirect:/?error=invalid_acceptance_status")
        }
        
        if (comment.isNullOrBlank()) {
            println("Error: comment is null or blank")
            return Mono.just("redirect:/?error=invalid_comment")
        }
        
        println("Processing review for newsId: $newsId, isAccepted: $isAccepted, comment: $comment")
        
        return newsService.reviewNewsReactive(newsId, isAccepted, comment)
            .then(Mono.just("redirect:/"))
            .onErrorResume { error ->
                println("Error in reviewNews: ${error.message}")
                error.printStackTrace()
                Mono.just("redirect:/?error=review_failed")
            }
    }
}