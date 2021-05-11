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
        val createResponse = createAndAssertEndpointRemotely()

        // change the monitored endpoint
        val update = MonitoredEndpointInFDTO(
            name = UUID.randomUUID().toString(),
            url = URL("https://proper-url-after-update.com"),
            monitoredInterval = Duration.ofSeconds(12)
        )
        updateEndpointRemotely(createResponse.id, update).also { response ->
            assertAll(
                { Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
                { Assertions.assertThat(response.hasBody()).isTrue() },
                { Assertions.assertThat(response.body?.id).isNotNull() },
                { Assertions.assertThat(response.body?.createdAt).isEqualTo(createResponse.createdAt) },
                { Assertions.assertThat(response.body?.monitoredInterval).isEqualTo(update.monitoredInterval) },
                { Assertions.assertThat(response.body?.url).isEqualTo(update.url) },
                { Assertions.assertThat(response.body?.name).isEqualTo(update.name) },
                {
                    Assertions.assertThat(response.body?.ownerUserId?.toString())
                        .isEqualTo("aeb0686e-592d-4853-be57-ac64c892c370")
                },
            )
        }

        // retrieve the monitored endpoint
        getEndpointRemotely(createResponse.id).also { response ->
            assertAll(
                { Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK) },
                { Assertions.assertThat(response.hasBody()).isTrue() },
                { Assertions.assertThat(response.body?.id).isNotNull() },
                { Assertions.assertThat(response.body?.createdAt).isEqualTo(createResponse.createdAt) },
                { Assertions.assertThat(response.body?.monitoredInterval).isEqualTo(update.monitoredInterval) },
                { Assertions.assertThat(response.body?.url).isEqualTo(update.url) },
                { Assertions.assertThat(response.body?.name).isEqualTo(update.name) },
                {
                    Assertions.assertThat(response.body?.ownerUserId?.toString())
                        .isEqualTo("aeb0686e-592d-4853-be57-ac64c892c370")
                },
            )
        }

        // delete the monitored endpoint
        deleteEndpointRemotely(createResponse.id).also { response ->
            Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        // assert that HTTP 404 is returned when the endpoint is deleted
        getEndpointRemotelyNoBody(createResponse.id).also { response ->
            Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    // ?|FIXME THa maybe cleaner to return forbidden HTTP 403? Guessing?
    @Test
    fun `monitored endpoint cannot be updated or retrieved by other users`() {
        val createdEndpoint = createAndAssertEndpointRemotely(apiKey = TestUserData.USER_API_KEY_1)

        assertAll(
            {
                // another user cannot read other user's endpoint
                getEndpointRemotelyNoBody(
                    id = createdEndpoint.id, apiKey = TestUserData.USER_API_KEY_2
                ).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
                }
            }, {
                // another user cannot update other user's endpoint
                updateEndpointRemotelyNoBody(
                    id = createdEndpoint.id,
                    update = mockRandomMonitoredEndpointInFDTO(),
                    apiKey = TestUserData.USER_API_KEY_2
                ).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
                }
            }, {
                // another user can call the delete, but nothing actually happens
                deleteEndpointRemotely(
                    id = createdEndpoint.id, apiKey = TestUserData.USER_API_KEY_2
                ).let { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                }

                // assert that the delete did not happen for first user
                getEndpointRemotely(
                    id = createdEndpoint.id, apiKey = TestUserData.USER_API_KEY_1
                ).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
                    Assertions.assertThat(result.body?.id).isEqualTo(createdEndpoint.id)
                }
            }
        )
    }

    @Test
    fun `url and name is unique for one user`() {
        val createdEndpoint = createAndAssertEndpointRemotely(apiKey = TestUserData.USER_API_KEY_1)

        assertAll(
            {
                // cannot use the name again for user
                val inputWithDuplicateName = mockRandomMonitoredEndpointInFDTO().copy(name = createdEndpoint.name)

                createEndpointRemotelyNoBody( // user 1 will fail
                    input = inputWithDuplicateName,
                    apiKey = TestUserData.USER_API_KEY_1
                ).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }

                createEndpointRemotelyNoBody( // user 2 will succeed
                    input = inputWithDuplicateName,
                    apiKey = TestUserData.USER_API_KEY_2
                ).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
                }
            }, {
                // cannot use the url again for user
                val inputWithDuplicateUrl = mockRandomMonitoredEndpointInFDTO().copy(url = createdEndpoint.url)

                createEndpointRemotelyNoBody( // user 1 will fail
                    input = inputWithDuplicateUrl,
                    apiKey = TestUserData.USER_API_KEY_1
                ).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }

                createEndpointRemotelyNoBody( // user 2 will succeed
                    input = inputWithDuplicateUrl,
                    apiKey = TestUserData.USER_API_KEY_2
                ).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
                }
            }
        )
    }

    @Test
    fun `create and update endpoint general negative tests`() {
        val createdEndpoint = createAndAssertEndpointRemotely(mockRandomMonitoredEndpointInFDTO())

        assertAll(
            { // empty name
                val emptyNameInput = mockRandomMonitoredEndpointInFDTO().copy(name = "")

                createEndpointRemotelyNoBody(emptyNameInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }

                updateEndpointRemotelyNoBody(createdEndpoint.id, emptyNameInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }
            }, { // name too long
                val tooLongNameInput = mockRandomMonitoredEndpointInFDTO().copy(name = "".padStart(257, 'x'))

                createEndpointRemotelyNoBody(tooLongNameInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }

                updateEndpointRemotelyNoBody(createdEndpoint.id, tooLongNameInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }
            }, { // URL too long
                val longUrlInput = mockRandomMonitoredEndpointInFDTO().copy(
                    url = URL("https://${"".padStart(9999, 'x')}.com")
                )

                createEndpointRemotelyNoBody(longUrlInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }

                updateEndpointRemotelyNoBody(createdEndpoint.id, longUrlInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }
            }, { // low duration
                val lowDurationInput = mockRandomMonitoredEndpointInFDTO().copy(monitoredInterval = Duration.ZERO)

                createEndpointRemotelyNoBody(lowDurationInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }

                updateEndpointRemotelyNoBody(createdEndpoint.id, lowDurationInput).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
                }
            }, { // cannot update non existing endpoint
                updateEndpointRemotelyNoBody(UUID.randomUUID(), mockRandomMonitoredEndpointInFDTO()).also { result ->
                    Assertions.assertThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
                }
            }
        )
    }
}
