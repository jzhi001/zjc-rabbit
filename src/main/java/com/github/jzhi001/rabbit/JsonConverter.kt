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

        @JvmStatic
        fun fromJsonBytes(jsonBytes: ByteArray, classDescription: String): Any {
            if (!classDescription.contains('<'))
                return mapper.readValue(jsonBytes, classDescription.toClass())
            val listClass = classDescription.getListGeneric()?.toClass()
            if (listClass != null) {
                mapper.typeFactory.constructCollectionLikeType(List::class.java, listClass)
                        .also { return mapper.readValue(jsonBytes, it) }
            }
            val mapClasses = classDescription.getMapGeneric()
            if (mapClasses != null) {
                val keyClass = mapClasses.first.toClass()
                val valClass = mapClasses.second.toClass()
                mapper.typeFactory.constructMapLikeType(HashMap::class.java, keyClass, valClass)
                        .also { return mapper.readValue(jsonBytes, it) }
            }
            return mapper.readValue(jsonBytes, classDescription.toClass())
        }

        private val listGenericRegex = Regex("""java\.util\.List<(.*)>""")
        private val mapGenericRegex = Regex("""java\.util\.Map<(.*),(.*)>""")
        private const val javaObjectClass: String = "java.lang.Object"

        private fun String.toClass() = Class.forName(this)

        private fun String.getListGeneric(): String? {
            val findResult = listGenericRegex.find(this)
            findResult?.value ?: return null
            return findResult.groups[1]?.value ?: javaObjectClass
        }

        private fun String.getMapGeneric(): Pair<String, String>? {
            val findResult = mapGenericRegex.find(this)
            findResult?.value ?: return null
            val keyClass: String = findResult.groups[1]?.value ?: javaObjectClass
            val valClass: String = findResult.groups[2]?.value ?: javaObjectClass
            return keyClass to valClass
        }

    }
}

