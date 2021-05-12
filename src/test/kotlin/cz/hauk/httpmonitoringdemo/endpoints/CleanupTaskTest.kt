package cz.hauk.httpmonitoringdemo.endpoints

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired


/**
 * Tests the cleanup task function.
 */
class CleanupTaskTest: MonitoredEndpointIntegrationTestBase() {

    @Autowired
    lateinit var deleteTask: MonitoredEndpointResultDeleteTask

    // X|FIXME THa finish this
    @Test
    fun `url and name is unique for one user`() {
        val createdEndpoint = createAndAssertEndpointRemotely(apiKey = TestUserData.USER_API_KEY_1)
        fail("finish")
    }
}