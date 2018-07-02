import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel

class EnhancedChannel(var innerChannel: Channel) : Channel by innerChannel {
    fun declareDirectExchange(exchangeName: String) {
        innerChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT)
    }

    fun declareFanoutExchange(exchangeName: String){
        innerChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT)
    }

    fun declareTopicExchange(exchangeName: String){
        innerChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC)
    }

    fun declareAnoymousQueue(): String = innerChannel.queueDeclare().queue

    fun declareOnlyDurableQueue(queueName: String) {
        innerChannel.queueDeclare(queueName,true,false,false,null)
    }

    fun declareBinding(queueName:String, exchange: String, routeKey: String){
        innerChannel.queueBind(queueName, exchange, routeKey)
    }

}
