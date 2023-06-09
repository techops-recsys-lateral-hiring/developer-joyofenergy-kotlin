package de.tw.energy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

fun ObjectMapper.setup(): ObjectMapper {
    enable(SerializationFeature.INDENT_OUTPUT)
    enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    registerModule(JavaTimeModule())
    return this
}
