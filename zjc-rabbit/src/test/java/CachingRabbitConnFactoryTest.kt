import com.github.jzhi001.rabbit.CachingRabbitConnFactory
import com.github.jzhi001.rabbit.RabbitConnFactory
import org.junit.BeforeClass
import org.junit.Test

import org.junit.Assert.*

class CachingRabbitConnFactoryTest {

    @Test
    fun test_get_connection() {
        assertNotNull("connection is null", factory!!.getConnection())
    }

    @Test
    fun test_get_channel() {
        assertNotNull("channel is null", factory!!.getChannel())
    }

    @Test
    fun test_get_enhanced_channel() {
        assertEquals("not a EnhancedChannel",
                EnhancedChannel::class.java, factory!!.getEnhancedChannel().javaClass)
    }

    companion object {
        private val host = "localhost"
        private val port = 5672
        private val username = "guest"
        private val password = "guest"

        private var factory: RabbitConnFactory? = null

        @BeforeClass
        fun initFactory() {
            factory = CachingRabbitConnFactory(host, port, username, password)
        }
    }
}