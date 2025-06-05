package org.example.newsreview.config

import com.newsreview.model.NewsToReview
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions
import java.util.*

@Configuration
class KafkaConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun reactiveKafkaConsumerTemplate(): ReactiveKafkaConsumerTemplate<String, NewsToReview> {
        val props = HashMap<String, Any>()
        props["bootstrap.servers"] = bootstrapServers
        props["key.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
        props["value.deserializer"] = "org.springframework.kafka.support.serializer.JsonDeserializer"
        props["spring.json.trusted.packages"] = "org.example.newsreview.model"
        props["group.id"] = "news-review-group"
        props["auto.offset.reset"] = "earliest"

        return ReactiveKafkaConsumerTemplate(
            ReceiverOptions.create<String, NewsToReview>(props)
                .subscription(listOf("news-to-review"))
        )
    }

    @Bean
    fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, Any> {
        val props = HashMap<String, Any>()
        props["bootstrap.servers"] = bootstrapServers
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.springframework.kafka.support.serializer.JsonSerializer"

        return ReactiveKafkaProducerTemplate(
            SenderOptions.create<String, Any>(props)
        )
    }
}