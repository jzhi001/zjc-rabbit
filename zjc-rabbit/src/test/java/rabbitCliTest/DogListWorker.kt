package rabbitCliTest

import com.github.jzhi001.rabbit.RabbitClient
import factory
import queue
import Dog

fun main(args: Array<String>) {
    RabbitClient(factory)
            .from(queue)
            .work<List<Dog>> {
                println("worker get: $it")
                it.forEach { dog -> dog.name = "done: ${dog.name}" }
                it
            }
}