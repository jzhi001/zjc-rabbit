package com.github.jzhi001.rabbit

import com.fasterxml.jackson.databind.ObjectMapper
import jdk.internal.org.objectweb.asm.TypeReference
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JsonConverter {

    companion object Converter {
        private val mapper: ObjectMapper = ObjectMapper()

        @JvmStatic
        fun toJsonBytes(obj: Any): ByteArray = mapper.writeValueAsBytes(obj)

        @JvmStatic
        fun fromJsonBytes(jsonBytes: ByteArray, clazz: Class<*>): Any = mapper.readValue(jsonBytes, clazz)

        private val regex = Regex("""java\.util\.List<(.*)>""")

        //TODO refactor
        @JvmStatic
        fun fromJsonBytes(jsonBytes: ByteArray, classDescription: String): Any {
            val findResult = regex.find(classDescription)
            findResult?.value ?: return mapper.readValue(jsonBytes, Class.forName(classDescription))
            val listType = mapper.typeFactory.constructCollectionLikeType(List::class.java, Class.forName(findResult.groupValues[1]))
            return mapper.readValue(jsonBytes, listType)
        }

    }


}