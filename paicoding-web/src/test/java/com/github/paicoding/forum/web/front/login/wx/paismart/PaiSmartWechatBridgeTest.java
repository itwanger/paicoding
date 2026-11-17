package com.github.paicoding.forum.web.front.login.wx.paismart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.web.front.login.wx.config.WxLoginProperties;
import com.github.paicoding.forum.web.front.login.wx.pairesume.PaiResumeWechatBridgeSigner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaiSmartWechatBridgeTest {
    @Test void onlyRoutesEnabledPaiSmartScenes() {
        WxLoginProperties wx = new WxLoginProperties();
        PaiSmartWechatBridgeProperties config = new PaiSmartWechatBridgeProperties(wx);
        PaiSmartWechatBridgeClient client = new PaiSmartWechatBridgeClient(config, wx,
                new PaiResumeWechatBridgeSigner(), new ObjectMapper());
        assertFalse(client.isPaiSmartScene("ps_test"));
        config.setEnabled(true);
        assertTrue(client.isPaiSmartScene("ps_test"));
        assertFalse(client.isPaiSmartScene("pr_test"));
        assertFalse(client.isPaiSmartScene(null));
        assertFalse(client.isPaiSmartScene("normal-login"));
    }

    @Test void refusesUnsignedOrIncompleteConfiguration() {
        WxLoginProperties wx = new WxLoginProperties();
        PaiSmartWechatBridgeProperties config = new PaiSmartWechatBridgeProperties(wx);
        config.setEnabled(true);
        config.setCallbackUrl("https://example.test/api/v1/auth/wechat/events");
        assertThrows(IllegalStateException.class, config::requireReady);
        config.setBridgeSecret("test-only-key-which-is-long-enough");
        wx.setAppId("test-app");
        // Even with a bridge signature, incoming WeChat events must be authenticated.
        assertThrows(IllegalStateException.class, config::requireReady);
    }
}
