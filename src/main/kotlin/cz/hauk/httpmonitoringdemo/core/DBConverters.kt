package cz.hauk.httpmonitoringdemo.core

import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Duration
import java.util.*

@Service
class MyJdbcConfiguration : AbstractJdbcConfiguration() {
    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions = listOf(
        UUIDToStringConverter(),
        StringToUUIDConverter(),
        UrlToStringConverter(),
        StringToUrlConverter(),
        DurationToLongConverter(),
        LongToDurationConverter()
    ).let { converters ->
        JdbcCustomConversions(converters)
    }
}

@WritingConverter
class UUIDToStringConverter : Converter<UUID, String> {
    override fun convert(source: UUID): String = source.toString()
}

@ReadingConverter
class StringToUUIDConverter : Converter<String, UUID> {
    override fun convert(source: String): UUID = UUID.fromString(source)
}

@WritingConverter
class UrlToStringConverter : Converter<URL, String> {
    override fun convert(source: URL): String = source.toString()
}

@ReadingConverter
class StringToUrlConverter : Converter<String, URL> {
    override fun convert(source: String): URL = URL(source)
}

@WritingConverter
class DurationToLongConverter : Converter<Duration, Long> {
    override fun convert(source: Duration): Long = source.toMillis()
}

@ReadingConverter
class LongToDurationConverter : Converter<Long, Duration> {
    override fun convert(source: Long): Duration = Duration.ofMillis(source)
}
