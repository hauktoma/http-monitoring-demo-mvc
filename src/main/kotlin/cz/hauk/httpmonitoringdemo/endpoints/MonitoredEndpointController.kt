package cz.hauk.httpmonitoringdemo.endpoints

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * Defines REST API for manipulation with monitored HTTP endpoints.
 *
 * @author Tomas Hauk
 */
@RestController
@RequestMapping("/api/v1/monitoredEndpoint")
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

    @GetMapping("/id-{id}/result/list")
    fun getMonitoredEndpointResultList(
        @PathVariable("id", required = true) id: UUID,
        @Valid filter: MonitoredResultFilterInFDTO
    ): MonitoringResultListFDTO = endpointService.filterResults(id, filter)
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

data class MonitoredResultFilterInFDTO(
    @field:PositiveOrZero
    val page: Long = 0,
    @field:Min(1) @field:Max(100)
    val size: Long = 20
)

data class MonitoringResultFDTO(
    val id: UUID,
    val checkedAt: Instant,
    val statusCode: HttpStatus?,
    val contentType: MediaType?,
    val payload: String?,   // X|FIXME THa revise payload -- may be quite large -> truncate / separate endpoint with proper media type / multipart
    val monitoredEndpointId: UUID,
    val url: URL,
    val error: String?,
)

data class MonitoringResultListFDTO(
    val items: List<MonitoringResultFDTO>,
    val totalCount: Long,
    val resultCount: Int,
    val requestedPage: Long,
    val requestedPageSize: Long
)
