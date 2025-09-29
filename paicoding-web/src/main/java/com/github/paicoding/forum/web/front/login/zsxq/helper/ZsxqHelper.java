package com.github.paicoding.forum.web.front.login.zsxq.helper;

import cn.hutool.core.net.URLEncodeUtil;
import com.github.paicoding.forum.web.front.login.zsxq.config.ZsxqProperties;
import com.github.paicoding.forum.web.front.login.zsxq.vo.ZsxqLoginVo;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class ZsxqHelper {
    public static final String EXTRA_TAG_USER_TRANSFER = "zsxqUserTransfer";

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

        log.debug("开始验证知识星球签名，用户名: [{}], 头像: [{}]", vo.getUser_name(), vo.getUser_icon());

        // 方案1: 不进行额外编码
        boolean verified = verifySignatureWithoutEncoding(vo);
        log.debug("方案1验证结果（不编码）: {}", verified);

        // 方案2: 使用UTF-8编码
        if (!verified) {
            verified = verifySignatureWithUTF8Encoding(vo);
            log.debug("方案2验证结果（UTF-8编码）: {}", verified);
        }

        // 方案3: 使用空格转+号的方式编码（知识星球可能使用这种方式）
        if (!verified) {
            verified = verifySignatureWithSpacePlusEncoding(vo);
            log.debug("方案3验证结果（空格转+号编码）: {}", verified);
        }

        if (!verified) {
            log.warn("知识星球签名验证失败，用户名: [{}], 预期签名: [{}]", vo.getUser_name(), vo.getSignature());
        } else {
            log.info("知识星球签名验证成功，用户名: [{}]", vo.getUser_name());
        }

        return verified;
    }

    /**
     * 方案1: 不对user_name和user_icon进行额外编码
     */
    private boolean verifySignatureWithoutEncoding(ZsxqLoginVo vo) {
        Map<String, Object> params = buildSignatureParams(vo, false);
        return computeAndVerifySignature(params, vo.getSignature());
    }

    /**
     * 方案2: 使用UTF-8编码（备用方案）
     */
    private boolean verifySignatureWithUTF8Encoding(ZsxqLoginVo vo) {
        Map<String, Object> params = buildSignatureParams(vo, true);
        return computeAndVerifySignature(params, vo.getSignature());
    }

    /**
     * 方案3: 使用空格转+号的方式编码（模拟知识星球的编码方式）
     */
    private boolean verifySignatureWithSpacePlusEncoding(ZsxqLoginVo vo) {
        Map<String, Object> params = buildSignatureParamsWithSpacePlus(vo);
        return computeAndVerifySignature(params, vo.getSignature());
    }

    /**
     * 构建签名参数（空格转+号编码版本）
     */
    private Map<String, Object> buildSignatureParamsWithSpacePlus(ZsxqLoginVo vo) {
        Map<String, Object> params = new TreeMap<>();
        params.put("app_id", vo.getApp_id());
        params.put("group_number", vo.getGroup_number());
        params.put("user_id", vo.getUser_id());

        // 使用特殊的编码方式：空格转+号，其他字符进行UTF-8编码
        params.put("user_name", vo.getUser_name() != null ? encodeWithSpacePlus(vo.getUser_name()) : null);
        params.put("user_icon", vo.getUser_icon() != null ? encodeWithSpacePlus(vo.getUser_icon()) : null);

        params.put("user_number", vo.getUser_number());
        params.put("user_role", vo.getUser_role());
        params.put("extra", vo.getExtra());
        params.put("join_time", vo.getJoin_time());
        params.put("expire_time", vo.getExpire_time());
        params.put("timestamp", vo.getTimestamp());

        return params;
    }

    /**
     * 特殊编码：先进行UTF-8编码，然后将%20替换为+
     */
    private String encodeWithSpacePlus(String input) {
        if (input == null) {
            return null;
        }
        // 先进行标准URL编码
        String encoded = URLEncodeUtil.encodeAll(input);
        // 将%20（空格的URL编码）替换为+号
        return encoded.replace("%20", "+");
    }

    /**
     * 构建签名参数
     */
    private Map<String, Object> buildSignatureParams(ZsxqLoginVo vo, boolean needEncoding) {
        Map<String, Object> params = new TreeMap<>();
        params.put("app_id", vo.getApp_id());
        params.put("group_number", vo.getGroup_number());
        params.put("user_id", vo.getUser_id());

        // 根据needEncoding参数决定是否编码
        if (needEncoding) {
            params.put("user_name", vo.getUser_name() != null ? URLEncodeUtil.encodeAll(vo.getUser_name()) : null);
            params.put("user_icon", vo.getUser_icon() != null ? URLEncodeUtil.encodeAll(vo.getUser_icon()) : null);
        } else {
            params.put("user_name", vo.getUser_name());
            params.put("user_icon", vo.getUser_icon());
        }

        params.put("user_number", vo.getUser_number());
        params.put("user_role", vo.getUser_role());
        params.put("extra", vo.getExtra());
        params.put("join_time", vo.getJoin_time());
        params.put("expire_time", vo.getExpire_time());
        params.put("timestamp", vo.getTimestamp());

        return params;
    }

    /**
     * 计算并验证签名
     */
    private boolean computeAndVerifySignature(Map<String, Object> params, String expectedSignature) {
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

        // 调试日志
        log.debug("签名参数字符串: [{}]", toSignParam.replaceAll("&secret=.*", "&secret=***"));
        log.debug("计算得到的签名: [{}], 预期签名: [{}]", sign, expectedSignature);

        // 比较签名
        return sign.equals(expectedSignature);
    }
}
