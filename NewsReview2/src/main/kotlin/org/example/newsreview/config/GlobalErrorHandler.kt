package org.example.newsreview.config

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorHandler : DefaultErrorAttributes() {
    
    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): MutableMap<String, Any> {
        val errorAttributes = super.getErrorAttributes(request, options)
        
        val error = getError(request)
        
        when (error) {
            is IllegalArgumentException -> {
                errorAttributes["status"] = 400
                errorAttributes["error"] = "Bad Request"
                errorAttributes["message"] = error.message ?: "Invalid request parameters"
            }
            is RuntimeException -> {
                errorAttributes["status"] = 500
                errorAttributes["error"] = "Internal Server Error"
                errorAttributes["message"] = "An unexpected error occurred"
            }
        }
        
        return errorAttributes
    }
}