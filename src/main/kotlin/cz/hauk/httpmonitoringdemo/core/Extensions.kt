package cz.hauk.httpmonitoringdemo.core

import arrow.core.Option
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

fun <T> Mono<T>.toMonoOption(): Mono<Option<T>> = this.map {
    Option.just(it)
}.switchIfEmpty {
    Mono.just(Option.empty())
}
