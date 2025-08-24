package com.github.paicoding.forum.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 星球编号工具类
 *
 * @author YiHui
 * @date 2025/8/24
 */
public class StarNumberUtil {

    /**
     * 将星球编号格式化为5位数字符串，不足位数的前面补0
     *
     * @param starNumber 星球编号
     * @return 格式化后的5位数字符串
     */
    public static String formatStarNumber(String starNumber) {
        if (StringUtils.isBlank(starNumber)) {
            return "";
        }
        
        // 移除可能存在的前导零，然后重新格式化
        try {
            // 解析为整数以去除前导零
            int number = Integer.parseInt(starNumber);
            // 格式化为5位数字符串
            return String.format("%05d", number);
        } catch (NumberFormatException e) {
            // 如果不是有效数字，直接返回原值
            return starNumber;
        }
    }
}