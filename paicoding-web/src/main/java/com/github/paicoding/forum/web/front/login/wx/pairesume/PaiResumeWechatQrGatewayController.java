package com.github.paicoding.forum.web.front.login.wx.pairesume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.web.front.login.wx.helper.WxLoginQrGenIntegration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/pairesume/wechat")
@RequiredArgsConstructor
public class PaiResumeWechatQrGatewayController {
    private static final int MAX_BODY_BYTES = 4096;

    private final PaiResumeWechatBridgeProperties properties;
    private final PaiResumeWechatBridgeSigner signer;
    private final WxLoginQrGenIntegration qrGenIntegration;
    private final ObjectMapper objectMapper;

    @PostMapping("/qrcodes")
    @ResponseStatus(HttpStatus.OK)
    public ResVo<Map<String, Object>> createQr(
            @RequestHeader("X-Pai-Timestamp") String timestamp,
            @RequestHeader("X-Pai-Nonce") String nonce,
            @RequestHeader("X-Pai-Signature") String signature,
            @RequestBody byte[] rawBody) {
        properties.requireReady();
        verifyRequest(timestamp, nonce, signature, rawBody);

        final QrRequest request;
        try {
            request = objectMapper.readValue(rawBody, QrRequest.class);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request body");
        }
        String scene = request.getScene();
        Integer expireSeconds = request.getExpireSeconds();
        if (StringUtils.isBlank(scene) || scene.length() > 64
                || !scene.startsWith(properties.getScenePrefix())
                || !scene.matches("[A-Za-z0-9_-]{8,64}")
                || expireSeconds == null || expireSeconds < 60 || expireSeconds > 600) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid QR request");
        }

        String qrImageDataUrl = qrGenIntegration.genTemporarySceneQrImg(scene, expireSeconds);
        Map<String, Object> data = new HashMap<>();
        data.put("qrImageDataUrl", qrImageDataUrl);
        data.put("expiresIn", expireSeconds);
        return ResVo.ok(data);
    }

    private void verifyRequest(String timestamp, String nonce, String signature, byte[] rawBody) {
        if (rawBody == null || rawBody.length == 0 || rawBody.length > MAX_BODY_BYTES
                || StringUtils.isBlank(timestamp)
                || StringUtils.isBlank(nonce) || !nonce.matches("[A-Za-z0-9_-]{16,64}")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid signature context");
        }
        final long epoch;
        try {
            epoch = Long.parseLong(timestamp);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid timestamp");
        }
        if (Math.abs(Instant.now().getEpochSecond() - epoch) > properties.getClockSkewSeconds()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "expired request");
        }
        String expected = signer.sign(properties.getBridgeSecret(), timestamp, nonce, rawBody);
        if (!signer.matches(expected, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid signature");
        }
        String replayKey = "wx:pairesume:qr:replay:"
                + signer.sha256(timestamp + "\n" + nonce);
        Boolean fresh = RedisClient.setStrIfAbsentWithExpire(
                replayKey, "1", (long) properties.getReplayTtlSeconds());
        if (!Boolean.TRUE.equals(fresh)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "replayed request");
        }
    }

    @Data
    private static class QrRequest {
        private String scene;
        private Integer expireSeconds;
    }
}
