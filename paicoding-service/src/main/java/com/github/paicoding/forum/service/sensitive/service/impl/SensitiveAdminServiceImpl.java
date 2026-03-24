package com.github.paicoding.forum.service.sensitive.service.impl;

import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.event.ConfigRefreshEvent;
import com.github.paicoding.forum.api.model.vo.config.SensitiveWordConfigReq;
import com.github.paicoding.forum.api.model.vo.config.SearchSensitiveWordHitReq;
import com.github.paicoding.forum.api.model.vo.config.dto.SensitiveWordConfigDTO;
import com.github.paicoding.forum.api.model.vo.config.dto.SensitiveWordHitDTO;
import com.github.paicoding.forum.core.senstive.SensitiveProperty;
import com.github.paicoding.forum.core.senstive.SensitiveService;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.sensitive.service.SensitiveAdminService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 敏感词后台管理服务实现
 *
 * @author Codex
 * @date 2026/3/24
 */
@Service
public class SensitiveAdminServiceImpl implements SensitiveAdminService {
    private static final long MAX_HIT_PAGE_SIZE = 100L;
    private static final String KEY_ENABLE = SensitiveProperty.SENSITIVE_KEY_PREFIX + ".enable";
    private static final String KEY_DENY = SensitiveProperty.SENSITIVE_KEY_PREFIX + ".deny";
    private static final String KEY_ALLOW = SensitiveProperty.SENSITIVE_KEY_PREFIX + ".allow";

    private static final String COMMENT_ENABLE = "敏感词开关";
    private static final String COMMENT_DENY = "敏感词名单";
    private static final String COMMENT_ALLOW = "敏感词白名单";

    @Autowired
    private SensitiveProperty sensitiveProperty;

    @Autowired
    private SensitiveService sensitiveService;

    @Autowired
    private ConfigDao configDao;

    @Override
    public SensitiveWordConfigDTO getConfig() {
        SensitiveWordConfigDTO dto = new SensitiveWordConfigDTO();
        dto.setEnable(readEnableConfig());
        dto.setDenyWords(readWordConfig(KEY_DENY, sensitiveProperty.getDeny()));
        dto.setAllowWords(readWordConfig(KEY_ALLOW, sensitiveProperty.getAllow()));
        dto.setHitTotal(countHitWords());
        dto.setHitWords(Collections.emptyList());
        return dto;
    }

    @Override
    public PageVo<SensitiveWordHitDTO> getHitWordPage(SearchSensitiveWordHitReq req) {
        Map<String, Integer> hitWords = sensitiveService.getHitSensitiveWords();
        long pageNum = normalizePageNumber(req);
        long pageSize = normalizePageSize(req);
        if (CollectionUtils.isEmpty(hitWords)) {
            return PageVo.build(Collections.emptyList(), pageSize, pageNum, 0);
        }

        long total = hitWords.size();
        long pageTotal = (long) Math.ceil((double) total / pageSize);
        long actualPageNum = Math.min(pageNum, pageTotal);
        long offset = (actualPageNum - 1) * pageSize;
        List<SensitiveWordHitDTO> list = hitWords.entrySet().stream()
                .skip(offset)
                .limit(pageSize)
                .map(entry -> {
                    SensitiveWordHitDTO dto = new SensitiveWordHitDTO();
                    dto.setWord(entry.getKey());
                    dto.setHitCount(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
        return PageVo.build(list, pageSize, actualPageNum, total);
    }

    @Override
    public void saveConfig(SensitiveWordConfigReq req) {
        String enableValue = String.valueOf(Boolean.TRUE.equals(req.getEnable()));
        String denyValue = joinWords(req.getDenyWords());
        String allowValue = joinWords(req.getAllowWords());

        saveOrUpdateConfig(KEY_ENABLE, enableValue, COMMENT_ENABLE);
        saveOrUpdateConfig(KEY_DENY, denyValue, COMMENT_DENY);
        saveOrUpdateConfig(KEY_ALLOW, allowValue, COMMENT_ALLOW);
        refreshSensitiveConfig(enableValue, denyValue, allowValue);
    }

    @Override
    public void clearHitWord(String word) {
        if (StringUtils.isBlank(word)) {
            return;
        }
        sensitiveService.removeSensitiveWord(word.trim());
    }

    private long countHitWords() {
        Map<String, Integer> hitWords = sensitiveService.getHitSensitiveWords();
        return CollectionUtils.isEmpty(hitWords) ? 0L : hitWords.size();
    }

    private void saveOrUpdateConfig(String key, String value, String comment) {
        GlobalConfigDO config = configDao.getGlobalConfigByKey(key);
        if (config == null) {
            config = new GlobalConfigDO();
            config.setKey(key);
            config.setValue(value);
            config.setComment(comment);
            configDao.save(config);
        } else {
            config.setValue(value);
            config.setComment(comment);
            configDao.updateById(config);
        }
    }

    private void refreshSensitiveConfig(String enableValue, String denyValue, String allowValue) {
        SpringUtil.publishEvent(new ConfigRefreshEvent(this, KEY_ENABLE, enableValue));
        SpringUtil.publishEvent(new ConfigRefreshEvent(this, KEY_DENY, denyValue));
        SpringUtil.publishEvent(new ConfigRefreshEvent(this, KEY_ALLOW, allowValue));
    }

    private Boolean readEnableConfig() {
        GlobalConfigDO config = configDao.getGlobalConfigByKey(KEY_ENABLE);
        if (config == null || StringUtils.isBlank(config.getValue())) {
            return Boolean.TRUE.equals(sensitiveProperty.getEnable());
        }
        return Boolean.parseBoolean(config.getValue().trim());
    }

    private List<String> readWordConfig(String key, List<String> fallbackWords) {
        GlobalConfigDO config = configDao.getGlobalConfigByKey(key);
        if (config == null || StringUtils.isBlank(config.getValue())) {
            return normalizeWords(fallbackWords);
        }
        return normalizeWords(splitWords(config.getValue()));
    }

    private List<String> normalizeWords(List<String> words) {
        if (CollectionUtils.isEmpty(words)) {
            return Collections.emptyList();
        }
        return words.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
    }

    private String joinWords(List<String> words) {
        return normalizeWords(words).stream().collect(Collectors.joining(","));
    }

    private List<String> splitWords(String words) {
        if (StringUtils.isBlank(words)) {
            return Collections.emptyList();
        }
        String[] items = words.split(",");
        List<String> result = new ArrayList<>(items.length);
        for (String item : items) {
            if (StringUtils.isNotBlank(item)) {
                result.add(item.trim());
            }
        }
        return result;
    }

    private long normalizePageNumber(SearchSensitiveWordHitReq req) {
        return Optional.ofNullable(req)
                .map(SearchSensitiveWordHitReq::getPageNumber)
                .filter(pageNumber -> pageNumber > 0)
                .orElse(PageParam.DEFAULT_PAGE_NUM);
    }

    private long normalizePageSize(SearchSensitiveWordHitReq req) {
        long pageSize = Optional.ofNullable(req)
                .map(SearchSensitiveWordHitReq::getPageSize)
                .filter(size -> size > 0)
                .orElse(PageParam.DEFAULT_PAGE_SIZE);
        return Math.min(pageSize, MAX_HIT_PAGE_SIZE);
    }
}
