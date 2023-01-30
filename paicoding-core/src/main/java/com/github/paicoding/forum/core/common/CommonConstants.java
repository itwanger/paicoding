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

    // 分类类型
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

    // 首页图片
    public static final Map<String, List<String>> HOMEPAGE_TOP_PIC_MAP = new HashMap<String, List<String>>() {
        {
            put(CATEGORY_ALL, new ArrayList<String>() {
                {
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/130773/36/28029/41932/63606c72E8e68370b/72f322b90eb6f146.webp");
                    add("https://img13.360buyimg.com/ddimg/jfs/t1/216869/6/23184/32776/63606c72E8edb111e/a4de234440299448.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/182698/4/29623/19646/636255b3Ecb930e42/00606f148e71d345.webp");
                    add("https://img12.360buyimg.com/ddimg/jfs/t1/169217/34/31577/31440/63606c72E7a4f58b3/a72b7df2ec84d79f.webp");
                }
            });
            put(CATEGORY_BACK_EMD, new ArrayList<String>() {
                {
                    add("https://img13.360buyimg.com/ddimg/jfs/t1/72497/8/23472/367421/63b3e036F68e9048e/ace40ac4067dff63.gif");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/138738/8/31452/12286/636076f0Ef18f77d8/fde4de8bbf0b5380.webp");
                    add("https://img12.360buyimg.com/ddimg/jfs/t1/69814/7/22820/21218/636076f0Ed6dea845/f53aaf828ec9c135.webp");
                    add("https://img12.360buyimg.com/ddimg/jfs/t1/23649/3/19923/65450/636076f0Ec39e3fd9/763afa10946ef984.webp");
                }
            });
            put(CATEGORY_FORNT_END, new ArrayList<String>() {
                {
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/77817/10/17147/40054/6360781bE6204777c/961a0321b18743e9.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/83336/15/23862/19608/6360781bEbf5c07c0/9c6244f0b7647c1f.webp");
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/204452/27/27780/19702/6360781bE2b6144db/9bf09b93cc4568a2.webp");
                    add("https://img12.360buyimg.com/ddimg/jfs/t1/181696/4/29961/41932/6360781bE66fc1f29/083e3d75d28f6079.webp");
                }
            });
            put(CATEGORY_ANDROID, new ArrayList<String>() {
                {
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/61845/7/21525/13468/6360774fEade81f71/9c62da060491730d.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/185329/13/28290/26170/6360774fEe80c6347/92e939938b16c8d3.webp");
                    add("https://img13.360buyimg.com/ddimg/jfs/t1/208391/40/27231/20626/6360774fE218bb226/100d4e4949a132a6.webp");
                    add("https://img10.360buyimg.com/ddimg/jfs/t1/98090/38/34283/20116/6360774fE993d3130/b9cfd877e0aa6f5b.webp");
                }
            });
            put(CATEGORY_IOS, new ArrayList<String>() {
                {
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/120629/7/31346/24502/636077b3E09ea8380/2f8c9dde9adba631.webp");
                    add("https://img10.360buyimg.com/ddimg/jfs/t1/129648/9/27996/14852/636077b3Ec4e1afe0/d355b387fa4ad9c2.webp");
                    add("https://img10.360buyimg.com/ddimg/jfs/t1/14969/10/19903/45082/636077b3Ef1327489/8d0dc76011eb1ad3.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/107092/29/20068/17118/636077beEfe577d01/af720e6202441018.webp");
                }
            });
            put(CATEGORY_BIG_DATA, new ArrayList<String>() {
                {
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/143513/1/30950/11464/6360767bEada4a5db/eb24093e34dd6991.webp");
                    add("https://img12.360buyimg.com/ddimg/jfs/t1/100700/30/30797/18850/6360767bE752105f6/df6b065028fd0344.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/177012/28/29445/19784/6360767bE4a30481e/fecfcd7cfd87c692.webp");
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/32179/19/18630/11418/6360767fEfb0a2215/299fe71933c4427b.webp");
                }
            });
            put(CATEGORY_INTELLIGENCE, new ArrayList<String>() {
                {
                    add("https://img13.360buyimg.com/ddimg/jfs/t1/125933/19/32258/20996/636078a9E8e5bf167/921be7642049dbfe.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/140072/31/31331/14532/636078a9Ef15048e9/9b1e75d21ef18cc5.webp");
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/34445/1/19974/12508/636078a9Ec55907bb/8b6c840c56b56a2b.webp");
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/84474/1/22704/18326/636078a9E843708b9/646433e3748bae7c.webp");
                }
            });
            put(CATEGORY_CODE_LIFE, new ArrayList<String>() {
                {
                    add("https://img13.360buyimg.com/ddimg/jfs/t1/125933/19/32258/20996/636078a9E8e5bf167/921be7642049dbfe.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/140072/31/31331/14532/636078a9Ef15048e9/9b1e75d21ef18cc5.webp");
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/34445/1/19974/12508/636078a9Ec55907bb/8b6c840c56b56a2b.webp");
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/84474/1/22704/18326/636078a9E843708b9/646433e3748bae7c.webp");
                }
            });
            put(CATEGORY_TOOL, new ArrayList<String>() {
                {
                    add("https://img12.360buyimg.com/ddimg/jfs/t1/179634/18/29011/10722/63607a0eEd059ccf0/b2859f8c84e8a4d2.webp");
                    add("https://img12.360buyimg.com/ddimg/jfs/t1/209419/11/27128/9162/63607a0eE4047c45e/448937c41a936417.webp");
                    add("https://img11.360buyimg.com/ddimg/jfs/t1/97611/4/30567/14094/63607a0eE24ec1bb8/95533f37cf2490f4.webp");
                    add("https://img14.360buyimg.com/ddimg/jfs/t1/71725/13/22737/10122/63607a0eE386bf3be/6caff5dd52f91c6e.webp");
                }
            });
            put(CATEGORY_READ, new ArrayList<String>() {
                {
                    add("https://img10.360buyimg.com/ddimg/jfs/t1/87165/1/30748/18446/63607a5cEcab43669/0afae1d1029b603d.webp");
                    add("https://img10.360buyimg.com/ddimg/jfs/t1/222361/23/20018/40054/63607a5cE90808737/1cdf8a6f8a029c56.webp");
                    add("https://img13.360buyimg.com/ddimg/jfs/t1/155739/26/31985/25138/63607a5cE5fce58a9/1e189a3016167a01.webp");
                    add("https://img13.360buyimg.com/ddimg/jfs/t1/207873/15/27877/35772/63607a5cE09ee1612/52bc716f2341cc22.webp");
                }
            });
        }
    };
}
