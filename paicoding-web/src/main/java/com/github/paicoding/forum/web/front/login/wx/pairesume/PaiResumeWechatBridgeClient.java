package com.github.paicoding.forum.web.front.login.wx.pairesume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.web.front.login.wx.config.WxLoginProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class PaiResumeWechatBridgeClient {
    private final PaiResumeWechatBridgeProperties properties;
    private final WxLoginProperties wxLoginProperties;
    private final PaiResumeWechatBridgeSigner signer;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public PaiResumeWechatBridgeClient(PaiResumeWechatBridgeProperties properties,
                                       WxLoginProperties wxLoginProperties,
                                       PaiResumeWechatBridgeSigner signer,
                                       ObjectMapper objectMapper) {
        this.properties = properties;
        this.wxLoginProperties = wxLoginProperties;
        this.signer = signer;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMillis());
        factory.setReadTimeout(properties.getReadTimeoutMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public boolean isPaiResumeScene(String scene) {
        return properties.isEnabled()
                && StringUtils.isNotBlank(scene)
                && scene.startsWith(properties.getScenePrefix());
    }

    public void forward(String eventType, String openId, String scene) {
        properties.requireReady();
        if (StringUtils.isBlank(eventType) || StringUtils.isBlank(openId)) {
            throw new IllegalArgumentException("WeChat bridge event is incomplete");
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("appId", wxLoginProperties.getAppId());
            payload.put("eventType", eventType.toLowerCase());
            payload.put("openId", openId);
            if (StringUtils.isNotBlank(scene)) {
                payload.put("scene", scene);
            }
            byte[] rawBody = objectMapper.writeValueAsBytes(payload);
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            byte[] nonceBytes = new byte[18];
            secureRandom.nextBytes(nonceBytes);
            String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Pai-Timestamp", timestamp);
            headers.set("X-Pai-Nonce", nonce);
            headers.set("X-Pai-Signature", signer.sign(
                    properties.getBridgeSecret(), timestamp, nonce, rawBody));

            ResponseEntity<Void> response = restTemplate.exchange(
                    properties.normalizedCallbackUrl(), HttpMethod.POST,
                    new HttpEntity<>(rawBody, headers), Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("PaiResume bridge rejected the event");
            }
        } catch (Exception exception) {
            log.warn("PaiResume WeChat bridge delivery failed eventType={}, errorType={}",
                    eventType, exception.getClass().getSimpleName());
            throw exception instanceof RuntimeException
                    ? (RuntimeException) exception
                    : new IllegalStateException("PaiResume bridge delivery failed", exception);
        }
    }
}
