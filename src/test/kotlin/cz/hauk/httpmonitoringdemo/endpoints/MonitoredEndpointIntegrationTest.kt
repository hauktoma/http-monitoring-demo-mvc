package cz.hauk.httpmonitoringdemo.endpoints

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.http.HttpStatus
import java.net.URL
import java.time.Duration
import java.util.*

internal class MonitoredEndpointIntegrationTest : MonitoredEndpointIntegrationTestBase() {

    @Test
    fun `can create update and delete monitored endpoint properly`() {
        val createResponse = createEndpointRemotelyAndAssertResult(mockRandomMonitoredEndpointInFDTO())

        // change the monitored endpoint
        val update = MonitoredEndpointInFDTO(
            name = UUID.randomUUID().toString(),
            url = URL("https://proper-url-after-update.com"),
            monitoredInterval = Duration.ofSeconds(12)
        )
        updateEndpointRemotelyAndAssertResult(createResponse.id, update).also { response ->
            assertAll(
                { Assertions.assertThat(response.id).isNotNull() },
                { Assertions.assertThat(response.createdAt).isEqualTo(createResponse.createdAt) },
                { Assertions.assertThat(response.monitoredInterval).isEqualTo(update.monitoredInterval) },
                { Assertions.assertThat(response.url).isEqualTo(update.url) },
                { Assertions.assertThat(response.name).isEqualTo(update.name) },
                {
                    Assertions.assertThat(response.ownerUserId.toString())
                        .isEqualTo("aeb0686e-592d-4853-be57-ac64c892c370")
                },
            )
        }

        // retrieve the monitored endpoint
        getEndpointRemotelyAndAssertResult(createResponse.id).also { response ->
            assertAll(
                { Assertions.assertThat(response.id).isNotNull() },
                { Assertions.assertThat(response.createdAt).isEqualTo(createResponse.createdAt) },
                { Assertions.assertThat(response.monitoredInterval).isEqualTo(update.monitoredInterval) },
                { Assertions.assertThat(response.url).isEqualTo(update.url) },
                { Assertions.assertThat(response.name).isEqualTo(update.name) },
                {
                    Assertions.assertThat(response.ownerUserId.toString())
                        .isEqualTo("aeb0686e-592d-4853-be57-ac64c892c370")
                },
            )
        }

        // delete the monitored endpoint
        deleteEndpointRemotelyAndAssertResult(createResponse.id)

        // assert that HTTP 404 is returned when the endpoint is deleted
        getEndpointRemotely(createResponse.id).expectStatus().isNotFound
    }

    // ?|FIXME THa maybe cleaner to return forbidden HTTP 403?
    @Test
    fun `monitored endpoint cannot be updated or retrieved by other users`() {
        val createdEndpoint = createEndpointRemotelyAndAssertResult(
            mockRandomMonitoredEndpointInFDTO(),
            apiKey = TestUserData.USER_API_KEY_1
        )

        assertAll(
            {
                // another user cannot read other user's endpoint
                getEndpointRemotely(
                    id = createdEndpoint.id, apiKey = TestUserData.USER_API_KEY_2
                ).expectStatus().isNotFound
            }, {
                // another user cannot update other user's endpoint
                updateEndpointRemotely(
                    id = createdEndpoint.id,
                    update = mockRandomMonitoredEndpointInFDTO(),
                    apiKey = TestUserData.USER_API_KEY_2
                ).expectStatus().isNotFound
            }, {
                // another user can call the delete, but nothing actually happens
                deleteEndpointRemotelyAndAssertResult(id = createdEndpoint.id, apiKey = TestUserData.USER_API_KEY_2)

                // assert that the delete did not happen for first user
                getEndpointRemotelyAndAssertResult(
                    id = createdEndpoint.id, apiKey = TestUserData.USER_API_KEY_1
                ).also { response ->
                    Assertions.assertThat(response.id).isEqualTo(createdEndpoint.id)
                }
            }
        )
    }

    @Test
    fun `url and name is unique for one user`() {
        val createdEndpoint = createEndpointRemotelyAndAssertResult(
            mockRandomMonitoredEndpointInFDTO(),
            apiKey = TestUserData.USER_API_KEY_1
        )

        assertAll(
            {
                // cannot use the name again for user
                val inputWithDuplicateName = mockRandomMonitoredEndpointInFDTO().copy(name = createdEndpoint.name)

                // user 1 will fail
                createEndpointRemotely(inputWithDuplicateName, TestUserData.USER_API_KEY_1).expectStatus().isBadRequest
                // user 2 will succeed
                createEndpointRemotelyAndAssertResult(inputWithDuplicateName, TestUserData.USER_API_KEY_2)
            }, {
                // cannot use the url again for user
                val inputWithDuplicateUrl = mockRandomMonitoredEndpointInFDTO().copy(url = createdEndpoint.url)

                // user 1 will fail
                createEndpointRemotely(inputWithDuplicateUrl, TestUserData.USER_API_KEY_1).expectStatus().isBadRequest
                createEndpointRemotelyAndAssertResult(inputWithDuplicateUrl, TestUserData.USER_API_KEY_2)
            }
        )
    }

    @Test
    fun `create and update endpoint general negative tests`() {
        val createdEndpoint = createEndpointRemotelyAndAssertResult(mockRandomMonitoredEndpointInFDTO())

        assertAll(
            { // empty name
                val emptyNameInput = mockRandomMonitoredEndpointInFDTO().copy(name = "")
                createEndpointRemotely(emptyNameInput).expectStatus().isBadRequest
                updateEndpointRemotely(createdEndpoint.id, emptyNameInput).expectStatus().isBadRequest
            }, { // name too long
                val tooLongNameInput = mockRandomMonitoredEndpointInFDTO().copy(name = "".padStart(257, 'x'))
                createEndpointRemotely(tooLongNameInput).expectStatus().isBadRequest
                updateEndpointRemotely(createdEndpoint.id, tooLongNameInput).expectStatus().isBadRequest
            }, { // URL too long
                val longUrlInput = mockRandomMonitoredEndpointInFDTO().copy(
                    url = URL("https://${"".padStart(9999, 'x')}.com")
                )
                createEndpointRemotely(longUrlInput).expectStatus().isBadRequest
                updateEndpointRemotely(createdEndpoint.id, longUrlInput).expectStatus().isBadRequest
            }, { // low duration
                val lowDurationInput = mockRandomMonitoredEndpointInFDTO().copy(monitoredInterval = Duration.ZERO)
                createEndpointRemotely(lowDurationInput).expectStatus().isBadRequest
                updateEndpointRemotely(createdEndpoint.id, lowDurationInput).expectStatus().isBadRequest
            }, { // cannot update non existing endpoint
                updateEndpointRemotely(UUID.randomUUID(), mockRandomMonitoredEndpointInFDTO()).expectStatus().isNotFound
            }
        )
    }
}
