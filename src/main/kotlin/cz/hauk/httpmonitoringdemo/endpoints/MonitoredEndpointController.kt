package cz.hauk.httpmonitoringdemo.endpoints

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springdoc.api.annotations.ParameterObject
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
    @Operation(description = "Creates new endpoint to be monitored.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addMonitoredEndpoint(
        @Valid @RequestBody input: MonitoredEndpointInFDTO
    ): MonitoredEndpointOutFDTO = endpointService.createEndpoint(input).toFDTO()

    @Operation(description = "Updates endpoint to be monitored.")
    @PutMapping("/id-{id}")
    fun updateMonitoredEndpoint(
        @PathVariable("id", required = true) id: UUID,
        @Valid @RequestBody input: MonitoredEndpointInFDTO
    ): MonitoredEndpointOutFDTO = endpointService.updateEndpoint(id, input).toFDTO()

    @Operation(description = "Returns monitored endpoint configuration by id.")
    @GetMapping("/id-{id}")
    fun getMonitoredEndpoint(
        @PathVariable("id", required = true) id: UUID
    ): MonitoredEndpointOutFDTO = endpointService.getEndpoint(id).toFDTO()

    @Operation(description = "Deletes monitored endpoint.")
    @DeleteMapping("/id-{id}")
    fun removeMonitoredEndpoint(
        @PathVariable("id", required = true) id: UUID
    ): Unit = endpointService.deleteEndpoint(id)

    @Operation(description = "Allows to list the monitoring results for endpoint by id.")
    @GetMapping("/id-{id}/result/list")
    fun getMonitoredEndpointResultList(
        @PathVariable("id", required = true) id: UUID,
        @ParameterObject @Valid filter: MonitoredResultFilterInFDTO
    ): MonitoringResultListFDTO = endpointService.filterResults(id, filter)
}

data class MonitoredEndpointInFDTO(
    @field:NotBlank @field:Size(min = 3, max = 256)
    @field:Schema(description = "Endpoint name. Must be unique for this user.")
    val name: String,
    @field:NotNull
    @field:Schema(description = "URL to monitor. Must be unique for this user.", type = "string")
    val url: URL,
    @field:NotNull
    @field:Schema(description = "ISO 8601 duration of check interval.", example = "PT20S", type = "string")
    val monitoredInterval: Duration
)

data class MonitoredEndpointOutFDTO(
    val id: UUID,
    val name: String,
    val url: URL,
    val createdAt: Instant,
    @field:Schema(description = "ISO 8601 duration of check interval.", example = "PT20S", type = "string")
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
    val statusCode: Int?,
    @Schema(example = "application/json", type = "string")
    val contentType: MediaType?,
    val payload: String?,   // X|FIXME THa revise payload -- may be quite large -> truncate / separate endpoint with proper media type / multipart
    val monitoredEndpointId: UUID,
    val url: URL,
    @Schema(description = "Contains error if the check failed without HTTP status, e,g. in case of network failure.",)
    val error: String?,
)

data class MonitoringResultListFDTO(
    val items: List<MonitoringResultFDTO>,
    val totalCount: Long,
    val resultCount: Int,
    val requestedPage: Long,
    val requestedPageSize: Long
)
