package rabbitCliTest

import Dog
import com.github.jzhi001.rabbit.RabbitClient
import factory
import exchange
import routeKey
import getDogs

val resolve: (List<Dog>) -> Unit = {
    it.forEach { dog -> println(dog) }
}

val reject: (List<Dog>) -> Unit = { println("error, $it") }

fun main(args: Array<String>) {
    RabbitClient(factory)
            .sendTo(exchange, routeKey)
            .setCallback(resolve, reject)
            .sendJson(getDogs())
}