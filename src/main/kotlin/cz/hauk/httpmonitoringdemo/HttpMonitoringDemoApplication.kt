package cz.hauk.httpmonitoringdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableJdbcRepositories
@EnableTransactionManagement
class HttpMonitoringDemoApplication

fun main(args: Array<String>) {
	runApplication<HttpMonitoringDemoApplication>(*args)
}
