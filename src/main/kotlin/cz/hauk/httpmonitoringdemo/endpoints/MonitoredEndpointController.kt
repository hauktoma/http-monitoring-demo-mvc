package cz.hauk.httpmonitoringdemo.endpoints

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Defines REST API for manipulation with monitored HTTP endpoints.
 *
 * @author Tomas Hauk
 */
@RestController
@RequestMapping("/api/v1/monitoredEndpoints")
class MonitoredEndpointController(
    private val endpointService: MonitoredEndpointService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addMonitoredEndpoint(
        @Valid @RequestBody input: MonitoredEndpointInFDTO
    ): MonitoredEndpointOutFDTO = endpointService.createEndpoint(input).toFDTO()

    @PutMapping("/id-{id}")
    fun updateMonitoredEndpoint(
        @PathVariable("id", required = true) id: UUID,
        @Valid @RequestBody input: MonitoredEndpointInFDTO
    ): MonitoredEndpointOutFDTO = endpointService.updateEndpoint(id, input).toFDTO()

    @GetMapping("/id-{id}")
    fun getMonitoredEndpoint(
        @PathVariable("id", required = true) id: UUID
    ): MonitoredEndpointOutFDTO = endpointService.getEndpoint(id).toFDTO()

    @DeleteMapping("/id-{id}")
    fun removeMonitoredEndpoint(
        @PathVariable("id", required = true) id: UUID
    ): Unit = endpointService.deleteEndpoint(id)
}

data class MonitoredEndpointInFDTO(
    @field:NotBlank @field:Size(min = 3, max = 256)
    val name: String,
    @field:NotNull
    val url: URL,
    @field:NotNull
    val monitoredInterval: Duration
)

data class MonitoredEndpointOutFDTO(
    val id: UUID,
    val name: String,
    val url: URL,
    val createdAt: Instant,
    val monitoredInterval: Duration,
    val ownerUserId: UUID
)
