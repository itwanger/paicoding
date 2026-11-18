package com.github.paicoding.forum.web.front.table;

import com.github.paicoding.forum.service.chatai.service.impl.deepseek.DeepSeekIntegration.DeepSeekConf;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableAiConfigurationTest {
    private DeepSeekConf bind(MockEnvironment env) throws Exception {
        for (PropertySource<?> source : new YamlPropertySourceLoader().load("dev-ai",
                new FileSystemResource("src/main/resources-env/dev/application-ai.yml"))) {
            env.getPropertySources().addLast(source);
        }
        return Binder.get(env).bind("deepseek", Bindable.of(DeepSeekConf.class)).get();
    }

    @Test void localSharedKeyIsUsedWhenProjectKeyIsUnset() throws Exception {
        DeepSeekConf config=bind(new MockEnvironment().withProperty("DEEPSEEK_API_KEY", "test-shared-value"));
        assertEquals("test-shared-value", config.getApiKey());
        assertEquals("https://api.deepseek.com", config.getApiHost());
    }

    @Test void explicitProjectKeyKeepsPrecedence() throws Exception {
        DeepSeekConf config=bind(new MockEnvironment().withProperty("DEEPSEEK_API_KEY", "test-shared-value")
                .withProperty("PAICODING_DEEPSEEK_API_KEY", "test-project-value"));
        assertEquals("test-project-value", config.getApiKey());
    }
}
