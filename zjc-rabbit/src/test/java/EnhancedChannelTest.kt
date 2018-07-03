import org.junit.Test

class EnhancedChannelTest{

    @Test
    fun test_declare_queue_exchange_binding(){
        declareDirectExchange()
        declareQueue()
        declareBinding()
    }

    @Test
    fun test_declare_anonymous_queue(){
        channel.declareAnonymousQueue()
    }

}
