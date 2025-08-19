package com.github.paicoding.forum.web.front.login.zsxq.helper;

import cn.hutool.core.net.URLEncodeUtil;
import com.github.paicoding.forum.web.front.login.zsxq.config.ZsxqProperties;
import com.github.paicoding.forum.web.front.login.zsxq.vo.ZsxqLoginVo;
import org.bouncycastle.jcajce.provider.digest.SHA1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * 知识星球登录相关类
 *
 * @author YiHui
 * @date 2025/8/19
 */
@Component
public class ZsxqHelper {
    @Autowired
    private ZsxqProperties zsxqProperties;

    public String buildZsxqLoginUrl(String type) {
        StringBuilder builder = new StringBuilder();
        builder.append("app_id=").append(zsxqProperties.getAppId());
        builder.append("&extra=").append(type);
        builder.append("&group_number=").append(zsxqProperties.getGroupNumber());
        builder.append("&redirect_url=").append(URLEncodeUtil.encodeAll(zsxqProperties.getRedirectUrl()));
        builder.append("&timestamp=").append(System.currentTimeMillis() / 1000L);

        String toSignParam = builder + "&secret=" + zsxqProperties.getSecret();
        // 请求参数签名
        String sign = sha1(toSignParam);
        builder.append("&signature=").append(sign);
        return zsxqProperties.getApi() + "?" + builder;
    }

    /**
     * 使用SHA1算法对输入字符串进行摘要计算
     *
     * @param input 需要进行摘要计算的字符串
     * @return SHA1摘要结果（十六进制字符串）
     */
    private String sha1(String input) {
        SHA1.Digest digest = new SHA1.Digest();
        byte[] result = digest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public boolean verifySignature(ZsxqLoginVo vo) {
        // 校验签名，首先使用Map来承接请求参数，key为参数名称（驼峰转下划线），value为参数值
        // 根据Map的key 按照 ascii 排序，然后拼接成字符串，最后进行sha1加密，与signature进行对比
        Map<String, Object> params = new TreeMap<>();
        params.put("app_id", vo.getApp_id());
        params.put("group_number", vo.getGroup_number());
        params.put("user_id", vo.getUser_id());
        params.put("user_name", URLEncodeUtil.encodeAll(vo.getUser_name()));
        params.put("user_number", vo.getUser_number());
        params.put("user_icon", URLEncodeUtil.encodeAll(vo.getUser_icon()));
        params.put("user_role", vo.getUser_role());
        params.put("extra", vo.getExtra());
        params.put("join_time", vo.getJoin_time());
        params.put("expire_time", vo.getExpire_time());
        params.put("timestamp", vo.getTimestamp());

        // 拼接参数字符串
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        // 添加secret
        String toSignParam = sb + "&secret=" + zsxqProperties.getSecret();

        // 计算签名
        String sign = sha1(toSignParam);

        // 比较签名
        return sign.equals(vo.getSignature());
    }
}
