package com.github.paicoding.forum.api.model.util.cdn;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author YiHui
 * @date 2026/1/12
 */
public class CdnImgSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        // 自定义 CDN 转换逻辑
        String convertedUrl = CdnUtil.autoTransCdn(value);
        gen.writeString(convertedUrl);
    }
}
