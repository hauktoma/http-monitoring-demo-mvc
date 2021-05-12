package cz.hauk.httpmonitoringdemo.core

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class ApplicationConfiguration {

    // X|FIXME THa review if it has sane timeouts by default
    @Bean
    fun webClient(webClientBuilder: WebClient.Builder): WebClient = webClientBuilder
        .codecs { conf -> conf.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
        .build()
}

@ConditionalOnProperty(
    value = ["app.scheduling.enabled"], havingValue = "true", matchIfMissing = true
)
@Configuration
@EnableScheduling
class SchedulingConfiguration
