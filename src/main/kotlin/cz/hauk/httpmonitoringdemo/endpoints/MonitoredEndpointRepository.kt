package cz.hauk.httpmonitoringdemo.endpoints

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*

@Repository
interface MonitoredEndpointRepository : CrudRepository<MonitoredEndpointDBO, UUID> {

    @Query("SELECT * FROM monitored_endpoint_dbo WHERE id = :id AND owner_user_id = :owner_user_id")
    fun findByIdAndUser(
        @Param("id") id: UUID,
        @Param("owner_user_id") owner_user_id: UUID,
    ): MonitoredEndpointDBO?

    @Query("SELECT * FROM monitored_endpoint_dbo WHERE name = :name AND owner_user_id = :owner_user_id")
    fun findByNameAndUser(
        @Param("name") name: String,
        @Param("owner_user_id") owner_user_id: UUID,
    ): MonitoredEndpointDBO?

    @Query("SELECT * FROM monitored_endpoint_dbo WHERE url = :url AND owner_user_id = :owner_user_id")
    fun findByUrlAndUser(
        @Param("url") url: URL,
        @Param("owner_user_id") owner_user_id: UUID,
    ): MonitoredEndpointDBO?

    @Modifying
    @Query("DELETE FROM monitored_endpoint_dbo WHERE id = :id AND owner_user_id = :owner_user_id")
    fun deleteByIdAndUser(
        @Param("id") id: UUID,
        @Param("owner_user_id") owner_user_id: UUID
    )

    @Modifying
    @Query(
        """
        UPDATE monitored_endpoint_dbo SET
            name = :name,
            url = :url,
            monitored_interval = :monitored_interval
        WHERE id = :id AND owner_user_id = :owner_user_id
    """
    )
    fun updateForUser(
        @Param("id") id: UUID,
        @Param("owner_user_id") owner_user_id: UUID,
        @Param("name") name: String,
        @Param("url") url: URL,
        @Param("monitored_interval") monitoredInterval: Duration,
    ): Int

    @Query(
        """
            SELECT * FROM monitored_endpoint_dbo 
            WHERE next_check_at < :now
            ORDER BY next_check_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
        """
    )
    fun findAndLockRecordForMonitoring(@Param("now") now: Instant): MonitoredEndpointDBO?

    @Modifying
    @Query(
        """
            UPDATE monitored_endpoint_dbo SET
               next_check_at = :reschedule_to
            WHERE id = :id
        """
    )
    fun rescheduleRecordForMonitoring(
        @Param("id") id: UUID,
        @Param("reschedule_to") rescheduleTo: Instant
    ): Int
}

data class MonitoredEndpointDBO(
    @Id @JvmField val id: UUID,
    val name: String,
    val url: URL,
    val createdAt: Instant,
    val monitoredInterval: Duration,
    val ownerUserId: UUID,
    val nextCheckAt: Instant,
    @Transient
    val isNewEntity: Boolean = false
) : Persistable<UUID> {
    @PersistenceConstructor
    constructor(
        id: UUID,
        name: String,
        url: URL,
        createdAt: Instant,
        monitoredInterval: Duration,
        ownerUserId: UUID,
        nextCheckAt: Instant
    ) : this(
        id,
        name,
        url,
        createdAt,
        monitoredInterval,
        ownerUserId,
        nextCheckAt,
        false
    )

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewEntity
}

fun MonitoredEndpointDBO.toFDTO(): MonitoredEndpointOutFDTO = MonitoredEndpointOutFDTO(
    id = this.id,
    name = this.name,
    url = this.url,
    createdAt = this.createdAt,
    monitoredInterval = this.monitoredInterval,
    ownerUserId = this.ownerUserId
)
