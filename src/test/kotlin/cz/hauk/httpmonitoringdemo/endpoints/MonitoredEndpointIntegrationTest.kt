package cz.hauk.httpmonitoringdemo.endpoints

import cz.hauk.httpmonitoringdemo.HttpMonitoringDemoApplication
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.net.URL
import java.time.Duration
import java.util.*

@SpringBootTest(
    classes = [HttpMonitoringDemoApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
internal class MonitoredEndpointIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `can create update and delete monitored endpoint properly`() {
        val input = mockMonitoredEndpointInFDTO()

        // create the monitored endpoint
        val createResponse = restTemplate.exchange(
            "/api/v1/monitoredEndpoints",
            HttpMethod.POST,
            HttpEntity(
                input,
                HttpHeaders().apply { set("Authorization", "ApiKey 93f39e2f-80de-4033-99ee-249d92736a25") }
            ),
            MonitoredEndpointOutFDTO::class.java
        ).also { response ->
            assertAll(
                { Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED) },
                { Assertions.assertThat(response.hasBody()).isTrue() },
                { Assertions.assertThat(response.body?.id).isNotNull() },
                { Assertions.assertThat(response.body?.createdAt).isNotNull() },
                { Assertions.assertThat(response.body?.monitoredInterval).isEqualTo(input.monitoredInterval) },
                { Assertions.assertThat(response.body?.url).isEqualTo(input.url) },
                { Assertions.assertThat(response.body?.name).isEqualTo(input.name) },
                {
                    Assertions.assertThat(response.body?.ownerUserId?.toString())
                        .isEqualTo("aeb0686e-592d-4853-be57-ac64c892c370")
                },
            )
        }.let { it.body!! }

        // change the monitored endpoint
        val update = MonitoredEndpointInFDTO(
            name = UUID.randomUUID().toString(),
            url = URL("https://proper-url-after-update.com"),
            monitoredInterval = Duration.ofSeconds(12)
        )
        restTemplate.exchange(
            "/api/v1/monitoredEndpoints/id-{id}",
            HttpMethod.PUT,
            HttpEntity(
                update,
                HttpHeaders().apply { set("Authorization", "ApiKey 93f39e2f-80de-4033-99ee-249d92736a25") }
            ),
            MonitoredEndpointOutFDTO::class.java,
            mapOf("id" to createResponse.id.toString())
        ).also { response ->
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
        restTemplate.exchange(
            "/api/v1/monitoredEndpoints/id-{id}",
            HttpMethod.GET,
            HttpEntity<Unit>(
                HttpHeaders().apply { set("Authorization", "ApiKey 93f39e2f-80de-4033-99ee-249d92736a25") }
            ),
            MonitoredEndpointOutFDTO::class.java,
            mapOf("id" to createResponse.id.toString())
        ).also { response ->
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
        restTemplate.exchange(
            "/api/v1/monitoredEndpoints/id-{id}",
            HttpMethod.DELETE,
            HttpEntity<Unit>(
                HttpHeaders().apply { set("Authorization", "ApiKey 93f39e2f-80de-4033-99ee-249d92736a25") }
            ),
            Unit::class.java,
            mapOf("id" to createResponse.id.toString())
        ).also { response ->
            Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        // assert that HTTP 404 is returned when the endpoint is deleted
        restTemplate.exchange(
            "/api/v1/monitoredEndpoints/id-{id}",
            HttpMethod.GET,
            HttpEntity<Unit>(
                HttpHeaders().apply { set("Authorization", "ApiKey 93f39e2f-80de-4033-99ee-249d92736a25") }
            ),
            Unit::class.java,
            mapOf("id" to createResponse.id.toString())
        ).also { response ->
            Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    private fun mockMonitoredEndpointInFDTO(): MonitoredEndpointInFDTO = MonitoredEndpointInFDTO(
        name = UUID.randomUUID().toString(),
        url = URL("https://proper-url.com"),
        monitoredInterval = Duration.ofSeconds(11)
    )
}
