package cz.hauk.httpmonitoringdemo.endpoints

import cz.hauk.httpmonitoringdemo.HttpMonitoringDemoApplication
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
abstract class MonitoredEndpointIntegrationTestBase {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    protected fun deleteEndpointRemotely(
        id: UUID, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<Unit> = restTemplate.exchange(
        "/api/v1/monitoredEndpoints/id-{id}",
        HttpMethod.DELETE,
        HttpEntity<Unit>(
            HttpHeaders().apply { set("Authorization", "ApiKey $apiKey") }
        ),
        Unit::class.java,
        mapOf("id" to id.toString())
    )

    protected fun getEndpointRemotely(
        id: UUID, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<MonitoredEndpointOutFDTO> = getEndpointRemotely(id, MonitoredEndpointOutFDTO::class.java, apiKey)

    protected fun getEndpointRemotelyNoBody(
        id: UUID, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<Unit> = getEndpointRemotely(id, Unit::class.java, apiKey)

    private fun <T> getEndpointRemotely(
        id: UUID, bodyType: Class<T>, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<T> = restTemplate.exchange(
        "/api/v1/monitoredEndpoints/id-{id}",
        HttpMethod.GET,
        HttpEntity<Unit>(HttpHeaders().apply { set("Authorization", "ApiKey $apiKey") }),
        bodyType,
        mapOf("id" to id.toString())
    )

    protected fun updateEndpointRemotely(
        id: UUID, update: MonitoredEndpointInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<MonitoredEndpointOutFDTO> = updateEndpointRemotely(
        id, update, MonitoredEndpointOutFDTO::class.java, apiKey
    )

    protected fun updateEndpointRemotelyNoBody(
        id: UUID, update: MonitoredEndpointInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<Unit> = updateEndpointRemotely(id, update, Unit::class.java, apiKey)

    protected fun <T> updateEndpointRemotely(
        id: UUID, update: MonitoredEndpointInFDTO, bodyType: Class<T>, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<T> = restTemplate.exchange(
        "/api/v1/monitoredEndpoints/id-{id}",
        HttpMethod.PUT,
        HttpEntity(
            update,
            HttpHeaders().apply { set("Authorization", "ApiKey $apiKey") }
        ),
        bodyType,
        mapOf("id" to id.toString())
    )

    protected fun createEndpointRemotelyNoBody(
        input: MonitoredEndpointInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ) = createEndpointRemotely(input, Unit::class.java, apiKey)

    protected fun <T> createEndpointRemotely(
        input: MonitoredEndpointInFDTO, bodyType: Class<T>, apiKey: String = TestUserData.USER_API_KEY_1
    ): ResponseEntity<T> = restTemplate.exchange(
        "/api/v1/monitoredEndpoints",
        HttpMethod.POST,
        HttpEntity(
            input,
            HttpHeaders().apply { set("Authorization", "ApiKey $apiKey") }
        ),
        bodyType
    )

    protected fun createAndAssertEndpointRemotely(
        input: MonitoredEndpointInFDTO = mockRandomMonitoredEndpointInFDTO(),
        apiKey: String = TestUserData.USER_API_KEY_1
    ): MonitoredEndpointOutFDTO = createEndpointRemotely(
        input, MonitoredEndpointOutFDTO::class.java, apiKey
    ).also { response ->
        assertAll(
            { Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED) },
            { Assertions.assertThat(response.hasBody()).isTrue() },
            { Assertions.assertThat(response.body?.id).isNotNull() },
            { Assertions.assertThat(response.body?.createdAt).isNotNull() },
            { Assertions.assertThat(response.body?.monitoredInterval).isEqualTo(input.monitoredInterval) },
            { Assertions.assertThat(response.body?.url).isEqualTo(input.url) },
            { Assertions.assertThat(response.body?.name).isEqualTo(input.name) },
            { Assertions.assertThat(response.body?.ownerUserId?.toString()).isNotBlank() },
        )
    }.let { it.body!! }

    protected fun mockRandomMonitoredEndpointInFDTO(): MonitoredEndpointInFDTO = MonitoredEndpointInFDTO(
        name = UUID.randomUUID().toString(),
        url = URL("https://${UUID.randomUUID()}.com"),
        monitoredInterval = Duration.ofSeconds((Random().nextInt(999) + 100).toLong())
    )
}
