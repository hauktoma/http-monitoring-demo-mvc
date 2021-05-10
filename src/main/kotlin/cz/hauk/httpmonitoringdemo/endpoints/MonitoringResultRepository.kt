package cz.hauk.httpmonitoringdemo.endpoints

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpStatus
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
    @Id val id: UUID,
    val checkedAt: Instant,
    val statusCode: HttpStatus,
    val contentType: ContentDisposition?,
    val payload: String?,
    val monitoredEndpointId: UUID,

    /**
     * URL on which this result was obtained. May differ from the
     * [cz.hauk.httpmonitoringdemo.endpoints.MonitoredEndpointDBO.url] in case
     * when it was changed.
     */
    val url: URL
)
