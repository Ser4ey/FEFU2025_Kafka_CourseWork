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
                println("Adding news to model with ID: '${news.newsId}'")
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
        exchange: org.springframework.web.server.ServerWebExchange
    ): Mono<String> {
        return exchange.formData
            .flatMap { formData ->
                val newsId = formData.getFirst("newsId")
                val isAccepted = formData.getFirst("isAccepted")
                val comment = formData.getFirst("comment")
                
                println("DEBUG - All form data:")
                formData.forEach { (key, values) ->
                    println("$key: $values")
                }
                
                println("Request URI: ${exchange.request.uri}")
                println("Request method: ${exchange.request.method}")
                println("Request headers: ${exchange.request.headers}")
                
                println("Received review request with newsId: '$newsId', isAccepted: $isAccepted, comment: '$comment'")
                
                // Validate newsId
                if (newsId.isNullOrBlank()) {
                    println("Error: newsId is null or blank")
                    return@flatMap Mono.just("redirect:/?error=invalid_news_id")
                }

                // Validate and convert isAccepted
                val isAcceptedBool = when(isAccepted?.lowercase()) {
                    "true" -> true
                    "false" -> false
                    else -> {
                        println("Error: isAccepted is invalid: $isAccepted")
                        return@flatMap Mono.just("redirect:/?error=invalid_acceptance_status")
                    }
                }

                // Validate comment
                if (comment.isNullOrBlank()) {
                    println("Error: comment is null or blank")
                    return@flatMap Mono.just("redirect:/?error=invalid_comment")
                }

                newsService.reviewNewsReactive(newsId, isAcceptedBool, comment)
                    .then(Mono.just("redirect:/"))
                    .onErrorResume { error ->
                        println("Error in reviewNews: ${error.message}")
                        error.printStackTrace()
                        Mono.just("redirect:/?error=review_failed")
                    }
            }
    }
}