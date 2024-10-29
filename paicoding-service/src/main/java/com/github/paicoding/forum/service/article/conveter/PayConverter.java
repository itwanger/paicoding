package com.github.paicoding.forum.service.article.conveter;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.hui.quick.plugin.base.Base64Util;
import com.github.hui.quick.plugin.qrcode.wrapper.QrCodeGenV3;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticlePayInfoDTO;
import com.github.paicoding.forum.core.util.JsonUtil;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YiHui
 * @date 2024/10/29
 */
public class PayConverter {

    public static ArticlePayInfoDTO toPay(ArticlePayRecordDO record) {
        ArticlePayInfoDTO info = new ArticlePayInfoDTO();
        info.setPayId(record.getId());
        info.setPayUserId(record.getPayUserId());
        info.setPayStatus(record.getPayStatus());
        info.setReceiveUserId(record.getReceiveUserId());
        return info;
    }


    /**
     * 格式化收款码
     *
     * @return key: 渠道   value: 收款二维码base64格式
     */
    public static Map<String, String> formatPayCode(String dbCode) {
        JsonNode node = JsonUtil.toNode(dbCode);
        Map<String, String> result = new HashMap<>();
        node.fields().forEachRemaining(kv -> {
            String key = kv.getKey();
            String value = kv.getValue().asText();
            result.put(key, genQrCode(value));
        });
        return result;
    }

    private static String genQrCode(String txt) {
        try {
            BufferedImage img = QrCodeGenV3.of(txt).setSize(500).asImg();
            return Base64Util.encode(img, "png");
        } catch (Exception e) {
            return txt;
        }
    }
}
