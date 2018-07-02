@file:JvmName("TestEnvironment")

import com.github.jzhi001.rabbit.CachingRabbitConnFactory

data class Dog(var name: String? = null,
               var age: Int? = null)

fun getChannel(): EnhancedChannel =
        CachingRabbitConnFactory("localhost",
                5672,
                "guest",
                "guest").getEnhancedChannel()

fun getDog(): Dog = Dog("cola", 11)

fun getDogs(): List<Dog> = listOf(getDog(), Dog("doggie", 1))

fun getColaJson(): String = """{"name":"cola","age":11}"""

fun getDogsJson(): String = """[{"name":"cola","age":11},{"name":"doggie","age":1}]"""