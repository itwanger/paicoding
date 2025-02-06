package com.github.paicoding.forum.test.ai;

import cn.hutool.json.JSONUtil;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YiHui
 * @date 2025/2/6
 */
public class DeepSeekTest {

    public String chat(String prompt, int maxTokens) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer sk-3eecaxxx");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        body.put("stream", false);
        body.put("max_tokens", maxTokens);

        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt);
        body.put("messages", Arrays.asList(msg));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject("https://api.deepseek.com/chat/completions", entity, String.class);
    }

    @Test
    public void testChat() {
        String prompt = "说个段子";
        String res = chat(prompt, 300);
        res = JSONUtil.toJsonPrettyStr(JSONUtil.parseObj(res));
        System.out.println(res);
    }
}
