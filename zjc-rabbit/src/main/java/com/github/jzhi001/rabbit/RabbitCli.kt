@file: JvmName("RabbitCli")

package com.github.jzhi001.rabbit

import EnhancedChannel
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

//TODO strategy pattern for sendJson() and consumer(anonymous object)
class RabbitClient(private val channel: EnhancedChannel) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun sendTo(exchange: String, routeKey: String): RabbitCaller =
            RabbitCaller(Destination(exchange, routeKey), channel)

    fun from(queue: String): RabbitWorker = RabbitWorker(queue, channel)
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
            val req = getReplyObj(properties!!, body!!)
            val resp = job(req as T)

            if (needReply(resp, properties.replyTo)) {
                val msgParams: MsgParams<T> = MsgParams(correlationId = properties.correlationId, msg = resp as T)
                sendJson(channel, msgParams, Destination(properties.replyTo, properties.replyTo))
            }

        }

        private fun needReply(resp: Any?, replyTo: String?): Boolean =
                resp != null && !replyTo.isNullOrBlank()

    }
}

class RabbitCaller(
        private val destination: Destination,
        private val channel: EnhancedChannel) {

    private var replyQueue: String? = null

    private var callbackHandler: CallbackHandler<*>? = null

    //TODO send(Json).reply(true).cId(null).body(obj)
    fun <T : Any> sendJson(obj: T,
                           correlationId: String = UUID.randomUUID().toString(),
                           needReply: Boolean = false): RabbitCaller {
        if (needReply) initReplyQueue()
        val msgParam: MsgParams<T> = MsgParams(msg = obj, correlationId = correlationId, replyTo = replyQueue)
        channel.basicPublish(destination.exchange,
                destination.routeKey,
                buildAmqpJsonProp(msgParam),
                JsonConverter.toJsonBytes(obj))
        return this
    }

    private fun initReplyQueue() {
        replyQueue ?: channel.declareAnoymousQueue()
    }

    fun <T : Any> setCallback(resolve: (T) -> Unit, reject: (T) -> Unit): RabbitCaller {
        callbackHandler = CallbackHandler(resolve, reject).also {
            registerListener(channel, it)
        }
        return this
    }

    private fun <T : Any> registerListener(channel: EnhancedChannel, callbackHandler: CallbackHandler<T>) {
        channel.basicConsume(replyQueue,
                false,
                ReplyConsumer(channel, callbackHandler))
    }

}

private fun <T : Any> sendJson(channel: EnhancedChannel,
                               msgParams: MsgParams<T>,
                               destination: Destination) {
    channel.basicPublish(destination.exchange,
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
        JsonConverter.fromJsonBytes(body, Class.forName(properties.className))

data class CallbackHandler<T>(
        val resolve: (T) -> Unit,
        val reject: (T) -> Unit
)

data class MsgParams<T : Any>(
        val contentType: String = "application/json",
        val correlationId: String = UUID.randomUUID().toString(),
        val msg: T,
        val msgClassName: String = msg.javaClass.name,
        val replyTo: String? = null)

data class Destination(val exchange: String, val routeKey: String)

