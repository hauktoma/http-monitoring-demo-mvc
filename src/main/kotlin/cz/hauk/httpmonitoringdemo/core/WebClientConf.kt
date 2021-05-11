package cz.hauk.httpmonitoringdemo.core

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConf {

    // X|FIXME THa review if it has sane timeouts by default
    @Bean
    fun webClient(webClientBuilder: WebClient.Builder): WebClient = webClientBuilder
        .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
        .build()
}