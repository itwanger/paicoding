package com.github.paicoding.forum.web.front.login.wx.callback;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class WxCallbackSignatureTest {

    @Test
    void shouldSortWechatCallbackSignaturePartsLexicographically() {
        assertEquals(
                "bf37e74fc61ce5974ce58c68e55130b79b2578b9",
                WxCallbackRestController.computeWechatSignature(
                        "token", "1700000000", "nonce")
        );
        assertNotEquals(
                WxCallbackRestController.computeWechatSignature(
                        "token", "1700000000", "nonce"),
                WxCallbackRestController.computeWechatSignature(
                        "token", "1700000001", "nonce")
        );
    }
}
