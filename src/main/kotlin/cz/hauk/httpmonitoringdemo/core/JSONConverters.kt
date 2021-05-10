package cz.hauk.httpmonitoringdemo.core

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.boot.jackson.JsonComponent
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.net.URL

@JsonComponent
object URLConverter {
    class Serialize : JsonSerializer<URL>() {
        override fun serialize(
            value: URL, jgen: JsonGenerator, provider: SerializerProvider
        ) = jgen.writeString(value.toString())
    }

    class Deserialize : JsonDeserializer<URL>() {
        override fun deserialize(
            jp: JsonParser, ctxt: DeserializationContext
        ): URL? = jp.text.let { input ->
            kotlin.runCatching {
                URL(input)
            }.getOrElse {
                ctxt.handleWeirdStringValue(URL::class.java, input, "Invalid URL.")
                null
            }
        }
    }
}