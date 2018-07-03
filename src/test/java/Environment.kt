@file:JvmName("TestEnvironment")

import com.github.jzhi001.rabbit.CachingRabbitConnFactory
import com.github.jzhi001.rabbit.RabbitCaller
import com.github.jzhi001.rabbit.RabbitClient
import java.util.concurrent.ExecutorService

data class Dog(var name: String? = null,
               var age: Int? = null)

val factory = CachingRabbitConnFactory("localhost",
        5672,
        "guest",
        "guest")
val channel: EnhancedChannel = factory.getEnhancedChannel()
val exchange: String = "exchange.kt.test"
val queue: String = "queue.kt.test"
val routeKey: String = "rk.kt.test"
val client: RabbitClient = RabbitClient(factory)

fun getDog(): Dog = Dog("cola", 11)

fun getDogs(): List<Dog> = listOf(getDog(), Dog("doggie", 1))

fun getColaJson(): String = """{"name":"cola","age":11}"""

fun getDogsJson(): String = """[{"name":"cola","age":11},{"name":"doggie","age":1}]"""

fun declareDirectExchange() {
    channel.declareDirectExchange(exchange)
}

fun declareQueue() {
    channel.declareOnlyDurableQueue(queue)
}

fun declareBinding() {
    channel.declareBinding(queueName = queue, exchange = exchange, routeKey = routeKey)
}


val resolve: (Dog) -> Unit = { println("reply $it") }
val reject: (Dog) -> Unit = { println("error, $it") }


