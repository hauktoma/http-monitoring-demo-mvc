package cz.hauk.httpmonitoringdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableJdbcRepositories
@EnableTransactionManagement
@EnableScheduling
class HttpMonitoringDemoApplication

// X|FIXME THa database indexes
fun main(args: Array<String>) {
	runApplication<HttpMonitoringDemoApplication>(*args)
}
