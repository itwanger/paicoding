package com.github.paicoding.forum.core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用常量
 *
 * @author Louzai
 * @date 2022/11/1
 */
public class CommonConstants {

    /**
     * 消息队列
     */
    public static String EXCHANGE_NAME_DIRECT = "direct.exchange";
    public static String QUERE_KEY_PRAISE = "praise";
    public static String QUERE_NAME_PRAISE = "quere.praise";

    /**
     * 分类类型
     */
    public static final String CATEGORY_ALL             = "全部";
    public static final String CATEGORY_BACK_EMD        = "后端";
    public static final String CATEGORY_FORNT_END       = "前端";
    public static final String CATEGORY_ANDROID         = "Android";
    public static final String CATEGORY_IOS             = "IOS";
    public static final String CATEGORY_BIG_DATA        = "大数据";
    public static final String CATEGORY_INTELLIGENCE    = "人工智能";
    public static final String CATEGORY_CODE_LIFE       = "代码人生";
    public static final String CATEGORY_TOOL            = "开发工具";
    public static final String CATEGORY_READ            = "阅读";

    /**
     * 首页图片
     */
    public static final Map<String, List<String>> HOMEPAGE_TOP_PIC_MAP = new HashMap<String, List<String>>() {
        {
            put(CATEGORY_ALL, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/8b5865e9461948aed4aacffc62adbae7.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/71505c62fb6375cbbd62af63964b2ad4.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/147915cdddea55ce37c2c5ecfc7c089e.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/dee73c8810cb699ae1ec774a54612080.jpg");
                }
            });
            put(CATEGORY_BACK_EMD, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/b2af54aefab4fbf8f001065961a8118b.gif");
                    add("https://cdn.tobebetterjavaer.com/paicoding/99ca3b142d901d9ca63946efca0122d8.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/cfbdbd36b2194dd4fd9da2ea18e8a56a.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/f693ed4c1969724a44004a96fcce4263.jpg");
                }
            });
            put(CATEGORY_FORNT_END, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/7c591a44a9f83eec9606d16f89040632.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/4eb9f0bad37ba5903ca9e249743e3ecb.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/6a810a33480fc3b42740713a6687f3fd.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/6ed5aa120b7a06a5f4b5fe351adfda94.jpg");
                }
            });
            put(CATEGORY_ANDROID, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/f266aabeb976b2b9c4bf24a107a78c5d.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/dee27ae91078714cc9f6b1774161c1ef.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/a8cfe8140b683809a68205da76e77fb1.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/b964b76b111cf36602d9b2dc30bee9ee.jpg");
                }
            });
            put(CATEGORY_IOS, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/7edcb3dd19d4d517be34a30bc082338d.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/ee2a3fec62d85df3b1c27908d53698c5.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/f476fbc0cf90ed81802ef6a1d51fcf16.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/bfc7cbdeb928e03d034be1f9d73f4a9e.jpg");
                }
            });
            put(CATEGORY_BIG_DATA, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/989d3c1f73ee953a05347c0b99cba46d.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/a96fe34a09d9fd9cafd64eae90410428.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/1217d3dd677d91cb65b0cc85769f7f3d.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/5de37f5c879543bd17f031f4243fae7d.jpg");
                }
            });
            put(CATEGORY_INTELLIGENCE, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/077b7d8891e69701e8d3d4302392dab5.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/be98a2779d2dde96c40092bbae958864.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/d368d4bed7daf51116f4defbb4afcb6d.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/c6bfe6bea326a64a267520ba7cada539.jpg");
                }
            });
            put(CATEGORY_CODE_LIFE, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/077b7d8891e69701e8d3d4302392dab5.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/be98a2779d2dde96c40092bbae958864.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/d368d4bed7daf51116f4defbb4afcb6d.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/c6bfe6bea326a64a267520ba7cada539.jpg");
                }
            });
            put(CATEGORY_TOOL, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/53de05a01c7246feadffb6ba24120416.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/e3e1f7a729d5cfbde0e5373c2d61377a.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/326585eab30c33cdfa1cc058b269bf5a.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/870997d1442e4d67186bf0dbc52e2096.jpg");
                }
            });
            put(CATEGORY_READ, new ArrayList<String>() {
                {
                    add("https://cdn.tobebetterjavaer.com/paicoding/dd3f3e90b666cfe65f4ca5e56ebfc9f8.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/7c591a44a9f83eec9606d16f89040632.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/42dbb88fed8caa2d95860dbdb359c18f.jpg");
                    add("https://cdn.tobebetterjavaer.com/paicoding/b99cab7999d5bdf3f5926dc0a98d02da.jpg");
                }
            });
        }
    };
}
