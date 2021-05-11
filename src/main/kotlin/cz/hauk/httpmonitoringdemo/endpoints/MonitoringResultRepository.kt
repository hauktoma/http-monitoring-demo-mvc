package cz.hauk.httpmonitoringdemo.endpoints

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import java.net.URL
import java.time.Instant
import java.util.*

@Repository
interface MonitoringResultRepository : CrudRepository<MonitoringResultDBO, UUID> {

    @Modifying
    @Query("DELETE FROM monitoring_result_dbo WHERE monitored_endpoint_id = :monitoredEndpointId")
    fun deleteByMonitoredEndpointId(@Param("monitoredEndpointId") monitoredEndpointId: UUID)

}

data class MonitoringResultDBO(
    @Id @JvmField val id: UUID,
    val checkedAt: Instant,
    val statusCode: HttpStatus?,
    val contentType: MediaType?,
    val payload: String?,
    val monitoredEndpointId: UUID,

    /**
     * URL on which this result was obtained. May differ from the
     * [cz.hauk.httpmonitoringdemo.endpoints.MonitoredEndpointDBO.url] in case
     * when it was changed.
     */
    val url: URL,

    /**
     * Contains error message in case that the request was not processed
     * at all, e.g. network error, could not resolve host etc. In this case,
     * the [statusCode] and other request related fields will be null.
     */
    val error: String?,

    @Transient
    val isNewEntity: Boolean = false
) : Persistable<UUID> {
    @PersistenceConstructor
    constructor(
        id: UUID,
        checkedAt: Instant,
        statusCode: HttpStatus?,
        contentType: MediaType?,
        payload: String?,
        monitoredEndpointId: UUID,
        url: URL,
        error: String?
    ): this(
        id, checkedAt, statusCode, contentType, payload, monitoredEndpointId, url, error, false
    )

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewEntity
}
