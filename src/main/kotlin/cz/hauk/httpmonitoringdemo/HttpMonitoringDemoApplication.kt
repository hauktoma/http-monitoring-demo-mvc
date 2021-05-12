package cz.hauk.httpmonitoringdemo

import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.StringSchema
import org.springdoc.core.SpringDocUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.Duration

@SpringBootApplication
@EnableJdbcRepositories
@EnableTransactionManagement
class HttpMonitoringDemoApplication

// X|FIXME THa database indexes
fun main(args: Array<String>) {
    runApplication<HttpMonitoringDemoApplication>(*args)
}
