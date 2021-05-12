package cz.hauk.httpmonitoringdemo.endpoints

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired

/**
 * This should test approx. following scenario:
 *
 * - start a mock HTTP server
 * - call create monitored endpoint
 * - invoke manually the task that calls the monitored endpoint url and creates the result
 * - retrieve the results for the user, assert they can be retrieved
 * - delete the monitored endpoint
 * - invoke manually the task for cleanup
 * - assert that both endpoint AND results are deleted (API/DB)
 *
 * As of now, it seems that the H2 DB does not support the `SKIP LOCKED` clause (which is essential
 * in case of multiple instance environment). Therefore this
 * functional tests should be probably performed in e.g. some docker environment, ie as a real
 * integration test.
 */
@Disabled("h2 problem with SKIP LOCKED")    // X|FIXME THa finish full lifecycle test
class FullLifeCycleTest : MonitoredEndpointIntegrationTestBase() {

    @Autowired
    lateinit var deleteTask: MonitoredEndpointResultDeleteTask

    @Test
    fun `url and name is unique for one user`() {
        val createdEndpoint = createEndpointRemotelyAndAssertResult(
            input = mockRandomMonitoredEndpointInFDTO(),
            apiKey = TestUserData.USER_API_KEY_1
        )
        fail("finish test")
    }
}
