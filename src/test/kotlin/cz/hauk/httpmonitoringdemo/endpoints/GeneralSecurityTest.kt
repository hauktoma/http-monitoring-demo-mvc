package cz.hauk.httpmonitoringdemo.endpoints

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.*


class GeneralSecurityTest : MonitoredEndpointIntegrationTestBase() {

    @Test
    fun `cannot call endpoints with no api key`() {
        assertAll(
            { getEndpointRemotely(UUID.randomUUID(), "").expectStatus().isUnauthorized },
            { createEndpointRemotely(mockRandomMonitoredEndpointInFDTO(), "").expectStatus().isUnauthorized },
            {
                updateEndpointRemotely(
                    UUID.randomUUID(),
                    mockRandomMonitoredEndpointInFDTO(),
                    ""
                ).expectStatus().isUnauthorized
            },
            { deleteEndpointRemotely(UUID.randomUUID(), "").expectStatus().isUnauthorized },
            { listMonitoringEndpointsRemotely(CustomPaginationFDTO(), "").expectStatus().isUnauthorized },
            { listMonitoringResultsRemotely(UUID.randomUUID(), CustomPaginationFDTO(), "").expectStatus().isUnauthorized },
        )
    }
}