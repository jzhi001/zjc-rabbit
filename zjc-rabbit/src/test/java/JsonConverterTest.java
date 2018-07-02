import com.github.jzhi001.rabbit.JsonConverter;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonConverterTest {

    private Dog cola = TestEnvironment.getDog();
    private String colaJson = TestEnvironment.getColaJson();

    @Test
    public void test_to_json_pojo() throws Exception {
        String converted = new String(JsonConverter.Converter.toJsonBytes(cola), "utf-8");
        assertEquals("toJson(pojo) error", colaJson, converted);
    }

    @Test
    public void test_from_json_pojo() throws Exception {
        Dog converted = JsonConverter.Converter.fromJsonBytes(colaJson.getBytes("utf-8"), Dog.class);
        assertEquals("fromJson(pojo) error", cola, converted);
    }

}
