package smart.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;


/**
 * json 助手类
 */
public class Json {

    /**
     * 线程变量 object mapper
     */
    private static final ThreadLocal<ObjectMapper> om = ThreadLocal.withInitial(() -> {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    });

    /**
     * json 字符串转 map
     *
     * @param str json字符串
     * @return map
     */
    public static Map<String, String> decode(String str) {

        Map<String, String> map = null;
        try {
            map = om.get().readValue(str, new TypeReference<>() {
            });
        } catch (JsonProcessingException ignored) {
        }
        return map;
    }

    /**
     * json 字符串转 map
     *
     * @param str json字符串
     * @return map
     */
    public static Map<String, Object> decode1(String str) {

        Map<String, Object> map = null;
        try {
            map = om.get().readValue(str, new TypeReference<>() {
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
            return om.get().readValue(str, tClass);
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
            return om.get().writeValueAsString(value);
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
            list = om.get().readValue(str, new TypeReference<>() {
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
            list = om.get().readValue(str,
                    om.get().getTypeFactory().constructParametricType(List.class, tClass));
        } catch (JsonProcessingException ignored) {
        }
        return list;
    }
}
