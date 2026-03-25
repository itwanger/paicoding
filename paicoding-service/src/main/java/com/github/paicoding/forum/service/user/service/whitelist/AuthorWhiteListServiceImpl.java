package com.github.paicoding.forum.service.user.service.whitelist;

import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.config.repository.dao.ConfigDao;
import com.github.paicoding.forum.service.config.repository.entity.GlobalConfigDO;
import com.github.paicoding.forum.service.user.service.AuthorWhiteListService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YiHui
 * @date 2023/4/9
 */
@Service
public class AuthorWhiteListServiceImpl implements AuthorWhiteListService {
    /**
     * 实用 redis - set 来存储允许直接发文章的白名单
     */
    private static final String ARTICLE_WHITE_LIST = "auth_article_white_list";

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigDao configDao;

    @Override
    public boolean authorInArticleWhiteList(Long authorId) {
        if (authorId == null) {
            return false;
        }
        return RedisClient.sIsMember(ARTICLE_WHITE_LIST, authorId) || loadPersistedWhiteList().contains(authorId);
    }

    /**
     * 获取所有的白名单用户
     *
     * @return
     */
    @Override
    public List<BaseUserInfoDTO> queryAllArticleWhiteListAuthors() {
        Set<Long> users = new LinkedHashSet<>(loadPersistedWhiteList());
        Set<Long> redisUsers = RedisClient.sGetAll(ARTICLE_WHITE_LIST, Long.class);
        if (!CollectionUtils.isEmpty(redisUsers)) {
            users.addAll(redisUsers);
        }
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        List<BaseUserInfoDTO> userInfos = userService.batchQueryBasicUserInfo(users);
        return userInfos;
    }

    @Override
    public void addAuthor2ArticleWhitList(Long userId) {
        RedisClient.sPut(ARTICLE_WHITE_LIST, userId);
        Set<Long> users = new LinkedHashSet<>(loadPersistedWhiteList());
        users.add(userId);
        savePersistedWhiteList(users);
    }

    @Override
    public void removeAuthorFromArticleWhiteList(Long userId) {
        RedisClient.sDel(ARTICLE_WHITE_LIST, userId);
        Set<Long> users = new LinkedHashSet<>(loadPersistedWhiteList());
        users.remove(userId);
        savePersistedWhiteList(users);
    }

    private Set<Long> loadPersistedWhiteList() {
        GlobalConfigDO config = configDao.getGlobalConfigByKey(ARTICLE_WHITE_LIST);
        if (config == null || StringUtils.isBlank(config.getValue())) {
            return Collections.emptySet();
        }
        return Arrays.stream(config.getValue().split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void savePersistedWhiteList(Set<Long> users) {
        GlobalConfigDO config = configDao.getGlobalConfigByKey(ARTICLE_WHITE_LIST);
        String value = users.stream().map(String::valueOf).collect(Collectors.joining(","));
        if (config == null) {
            if (users.isEmpty()) {
                return;
            }
            config = new GlobalConfigDO();
            config.setKey(ARTICLE_WHITE_LIST);
            config.setValue(value);
            config.setComment("文章发布作者白名单");
            configDao.save(config);
            return;
        }
        config.setValue(value);
        configDao.updateById(config);
    }
}
