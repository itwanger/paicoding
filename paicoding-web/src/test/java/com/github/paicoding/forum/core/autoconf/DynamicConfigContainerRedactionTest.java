package com.github.paicoding.forum.core.autoconf;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicConfigContainerRedactionTest {

    @Test
    public void shouldMaskSensitiveConfigValuesInLogs() {
        Map<String, Object> config = Maps.newHashMap();
        config.put("view.site.websiteName", "技术派");
        config.put("paicoding.login.wx.appSecret", "wx-secret-value");
        config.put("deepseek.apiKey", "sk-sensitive-value");
        config.put("image.oss.ak", "oss-ak-value");
        config.put("image.oss.sk", "oss-sk-value");
        config.put("Authorization", "Bearer eyJsensitive");

        String json = DynamicConfigContainer.toSafeLogJson(config);

        assertTrue(json.contains("技术派"));
        assertTrue(json.contains("******"));
        assertFalse(json.contains("wx-secret-value"));
        assertFalse(json.contains("sk-sensitive-value"));
        assertFalse(json.contains("oss-ak-value"));
        assertFalse(json.contains("oss-sk-value"));
        assertFalse(json.contains("Bearer eyJsensitive"));
    }
}
