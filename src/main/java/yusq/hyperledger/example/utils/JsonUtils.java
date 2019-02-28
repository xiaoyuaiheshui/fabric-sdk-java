package yusq.hyperledger.example.utils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * @Author: yusq
 * @Date: 2018/12/27 0027
 */
public class JsonUtils {
    private static final ObjectMapper defaultObjectMapper = createDefaultObjectMapper();
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper defaultObjectMapper = new ObjectMapper();
        // 建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用
        //defaultObjectMapper.setSerializationInclusion(Include.NON_DEFAULT);
        //去掉默认的时间戳格式
        defaultObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultObjectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
        defaultObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //单引号处理,允许单引号
        defaultObjectMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        defaultObjectMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        return defaultObjectMapper;
    }
    /**
     * 对象转json字符串
     * @param object
     * @return
     */
    public static String object2Json(Object object) {
        try {
            return defaultObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JacksonJsonException(e);
        }
    }

    public static class JacksonJsonException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public JacksonJsonException(String message, Throwable cause) {
            super(message, cause);
        }

        public JacksonJsonException(String message) {
            super(message);
        }

        public JacksonJsonException(Throwable cause) {
            super(cause);
        }

    }
}
