package cz.hauk.httpmonitoringdemo.endpoints

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Helper service for cleaning database.
 */
@Service
class CleanDBTestHelper(
    private val endpointRepo: MonitoredEndpointRepository,
    private val resultRepo: MonitoringResultRepository,
) {
    fun cleanDB() {
        Mono.zip(
            Mono.fromCallable { endpointRepo.deleteAll() },
            Mono.fromCallable { resultRepo.deleteAll() }
        ).subscribeOn(Schedulers.boundedElastic()).block()
    }
}
