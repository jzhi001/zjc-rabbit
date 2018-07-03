package rabbitCliTest

import Dog
import com.github.jzhi001.rabbit.RabbitClient
import factory
import queue

fun main(args: Array<String>) {
    RabbitClient(factory)
            .from(queue)
            .work<Dog> {
                println("worker get: $it")
                it.also { it.name = "from worker" }
            }
}