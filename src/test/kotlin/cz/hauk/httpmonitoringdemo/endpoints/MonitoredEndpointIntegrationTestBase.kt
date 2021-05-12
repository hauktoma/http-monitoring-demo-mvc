package cz.hauk.httpmonitoringdemo.endpoints

import cz.hauk.httpmonitoringdemo.HttpMonitoringDemoApplication
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.time.Duration
import java.util.*

@SpringBootTest(
    classes = [HttpMonitoringDemoApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
abstract class MonitoredEndpointIntegrationTestBase {

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var dbCleanDBTestHelper: CleanDBTestHelper

    @BeforeEach
    fun cleanDB(): Unit = dbCleanDBTestHelper.cleanDB()

    fun deleteEndpointRemotely(
        id: UUID, apiKey: String = TestUserData.USER_API_KEY_1
    ): WebTestClient.ResponseSpec = webClient
        .delete()
        .uri("/api/v1/monitoredEndpoint/id-{id}", id)
        .setApiKey(apiKey)
        .exchange()

    fun deleteEndpointRemotelyAndAssertResult(
        id: UUID, apiKey: String = TestUserData.USER_API_KEY_1
    ) = deleteEndpointRemotely(id, apiKey).expectStatus().isOk


    fun getEndpointRemotely(
        id: UUID, apiKey: String = TestUserData.USER_API_KEY_1
    ) = webClient
        .get()
        .uri("/api/v1/monitoredEndpoint/id-{id}", id)
        .setApiKey(apiKey)
        .exchange()

    fun getEndpointRemotelyAndAssertResult(
        id: UUID, apiKey: String = TestUserData.USER_API_KEY_1
    ) = getEndpointRemotely(id, apiKey)
        .expectStatus().isOk
        .expectBody(MonitoredEndpointOutFDTO::class.java)
        .returnResult()
        .responseBody!!

    fun updateEndpointRemotely(
        id: UUID, update: MonitoredEndpointInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ) = webClient
        .put()
        .uri("/api/v1/monitoredEndpoint/id-{id}", id)
        .setApiKey(apiKey)
        .bodyValue(update)
        .exchange()

    fun updateEndpointRemotelyAndAssertResult(
        id: UUID, update: MonitoredEndpointInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ): MonitoredEndpointOutFDTO = updateEndpointRemotely(id, update, apiKey)
        .expectStatus().isOk
        .expectBody(MonitoredEndpointOutFDTO::class.java)
        .returnResult()
        .responseBody!!

    fun createEndpointRemotely(
        input: MonitoredEndpointInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ) = webClient
        .post()
        .uri("/api/v1/monitoredEndpoint")
        .setApiKey(apiKey)
        .bodyValue(input)
        .exchange()

    fun createEndpointRemotelyAndAssertResult(
        input: MonitoredEndpointInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ): MonitoredEndpointOutFDTO = createEndpointRemotely(input, apiKey)
        .expectStatus().isCreated
        .expectBody(MonitoredEndpointOutFDTO::class.java)
        .returnResult()
        .responseBody!!.also { response ->
            assertAll(
                { Assertions.assertThat(response.id).isNotNull() },
                { Assertions.assertThat(response.createdAt).isNotNull() },
                { Assertions.assertThat(response.monitoredInterval).isEqualTo(input.monitoredInterval) },
                { Assertions.assertThat(response.url).isEqualTo(input.url) },
                { Assertions.assertThat(response.name).isEqualTo(input.name) },
                { Assertions.assertThat(response.ownerUserId.toString()).isNotBlank() },
            )
        }

    fun listMonitoringResultsRemotely(
        id: UUID, filter: MonitoredResultFilterInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ) = webClient
        .get()
        .uri { uri ->
            uri.path("/api/v1/monitoredEndpoint/id-{id}/result/list")
                .queryParam("page", filter.page)
                .queryParam("size", filter.size)
                .build(id)
        }
        .setApiKey(apiKey)
        .exchange()

    fun listMonitoringResultRemotelyAndAssertResult(
        id: UUID, filter: MonitoredResultFilterInFDTO, apiKey: String = TestUserData.USER_API_KEY_1
    ): MonitoringResultListFDTO = listMonitoringResultsRemotely(id, filter, apiKey)
        .expectStatus().isOk
        .expectBody(MonitoringResultListFDTO::class.java)
        .returnResult()
        .responseBody!!

    protected fun mockRandomMonitoredEndpointInFDTO(): MonitoredEndpointInFDTO = MonitoredEndpointInFDTO(
        name = UUID.randomUUID().toString(),
        url = URL("https://${UUID.randomUUID()}.com"),
        monitoredInterval = Duration.ofSeconds((Random().nextInt(999) + 100).toLong())
    )
}

fun <S : WebTestClient.RequestHeadersSpec<S>> WebTestClient.RequestHeadersSpec<S>.setApiKey(
    apiKey: String
): S = header("Authorization", "ApiKey $apiKey")
