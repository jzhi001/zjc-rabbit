@file: JvmName("RabbitCli")

package com.github.jzhi001.rabbit

import EnhancedChannel
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

//TODO strategy pattern for publishJson() and consumer(anonymous object)
class RabbitClient(private val factory: CachingRabbitConnFactory) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun sendTo(exchange: String, routeKey: String): RabbitCaller =
            RabbitCaller(factory, Destination.sendDestination(exchange, routeKey))

    fun from(queue: String): RabbitWorker = RabbitWorker(queue, factory.getEnhancedChannel())
}

class RabbitWorker(private val queue: String,
                   private val channel: EnhancedChannel) {

    fun <T : Any> work(job: (T) -> Any?) {
        channel.basicConsume(queue, WorkerConsumer(channel, job))
    }

    private class WorkerConsumer<T : Any>(val channel: EnhancedChannel,
                                          val job: (T) -> Any?) : DefaultConsumer(channel) {
        override fun handleDelivery(consumerTag: String?,
                                    envelope: Envelope?,
                                    properties: AMQP.BasicProperties?,
                                    body: ByteArray?) {
            channel.basicAck(envelope!!.deliveryTag, false)
            val req = getReplyObj(properties!!, body!!)
            val resp = job(req as T)

            if (needReply(properties.replyTo)) sendResponse(resp, properties)

        }

        private fun needReply(replyTo: String?): Boolean = !replyTo.isNullOrBlank()

        private fun sendResponse(resp: Any?, properties: AMQP.BasicProperties) {
            println("sending to ${properties.replyTo}")
            val msgParams: MsgParams<T> = MsgParams(correlationId = properties.correlationId, msg = resp as T)
            channel.publishJson(msgParams, Destination.replyDestination(properties.replyTo))
        }

    }
}

class RabbitCaller(
        factory: RabbitConnFactory,
        private val destination: Destination) {

    private val sendChannel: EnhancedChannel = factory.getEnhancedChannel()
    private val replyChannel = factory.getEnhancedChannel()
    private var replyQueue: String = sendChannel.declareAnonymousQueue()

    private var callbackHandler: CallbackHandler<*>? = null

    fun <T : Any> sendJson(obj: T,
                           correlationId: String = UUID.randomUUID().toString()): RabbitCaller {
        val msgParam: MsgParams<T> = MsgParams(msg = obj, correlationId = correlationId, replyTo = replyQueue)
        sendChannel.publishJson(msgParam, destination)
        return this
    }


    fun <T : Any> setCallback(resolve: (T) -> Unit, reject: (T) -> Unit): RabbitCaller {
        callbackHandler = CallbackHandler(resolve, reject).also {
            registerListener(replyChannel, it)
        }
        return this
    }

    private fun <T : Any> registerListener(channel: EnhancedChannel, callbackHandler: CallbackHandler<T>) {
        channel.basicConsume(replyQueue,
                false,
                ReplyConsumer(channel, callbackHandler))
    }

}

private fun <T : Any> Channel.publishJson(
        msgParams: MsgParams<T>,
        destination: Destination) {
    basicPublish(destination.exchange,
            destination.routeKey,
            buildAmqpJsonProp(msgParams),
            JsonConverter.toJsonBytes(msgParams.msg))
}

private fun <T : Any> buildAmqpJsonProp(msgParams: MsgParams<T>): AMQP.BasicProperties {
    return AMQP.BasicProperties.Builder()
            .contentEncoding("utf-8")
            .contentType(msgParams.contentType)
            .type(msgParams.msgClassName)
            .correlationId(msgParams.correlationId)
            .timestamp(Date())
            .replyTo(msgParams.replyTo)
            .build()
}

private class ReplyConsumer<T : Any>(channel: EnhancedChannel,
                                     private val callbackHandler: CallbackHandler<T>) : DefaultConsumer(channel) {
    override fun handleDelivery(consumerTag: String?,
                                envelope: Envelope?,
                                properties: AMQP.BasicProperties?,
                                body: ByteArray?) {
        channel.basicAck(envelope!!.deliveryTag, false)
        val replyObj = getReplyObj(properties!!, body!!)

        try {
            callbackHandler.resolve(replyObj as T)
        } catch (e: Exception) {
            callbackHandler.reject(replyObj as T)
        }
    }
}

private fun getReplyObj(properties: AMQP.BasicProperties, body: ByteArray): Any =
        JsonConverter.fromJsonBytes(body, properties.type)

data class CallbackHandler<T>(
        val resolve: (T) -> Unit,
        val reject: (T) -> Unit
)

data class MsgParams<T : Any>(
        val contentType: String = "application/json",
        val correlationId: String = UUID.randomUUID().toString(),
        val msg: T,
        val msgClassName: String = msg.getClassDescription(),
        val replyTo: String? = null)

private fun Any.getClassDescription(): String =
        when (this) {
            is List<*> -> {
                val generic = this[0]!!::class.java.name
                "java.util.List<$generic>"
            }
            is Map<*, *> -> {
                val (keyClass, valClass) = this.asSequence()
                        .map { (k, v) ->
                            k!!::class.java.name to v!!::class.java.name
                        }
                        .first()
                "java.util.Map<$keyClass, $valClass>"
            }
            else -> this.javaClass.name
        }

private inline fun <reified T : Any> List<T>.genericType(): String = T::class.java.name


class Destination private constructor(val exchange: String = "", val routeKey: String) {
    companion object Factory {
        @JvmStatic
        fun replyDestination(queue: String): Destination = Destination(exchange = "", routeKey = queue)

        @JvmStatic
        fun sendDestination(exchange: String = "", routeKey: String): Destination = Destination(exchange, routeKey)
    }
}

