package com.github.paicoding.forum.core.util;

import org.apache.commons.lang3.math.NumberUtils;
import com.github.paicoding.forum.api.model.enums.login.LoginQrTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author YiHui
 * @date 2022/8/15
 */
public class CodeGenerateUtil {
    public static final Integer CODE_LEN = 3;

    private static final Random random = new Random();

    // 订阅号使用的特殊验证码列表（用户手动输入）
    private static final List<String> specialCodes = Arrays.asList(
            "666", "888", "000", "999", "555", "222", "333", "777",
            "520", "911",
            "234", "345", "456", "567", "678", "789"
    );

    /**
     * 根据登录类型生成验证码
     *
     * @param cnt 计数器（订阅号时使用）
     * @param loginType 登录类型
     * @return 验证码
     */
    public static String genCode(int cnt, LoginQrTypeEnum loginType) {
        if (loginType == LoginQrTypeEnum.SERVICE_ACCOUNT) {
            // 服务号：生成随机验证码，不使用specialCodes
            return genRandomCode();
        } else {
            // 订阅号：使用specialCodes列表
            return genSpecialCode(cnt);
        }
    }

    /**
     * 兼容性方法，默认使用订阅号方式
     */
    public static String genCode(int cnt) {
        return genSpecialCode(cnt);
    }

    /**
     * 生成订阅号专用的特殊验证码
     */
    private static String genSpecialCode(int cnt) {
        if (cnt >= specialCodes.size()) {
            int num = random.nextInt(1000);
            if (num >= 100 && num <= 200) {
                // 100-200之间的数字作为关键词回复，不用于验证码
                return genSpecialCode(cnt);
            }
            return String.format("%0" + CODE_LEN + "d", num);
        } else {
            return specialCodes.get(cnt);
        }
    }

    /**
     * 生成服务号专用的随机验证码
     */
    private static String genRandomCode() {
        // 使用UUID的哈希码作为随机源，确保唯一性
        int hashCode = Math.abs(UUID.randomUUID().hashCode());
        // 生成3位数字，避免100-200（保留用于关键词回复）
        int num = hashCode % 900 + 100; // 100-999

        // 如果落在101-200范围内，调整到其他范围
        if (num > 100 && num <= 200) {
            num = ((num - 101) % 700) + 201; // 201-999
        }

        return String.format("%0" + CODE_LEN + "d", num);
    }

    public static boolean isVerifyCode(String content) {
        if (!NumberUtils.isDigits(content) || content.length() != CodeGenerateUtil.CODE_LEN) {
            return false;
        }

        int num = Integer.parseInt(content);
        return num < 100 || num > 200;
    }
}
