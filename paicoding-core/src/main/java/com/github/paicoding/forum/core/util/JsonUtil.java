package com.github.paicoding.forum.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class JsonUtil {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static  <T> T toObj(String str, Class<T> clz) {
        try {
            return jsonMapper.readValue(str, clz);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static <T> String toStr(T t) {
        try {
            return jsonMapper.writeValueAsString(t);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * 序列换成json时,将所有的long变成string
     * 因为js中得数字类型不能包含所有的java long值
     */
    public static SimpleModule bigIntToStrsimpleModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, newSerializer(s -> String.valueOf(s)));
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        simpleModule.addSerializer(long[].class, newSerializer((Function<Long, String>) String::valueOf));
        simpleModule.addSerializer(Long[].class, newSerializer((Function<Long, String>) String::valueOf));
        simpleModule.addSerializer(BigDecimal.class, newSerializer(BigDecimal::toString));
        simpleModule.addSerializer(BigDecimal[].class, newSerializer(BigDecimal::toString));
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        simpleModule.addSerializer(BigInteger[].class, newSerializer((Function<BigInteger, String>) BigInteger::toString));
        return simpleModule;
    }

    public static <T, K> JsonSerializer<T> newSerializer(Function<K, String> func) {
        return new JsonSerializer<T>() {
            @Override
            public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                if (t == null) {
                    jsonGenerator.writeNull();
                    return;
                }

                if (t.getClass().isArray()) {
                    jsonGenerator.writeStartArray();
                    Stream.of(t).forEach(s -> {
                        try {
                            jsonGenerator.writeString(func.apply((K) s));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    jsonGenerator.writeEndArray();
                } else {
                    jsonGenerator.writeString(func.apply((K) t));
                }
            }
        };
    }
}
