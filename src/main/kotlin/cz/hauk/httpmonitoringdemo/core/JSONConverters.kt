package cz.hauk.httpmonitoringdemo.core

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.boot.jackson.JsonComponent
import org.springframework.http.MediaType
import java.net.URL

@JsonComponent
object JsonConverters {
    class SerializeUrl : JsonSerializer<URL>() {
        override fun serialize(
            value: URL, jgen: JsonGenerator, provider: SerializerProvider
        ) = jgen.writeString(value.toString())
    }

    class DeserializeUrl : JsonDeserializer<URL>() {
        override fun deserialize(
            jp: JsonParser, ctxt: DeserializationContext
        ): URL? = jp.text.let { input ->
            if (input.length > MAX_URL_LENGTH) ctxt.handleWeirdStringValue(
                URL::class.java,
                input,
                "URL too long. Max length: $MAX_URL_LENGTH, given: ${input.length}."
            )

            kotlin.runCatching {
                URL(input)
            }.getOrElse {
                ctxt.handleWeirdStringValue(URL::class.java, input, "Invalid URL.")
                null
            }
        }

        companion object {
            private const val MAX_URL_LENGTH = 9999
        }
    }

    class SerializeMediaType : JsonSerializer<MediaType>() {
        override fun serialize(
            value: MediaType, jgen: JsonGenerator, provider: SerializerProvider
        ) = jgen.writeString(value.toString())
    }
}