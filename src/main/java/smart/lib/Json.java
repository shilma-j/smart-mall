package smart.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * json 助手类
 */
@Component
public class Json {
    private static ObjectMapper mapper;
    private final Log log = LogFactory.getLog(Json.class);

    public Json(ObjectMapper objectMapper) {
        mapper = objectMapper;
    }

    /**
     * json 字符串转 map
     *
     * @param str json字符串
     * @return map
     */
    public static Map<String, String> decode(String str) {

        Map<String, String> map = null;
        try {
            map = mapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
        }
        return map;
    }


    /**
     * json字符串转对象实列
     *
     * @param str    json字符串
     * @param tClass 实例的类型
     * @param <T>    泛型
     * @return 转换的对象实例，失败返回null
     */
    public static <T> T decode(String str, Class<T> tClass) {
        try {
            return mapper.readValue(str, tClass);
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }

    /**
     * 对象序列化为 json
     *
     * @param value 要转换成 json 的实列
     * @return json字符串, 失败返回null
     */
    public static String encode(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }


    /***
     * json反序列化为List<Map<String, String>>
     * @param str json字符串
     * @return 转换后的List, 失败返回null
     */
    public static List<Map<String, String>> toList(String str) {

        List<Map<String, String>> list = null;
        try {
            list = mapper.readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
        }
        return list;
    }

    /**
     * json转换为指定类实例的List
     *
     * @param str    json字符串
     * @param tClass 实例的类型
     * @param <T>    泛型
     * @return 转换后的List, 失败返回null
     */
    public static <T> List<T> toList(String str, Class<T> tClass) {
        List<T> list = null;
        try {
            list = mapper.readValue(str,
                    mapper.getTypeFactory().constructParametricType(List.class, tClass));
        } catch (JsonProcessingException ignored) {
        }
        return list;
    }
}
