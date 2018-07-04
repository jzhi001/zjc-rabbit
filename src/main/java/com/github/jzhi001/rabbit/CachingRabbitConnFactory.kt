package com.github.jzhi001.rabbit

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.github.jzhi001.rabbit.EnhancedChannel
import com.rabbitmq.client.Channel

/**
 * Caching the single Connection for use
 */
class CachingRabbitConnFactory(
        host: String,
        port: Int,
        username: String,
        password: String) : RabbitConnFactory {

    private val factory: ConnectionFactory = ConnectionFactory()
    private val conn: Connection = factory.newConnection()

    init {
        factory.host = host
        factory.port = port
        factory.username = username
        factory.password = password
    }

    override fun getConnection(): Connection = conn

    override fun getChannel(): Channel = conn.createChannel()

    override fun getEnhancedChannel(): EnhancedChannel = EnhancedChannel(getChannel())
}