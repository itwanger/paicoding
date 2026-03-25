package com.github.paicoding.forum.core.senstive;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllowSystem;
import com.github.houbb.sensitive.word.support.deny.WordDenySystem;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.cache.RedisClient;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感词服务类
 *
 * @author YiHui
 * @date 2023/8/9
 */
@Slf4j
@Service
public class SensitiveService {
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s)\\]>'\"]+");
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    /**
     * 敏感词命中计数统计
     */
    private static final String SENSITIVE_WORD_CNT_PREFIX = "sensitive_word";
    private static final HanyuPinyinOutputFormat PINYIN_FORMAT = buildPinyinFormat();
    private volatile SensitiveWordBs sensitiveWordBs;
    @Autowired
    private SensitiveProperty sensitiveConfig;
    @Autowired
    private DynamicConfigContainer dynamicConfigContainer;

    @PostConstruct
    public void refresh() {
        dynamicConfigContainer.registerRefreshCallback(sensitiveConfig, this::refresh);
        IWordDeny deny = () -> {
            List<String> sub = WordDenySystem.getInstance().deny();
            sub.addAll(sensitiveConfig.getDeny());
            return sub;
        };

        IWordAllow allow = () -> {
            List<String> sub = WordAllowSystem.getInstance().allow();
            sub.addAll(sensitiveConfig.getAllow());
            return sub;
        };
        sensitiveWordBs = SensitiveWordBs.newInstance()
                .wordDeny(deny)
                .wordAllow(allow)
                .init();
        log.info("敏感词初始化完成!");
    }

    /**
     * 判断是否包含敏感词
     *
     * @param txt 需要校验的文本
     * @return 返回命中的敏感词
     */
    public List<String> contains(String txt) {
        if (!BooleanUtils.isTrue(sensitiveConfig.getEnable()) || txt == null) {
            return Collections.emptyList();
        }

        String protectedText = protectUrls(txt, new LinkedHashMap<>());
        List<String> ans = sensitiveWordBs.findAll(protectedText);
        recordHits(ans);
        return ans;
    }


    /**
     * 返回已命中的敏感词
     *
     * @return key: 敏感词， value：计数（按命中次数降序排序）
     */
    public Map<String, Integer> getHitSensitiveWords() {
        Map<String, Integer> hitWords = RedisClient.hGetAll(SENSITIVE_WORD_CNT_PREFIX, Integer.class);

        if (CollectionUtils.isEmpty(hitWords)) {
            return hitWords;
        }

        // 按 value（命中次数）降序排序
        return hitWords.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 移除敏感词
     *
     * @param word
     */
    public void removeSensitiveWord(String word) {
        RedisClient.hDel(SENSITIVE_WORD_CNT_PREFIX, word);
    }

    /**
     * 敏感词替换
     *
     * @param txt
     * @return
     */
    public String replace(String txt) {
        if (!BooleanUtils.isTrue(sensitiveConfig.getEnable()) || txt == null) {
            return txt;
        }
        return replaceFriendlyInternal(txt);
    }

    /**
     * 敏感词替换，并记录命中统计
     *
     * @param txt 原始文本
     * @return 替换后的文本
     */
    public String replaceAndCount(String txt) {
        if (!BooleanUtils.isTrue(sensitiveConfig.getEnable()) || txt == null) {
            return txt;
        }
        return replaceWithDefaultMask(txt, true);
    }

    /**
     * 查询文本中所有命中的敏感词
     *
     * @param txt 校验文本
     * @return 命中的敏感词
     */
    public List<String> findAll(String txt) {
        if (txt == null) {
            return Collections.emptyList();
        }
        return sensitiveWordBs.findAll(protectUrls(txt, new LinkedHashMap<>()));
    }

    private String replaceFriendlyInternal(String txt) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        String protectedText = protectUrls(txt, placeholders);
        List<String> hitWords = sensitiveWordBs.findAll(protectedText);
        if (CollectionUtils.isEmpty(hitWords)) {
            return txt;
        }
        String replaced = replaceHitWords(protectedText, hitWords);
        return restorePlaceholders(replaced, placeholders);
    }

    private String replaceWithDefaultMask(String txt, boolean recordHit) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        String protectedText = protectUrls(txt, placeholders);
        if (recordHit) {
            recordHits(sensitiveWordBs.findAll(protectedText));
        }
        String replaced = sensitiveWordBs.replace(protectedText);
        return restorePlaceholders(replaced, placeholders);
    }

    private void recordHits(List<String> words) {
        if (CollectionUtils.isEmpty(words)) {
            return;
        }

        RedisClient.PipelineAction action = RedisClient.pipelineAction();
        words.forEach(key -> action.add(SENSITIVE_WORD_CNT_PREFIX, key, (connection, k, v) -> connection.hIncrBy(k, v, 1)));
        action.execute();
    }

    private String protectUrls(String txt, Map<String, String> placeholders) {
        if (txt == null || txt.isEmpty()) {
            return txt;
        }

        Matcher matcher = URL_PATTERN.matcher(txt);
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        while (matcher.find()) {
            String token = "__PAI_URL_" + index++ + "__";
            placeholders.put(token, matcher.group());
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(token));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String restorePlaceholders(String txt, Map<String, String> placeholders) {
        if (txt == null || placeholders.isEmpty()) {
            return txt;
        }

        String restored = txt;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            restored = restored.replace(entry.getKey(), entry.getValue());
        }
        return restored;
    }

    private String replaceHitWords(String txt, List<String> hitWords) {
        String replaced = txt;
        List<String> orderedWords = hitWords.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());
        for (String word : orderedWords) {
            String friendlyWord = toFriendlyReplacement(word);
            if (StringUtils.equals(word, friendlyWord)) {
                continue;
            }
            replaced = replaced.replace(word, friendlyWord);
        }
        return replaced;
    }

    private String toFriendlyReplacement(String word) {
        if (StringUtils.isBlank(word)) {
            return word;
        }
        if (CHINESE_PATTERN.matcher(word).find()) {
            String pinyin = toPinyin(word);
            if (StringUtils.isNotBlank(pinyin) && !StringUtils.equalsIgnoreCase(word, pinyin)) {
                return pinyin;
            }
        }
        return toFullWidth(word);
    }

    private String toPinyin(String word) {
        StringBuilder builder = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (isChinese(c)) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, PINYIN_FORMAT);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        builder.append(pinyinArray[0]);
                        continue;
                    }
                } catch (Exception e) {
                    log.debug("敏感词转拼音失败, char={}", c, e);
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private String toFullWidth(String word) {
        StringBuilder builder = new StringBuilder(word.length());
        for (char c : word.toCharArray()) {
            if (c == ' ') {
                builder.append('\u3000');
            } else if (c >= 33 && c <= 126) {
                builder.append((char) (c + 65248));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fa5';
    }

    private static HanyuPinyinOutputFormat buildPinyinFormat() {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        return format;
    }
}
