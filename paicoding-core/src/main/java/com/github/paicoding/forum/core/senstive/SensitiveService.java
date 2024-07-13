package com.github.paicoding.forum.core.senstive;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllowSystem;
import com.github.houbb.sensitive.word.support.deny.WordDenySystem;
import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.cache.RedisClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 敏感词服务类
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class SensitiveService {
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
        if (!BooleanUtils.isTrue(sensitiveConfig.getEnable())) {
            return Collections.emptyList();
        }

        List<String> ans = sensitiveWordBs.findAll(txt);
        if (CollectionUtils.isEmpty(ans)) {
            return ans;
        }

        // 敏感词命中次数+1
        RedisClient.PipelineAction action = RedisClient.pipelineAction();
        ans.forEach(key -> action.add(SENSITIVE_WORD_CNT_PREFIX, key, (connection, k, v) -> connection.hIncrBy(k, v, 1)));
        action.execute();
        return ans;
    }


    /**
     * 返回已命中的敏感词
     *
     * @return key: 敏感词， value：计数
     */
    public Map<String, Integer> getHitSensitiveWords() {
        return RedisClient.hGetAll(SENSITIVE_WORD_CNT_PREFIX, Integer.class);
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
        if (BooleanUtils.isTrue(sensitiveConfig.getEnable())) {
            return sensitiveWordBs.replace(txt);
        }
        return txt;
    }

    /**
     * 查询文本中所有命中的敏感词
     *
     * @param txt 校验文本
     * @return 命中的敏感词
     */
    public List<String> findAll(String txt) {
        return sensitiveWordBs.findAll(txt);
    }
}
