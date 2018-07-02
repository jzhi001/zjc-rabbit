import com.github.jzhi001.rabbit.CachingRabbitConnFactory;
import com.github.jzhi001.rabbit.RabbitConnFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class CachingRabbitConnFactoryTest {
    private static String host = "localhost";
    private static int port = 5672;
    private static String username = "guest";
    private static String password = "guest";

    private static RabbitConnFactory factory;

    public static RabbitConnFactory getFactory() {
        initFactory();
        return factory;
    }

    @BeforeClass
    public static void initFactory() {
        factory = new CachingRabbitConnFactory(host, port, username, password);
    }

    @Test
    public void test_get_connection() {
        assertNotNull("connection is null", factory.getConnection());
    }

    @Test
    public void test_get_channel() {
        assertNotNull("channel is null", factory.getChannel());
    }

    @Test
    public void test_get_enhanced_channel() {
        assertEquals("not a EnhancedChannel",
                EnhancedChannel.class, factory.getEnhancedChannel().getClass());
    }
}
