package rabbitCliTest

import com.github.jzhi001.rabbit.RabbitClient
import exchange
import factory
import getDog
import reject
import resolve
import routeKey

fun main(args: Array<String>) {
    RabbitClient(factory)
            .sendTo(exchange, routeKey)
            .setCallback(resolve, reject)
            .sendJson(getDog())
}