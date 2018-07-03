import com.github.jzhi001.rabbit.JsonConverter
import org.junit.Test

import org.junit.Assert.*
import java.lang.reflect.ParameterizedType
import java.nio.charset.StandardCharsets

class JsonConverterTest {

    private val cola = getDog()
    private val colaJson = getColaJson()
    private val dogs = getDogs()
    private val dogsJson = getDogsJson()

    @Test
    @Throws(Exception::class)
    fun test_to_json_pojo() {
        val converted = String(JsonConverter.toJsonBytes(cola), StandardCharsets.UTF_8)
        assertEquals("toJson(pojo) error", colaJson, converted)
    }

    @Test
    @Throws(Exception::class)
    fun test_from_json_pojo() {
        val converted = JsonConverter.fromJsonBytes(colaJson.toByteArray(charset("utf-8")), Class.forName("Dog"))
        assertEquals("fromJson(pojo) error", cola, converted)
    }

    @Test
    @Throws(Exception::class)
    fun test_to_json_list() {
        val converted = String(JsonConverter.toJsonBytes(dogs), StandardCharsets.UTF_8)
        assertEquals("toJson(list) error", dogsJson, converted)
    }

    @Test
    @Throws(Exception::class)
    fun test_from_json_list() {
        val converted:List<Dog> = JsonConverter.fromJsonBytes(dogsJson.toByteArray(charset("utf-8")),
                "java.util.List<Dog>") as List<Dog>
        assertEquals("fromJson(list) error", dogs, converted)
    }

}
