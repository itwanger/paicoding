package com.github.liuyueyi.forum.web.front.login;

import com.github.liuyueyi.forum.core.util.JsonUtil;
import com.github.liuyueyi.forum.core.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YiHui
 * @date 2022/9/5
 */
@Slf4j
@Component
public class WxHelper {
    public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx4a128c315d9b1228&secret=077e2d92dee69f04ba6d53a0ef4459f9";

    public static final String QR_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=";
    /**
     * 访问token
     */
    public static volatile String token = "";

    /**
     * 失效时间
     */
    public static volatile long expireTime = 0L;


    private RestTemplate restTemplate;

    public WxHelper() {
        restTemplate = new RestTemplate();
    }

    private synchronized void doGetToken() {
        ResponseEntity<HashMap> entity = restTemplate.getForEntity(ACCESS_TOKEN_URL, HashMap.class);
        HashMap data = entity.getBody();
        log.info("getToken:{}", JsonUtil.toStr(entity));
        token = (String) data.get("access_token");
        int expire = (int) data.get("expires_in");
        // 提前至十分钟失效
        expireTime = System.currentTimeMillis() / 1000 + expire - 600;
    }

    public String autoUpdateAccessToken() {
        if (StringUtils.isBlank(token) || System.currentTimeMillis() / 1000 >= expireTime) {
            doGetToken();
        }
        return token;
    }

    /**
     * 获取带参数的登录二维码地址
     *
     * @param code
     * @return
     */
    public String getLoginQrCode(String code) {
        String url = QR_CREATE_URL + autoUpdateAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> params = MapUtils.create("action_name", "QR_LIMIT_SCENE",
                "expire_seconds", 300,
                "action_info", MapUtils.create("scene", MapUtils.create("scene_str", code)));
        HttpEntity<String> request = new HttpEntity<>(JsonUtil.toStr(params), headers);

        Map ans = restTemplate.postForObject(url, request, HashMap.class);
        String qrcode = (String) ans.get("url");
        return qrcode;
    }


}
