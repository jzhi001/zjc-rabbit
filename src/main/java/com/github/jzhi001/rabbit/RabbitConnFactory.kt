package com.github.jzhi001.rabbit

import com.rabbitmq.client.Connection
import com.github.jzhi001.rabbit.EnhancedChannel
import com.rabbitmq.client.Channel

interface RabbitConnFactory {
    fun getConnection(): Connection
    fun getChannel(): Channel
    fun getEnhancedChannel(): EnhancedChannel

}