package cz.hauk.httpmonitoringdemo.endpoints

import cz.hauk.httpmonitoringdemo.core.AuthenticationProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class MonitoredEndpointService(
    private val monitoredEndpointRepo: MonitoredEndpointRepository,
    private val monitoringResultRepository: MonitoringResultRepository,
    private val authenticationProvider: AuthenticationProvider,

    @Value("\${endpoints.minCheckDuration:PT5S}")
    private val minMonitoredInterval: Duration
) {
    fun createEndpoint(
        input: MonitoredEndpointInFDTO
    ): MonitoredEndpointDBO = validateCreateEndpointPayload(
        input
    ).let {
        val now = Instant.now()

        MonitoredEndpointDBO(
            id = UUID.randomUUID(),
            name = input.name,
            url = input.url,
            createdAt = now,
            monitoredInterval = input.monitoredInterval,
            ownerUserId = getUserId(),
            nextCheckAt = now,
            isNewEntity = true
        )
    }.let { newDBO ->
        kotlin.runCatching { monitoredEndpointRepo.save(newDBO) }.getOrElse { determineAndThrowCreateError(input, it) }
    }

    private fun validateCreateEndpointPayload(
        input: MonitoredEndpointInFDTO
    ): Unit = when {
        input.monitoredInterval < minMonitoredInterval -> throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Monitored interval ${input.monitoredInterval} lower than allowed minimum $minMonitoredInterval."
        )
        else -> Unit
    }

    private fun <T> determineAndThrowCreateError(
        input: MonitoredEndpointInFDTO,
        error: Throwable
    ): T = when {
        monitoredEndpointRepo.findByNameAndUser(input.name, getUserId()) != null -> throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Given monitored endpoint name ${input.name} already used for user."
        )
        monitoredEndpointRepo.findByUrlAndUser(input.url, getUserId()) != null -> throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Given monitored endpoint url ${input.url} already used for user."
        )
        else -> {
            LOG.error("Unknown error when creating monitored endpoint. Input: $input", error)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error.", error)
        }
    }

    fun updateEndpoint(
        id: UUID, input: MonitoredEndpointInFDTO
    ): MonitoredEndpointDBO = monitoredEndpointRepo.updateForUser(
        id, getUserId(), input.name, input.url, input.monitoredInterval
    ).let { rowsUpdated ->
        when (rowsUpdated) {
            1 -> getEndpoint(id)
            else -> throwEndpointNotFound(id)
        }
    }

    fun getEndpoint(
        id: UUID
    ): MonitoredEndpointDBO = monitoredEndpointRepo.findByIdAndUser(id, getUserId()) ?: throwEndpointNotFound(id)

    /**
     * Deletes the configuration of the monitored endpoint. The records of the
     * monitoring itself will be deleted by the dedicated background task.
     *
     * @see MonitoredEndpointResultDeleteTask
     */
    fun deleteEndpoint(
        id: UUID
    ): Unit = monitoredEndpointRepo.deleteByIdAndUser(
        id = id,
        owner_user_id = getUserId()
    )

    private fun getUserId(): UUID = authenticationProvider.getAuthentication().principal

    private fun <T> throwEndpointNotFound(
        id: UUID
    ): T = throw ResponseStatusException(HttpStatus.NOT_FOUND, "No endpoint with id: $id.")

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(MonitoredEndpointService::class.java)
    }
}

