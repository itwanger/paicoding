package com.github.paicoding.forum.web.front.login.wx.pairesume;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaiResumeWechatBridgeSignerTest {

    private final PaiResumeWechatBridgeSigner signer = new PaiResumeWechatBridgeSigner();

    @Test
    void shouldSignTimestampNonceAndExactRawBody() {
        byte[] body = ("{\"scene\":\"pr_L_abcdefghijklmnopqrstuvwxyzABCDEFG1234567890\","
                + "\"expireSeconds\":300}").getBytes(StandardCharsets.UTF_8);

        String signature = signer.sign(
                "0123456789abcdef0123456789abcdef",
                "1700000000",
                "nonce_1234567890",
                body
        );

        assertEquals("719afdfb0339e654336b66f7cd5988e0c29b1ccb940a467f40a2428f5882c090",
                signature);
        assertTrue(signer.matches(signature, signature.toUpperCase()));
        assertFalse(signer.matches(signature, signature.substring(2) + "00"));
    }
}
