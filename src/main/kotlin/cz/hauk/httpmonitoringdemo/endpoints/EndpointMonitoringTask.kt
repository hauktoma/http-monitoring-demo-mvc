package cz.hauk.httpmonitoringdemo.endpoints

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import cz.hauk.httpmonitoringdemo.core.toMonoOption
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class EndpointMonitoringTask(
    private val endpointRepo: MonitoredEndpointRepository,
    private val webClient: WebClient,
    private val monitoringResultRepo: MonitoringResultRepository
) {

    // X|FIXME THa cap on the response body size, do not read whole?
    @Scheduled(fixedDelayString = "\${task.monitoring.delay.millis:1000}")
    fun monitorEndpoints() {
        LOG.info("Starting monitor endpoint task")

        Flux.generate(
            { 0 },
            { recordCount: Int, sink: SynchronousSink<MonitoredEndpointDBO> ->
                LOG.debug("Searching for [${recordCount}] record from DB to process.")
                findAndLockRecordToProcess().map {
                    LOG.debug("Found record [${recordCount}] for processing $it")
                    sink.next(it)
                }.getOrElse {
                    LOG.debug("No record for processing found in [${recordCount}].")
                    sink.complete()
                }
                recordCount + 1
            }
        ).subscribeOn(
            Schedulers.boundedElastic()
        ).flatMap(
            { record -> processRecord(record) }, 10
        ).blockLast()

        LOG.info("Monitor endpoint task finished successfully.")
    }

    private fun processRecord(
        record: MonitoredEndpointDBO
    ): Mono<Unit> = Mono.fromCallable {
        LOG.info("Will process rescheduled record $record")
    }.flatMap {
        val requestStart = Instant.now()
        webClient.get()
            .uri(record.url.toURI())
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java).toMonoOption().map { maybeStringBody ->
                    MonitoringResultDBO(
                        id = UUID.randomUUID(),
                        checkedAt = requestStart,
                        statusCode = response.statusCode(),
                        contentType = response.headers().contentType().orElse(null),
                        payload = maybeStringBody.orNull(),
                        monitoredEndpointId = record.id,
                        url = record.url,
                        error = null,
                        isNewEntity = true
                    )
                }
            }
            .timeout(Duration.ofSeconds(10))
            .onErrorResume { error ->
                LOG.warn("Failed to check record ${record.id}}.", error)
                MonitoringResultDBO(
                    id = UUID.randomUUID(),
                    checkedAt = requestStart,
                    statusCode = null,
                    contentType = null,
                    payload = null,
                    monitoredEndpointId = record.id,
                    url = record.url,
                    error = error.message,
                    isNewEntity = true
                ).toMono()
            }
            .map {
                monitoringResultRepo.save(it)
                endpointRepo.rescheduleRecordForMonitoring(
                    record.id,
                    Instant.now().plus(record.monitoredInterval)
                )
                Unit
            }
            .subscribeOn(Schedulers.boundedElastic())
    }

    @Transactional
    private fun findAndLockRecordToProcess(): Option<MonitoredEndpointDBO> = endpointRepo
        .findAndLockRecordForMonitoring(Instant.now())
        .toOption()
        .filter { record ->
            // check that exactly one record was rescheduled, if not, skip it, since it was probably deleted
            val rescheduleTo = Instant.now().plus(Duration.ofMinutes(1))
            LOG.debug("Will reschedule record ${record.id} from ${record.nextCheckAt} to ${rescheduleTo}.")
            endpointRepo.rescheduleRecordForMonitoring(record.id, rescheduleTo) == 1
        }
        .flatMap { endpointRepo.findByIdOrNull(it.id).toOption() }

    companion object {
        private val LOG = LoggerFactory.getLogger(EndpointMonitoringTask::class.java)
    }
}
