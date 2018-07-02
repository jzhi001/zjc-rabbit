package com.github.jzhi001.rabbit

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JsonConverter {

    companion object Converter{
        private val mapper: ObjectMapper = ObjectMapper()

        fun toJsonBytes(obj: Any): ByteArray = mapper.writeValueAsBytes(obj)

        fun <T> fromJsonBytes(jsonBytes: ByteArray, clazz: Class<T>): T = mapper.readValue(jsonBytes, clazz)

    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)


}