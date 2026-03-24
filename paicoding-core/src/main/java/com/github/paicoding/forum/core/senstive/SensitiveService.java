package com.github.paicoding.forum.core.senstive;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllowSystem;
import com.github.houbb.sensitive.word.support.deny.WordDenySystem;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.cache.RedisClient;
import lombok.extern.slf4j.Slf4j;
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
    /**
     * 敏感词命中计数统计
     */
    private static final String SENSITIVE_WORD_CNT_PREFIX = "sensitive_word";
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
        return replaceInternal(txt, false);
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
        return replaceInternal(txt, true);
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

    private String replaceInternal(String txt, boolean recordHit) {
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
}
