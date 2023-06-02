package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.*;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.CategoryDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.SimpleArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.util.ArticleUtil;
import com.github.paicoding.forum.service.article.conveter.ArticleConverter;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.ArticleTagDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.article.service.CategoryService;
import com.github.paicoding.forum.service.constant.RedisConstant;
import com.github.paicoding.forum.service.user.repository.entity.UserFootDO;
import com.github.paicoding.forum.service.user.service.CountService;
import com.github.paicoding.forum.service.user.service.UserFootService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.service.utils.RedisLuaUtil;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 文章查询相关服务类
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
@Slf4j
public class ArticleReadServiceImpl implements ArticleReadService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ArticleTagDao articleTagDao;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisLuaUtil redisLuaUtil;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 在一个项目中，UserFootService 就是内部服务调用
     * 拆微服务时，这个会作为远程服务访问
     */
    @Autowired
    private UserFootService userFootService;

    @Autowired
    private CountService countService;

    @Autowired
    private UserService userService;

    @Override
    public ArticleDO queryBasicArticle(Long articleId) {
        return articleDao.getById(articleId);
    }

    @Override
    public String generateSummary(String content) {
        return ArticleUtil.pickSummary(content);
    }

    @Override
    public PageVo<TagDTO> queryTagsByArticleId(Long articleId) {
        List<TagDTO> tagDTOS = articleTagDao.queryArticleTagDetails(articleId);
        return PageVo.build(tagDTOS, 1, 10, tagDTOS.size());
    }

    @Override
    public ArticleDTO queryDetailArticleInfo(Long articleId) {

        // TODO ygl:引入Redis缓存
        String redisCacheKey = RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_CACHE + articleId;
        String articleStr = RedisClient.getStr(redisCacheKey);
        ArticleDTO article = null;
        if (!ObjectUtils.isEmpty(articleStr)) {
            article = JSONUtil.toBean(articleStr, ArticleDTO.class);
        } else {
            // TODO ygl:存在缓存击穿问题，引入分布式锁

            /*
            第一种方式：
            缺点：不加finally去del锁，那么会出现该线程执行完之后在不过期时间内一直持有该锁不释放，
            在这过程内导致其他线程无法再次获取锁
            */
            // article = this.checkArticleByDBOne(articleId);

            /*
            第二种方式：
                优点：与第一种方式相比增加了finally，在线程执行完之后会立即释放锁
            即使在执行finally之前宕机了，那么因为有了过期时间，还是会自动释放
                缺点：可能会释放别人的锁。
            */
            // article = this.checkArticleByDBTwo(articleId);

            /*
            第三种方式：
                优点：与第二种方式相比解决了其删除别人分布式锁的问题。在加锁时set(key, value);
            解锁时会对比是否和他加锁时的value是否相等，相等则是他自己的锁，否则是别人锁不能解锁。
                在解锁时采用了lua脚本保证其原子性
                缺点：这种方式会出现加锁过期时间不能够根据业务和运行环境设置合适过期时间；
            设置时间过短，则会业务还未执行完毕则锁自动释放，那么其他线程依旧可以拿到锁，无法很好解决缓存击穿问题
            设置时间过长：如果在执行finally释放锁之前系统宕机了，那么还需要等着到时间后才能自动解锁
            */
            // article = this.checkArticleByDBThree(articleId);

            /*
            第四种方式：
                优点：解决了第三种方式无法设置合适过期时间
            */
            article = this.checkArticleByDBFour(articleId);

        }
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        RedisClient.setStr(redisCacheKey, JSONUtil.toJsonStr(article));
        // 更新分类相关信息
        CategoryDTO category = article.getCategory();
        category.setCategory(categoryService.queryCategoryName(category.getCategoryId()));

        // 更新标签信息
        article.setTags(articleTagDao.queryArticleTagDetails(articleId));
        return article;
    }

    /**
     * Redis分布式锁第四种方法
     *
     * @param articleId
     * @return ArticleDTO
     */
    private ArticleDTO checkArticleByDBFour(Long articleId) {

        String redisLockKey =
                RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_LOCK + articleId;
        RLock lock = redissonClient.getLock(redisLockKey);
        //lock.lock();
        ArticleDTO article = null;
        try {
            //尝试加锁,最大等待时间3秒，上锁30秒自动解锁
            if (lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                article = articleDao.queryArticleDetail(articleId);
            } else {
                // 未获得分布式锁线程睡眠一下；然后再去获取数据
                Thread.sleep(200);
                this.queryDetailArticleInfo(articleId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //判断该lock是否已经锁 并且 锁是否是自己的
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }
        return article;
    }

    /**
     * Redis分布式锁第三种方法
     *
     * @param articleId
     * @return ArticleDTO
     */
    private ArticleDTO checkArticleByDBThree(Long articleId) {

        String redisLockKey = RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_LOCK + articleId;

        String value = RandomUtil.randomString(6);
        Boolean isLockSuccess = RedisClient.setStrWithExpire(redisLockKey, value, 90L);
        ArticleDTO article = null;
        try {
            if (isLockSuccess) {
                article = articleDao.queryArticleDetail(articleId);
            }
        } finally {
            // 这种先get出value，然后再比较删除；这无法保证原子性，为了保证原子性，采用了lua脚本
            /*
            String redisLockValue = RedisClient.getStr(redisLockKey);
            if (!ObjectUtils.isEmpty(redisLockValue) && StringUtils.equals(value, redisLockValue)) {
                RedisClient.del(redisLockKey);
            }
            */
            Long cad = redisLuaUtil.cad("pai_" + redisLockKey, value);
            log.info("lua 脚本删除结果：" + cad);


        }

        return article;

    }

    /**
     * Redis分布式锁第二种方法
     *
     * @param articleId
     * @return ArticleDTO
     */
    private ArticleDTO checkArticleByDBTwo(Long articleId) {

        String redisLockKey = RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_LOCK + articleId;

        ArticleDTO article = null;
        Boolean isLockSuccess = RedisClient.setStrWithExpire(redisLockKey, null, 100L);
        try {
            if (isLockSuccess) {
                article = articleDao.queryArticleDetail(articleId);
            }
        } finally {
            RedisClient.del(redisLockKey);
        }

        return article;

    }

    /**
     * Redis分布式锁第一种方法
     *
     * @param articleId
     * @return ArticleDTO
     */
    private ArticleDTO checkArticleByDBOne(Long articleId) {

        String redisLockKey = RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_LOCK + articleId;

        ArticleDTO article = null;
        Boolean isLockSuccess = RedisClient.setStrWithExpire(redisLockKey, null, 100L);

        if (isLockSuccess) {
            article = articleDao.queryArticleDetail(articleId);
        }

        return article;
    }

    /**
     * 查询文章所有的关联信息，正文，分类，标签，阅读计数，当前登录用户是否点赞、评论过
     *
     * @param articleId
     * @param readUser
     * @return
     */
    @Override
    public ArticleDTO queryTotalArticleInfo(Long articleId, Long readUser) {
        ArticleDTO article = queryDetailArticleInfo(articleId);

        // 文章阅读计数+1
        articleDao.incrReadCount(articleId);

        // 文章的操作标记
        if (readUser != null) {
            // 更新用于足迹，并判断是否点赞、评论、收藏
            UserFootDO foot = userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId,
                    article.getAuthor(), readUser, OperateTypeEnum.READ);
            article.setPraised(Objects.equals(foot.getPraiseStat(), PraiseStatEnum.PRAISE.getCode()));
            article.setCommented(Objects.equals(foot.getCommentStat(), CommentStatEnum.COMMENT.getCode()));
            article.setCollected(Objects.equals(foot.getCollectionStat(), CollectionStatEnum.COLLECTION.getCode()));
        } else {
            // 未登录，全部设置为未处理
            article.setPraised(false);
            article.setCommented(false);
            article.setCollected(false);
        }

        // 更新文章统计计数
        article.setCount(countService.queryArticleCountInfoByArticleId(articleId));

        // 设置文章的点赞列表
        article.setPraisedUsers(userFootService.queryArticlePraisedUsers(articleId));
        return article;
    }


    /**
     * 查询文章列表
     * @param categoryId
     * @param page
     * @return
     */
    @Override
    public PageListVo<ArticleDTO> queryArticlesByCategory(Long categoryId, PageParam page) {
        List<ArticleDO> records = articleDao.listArticlesByCategoryId(categoryId, page);
        return buildArticleListVo(records, page.getPageSize());
    }

    /**
     * 查询置顶的文章列表
     * @param categoryId
     * @return
     */
    @Override
    public List<ArticleDTO> queryTopArticlesByCategory(Long categoryId) {
        PageParam page = PageParam.newPageInstance(PageParam.DEFAULT_PAGE_NUM, PageParam.TOP_PAGE_SIZE);
        List<ArticleDO> articleDTOS = articleDao.listArticlesByCategoryId(categoryId, page);
        return articleDTOS.stream().map(this::fillArticleRelatedInfo).collect(Collectors.toList());
    }

    @Override
    public Long queryArticleCountByCategory(Long categoryId) {
        return articleDao.countArticleByCategoryId(categoryId);
    }

    @Override
    public Map<Long, Long> queryArticleCountsByCategory() {
        return articleDao.countArticleByCategoryId();
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByTag(Long tagId, PageParam page) {
        List<ArticleDO> records = articleDao.listRelatedArticlesOrderByReadCount(null, Arrays.asList(tagId), page);
        return buildArticleListVo(records, page.getPageSize());
    }

    @Override
    public List<SimpleArticleDTO> querySimpleArticleBySearchKey(String key) {
        // todo 当key为空时，返回热门推荐
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        key = key.trim();
        List<ArticleDO> records = articleDao.listSimpleArticlesByBySearchKey(key);
        return records.stream().map(s -> new SimpleArticleDTO().setId(s.getId()).setTitle(s.getTitle()))
                .collect(Collectors.toList());
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesBySearchKey(String key, PageParam page) {
        List<ArticleDO> records = articleDao.listArticlesByBySearchKey(key, page);
        return buildArticleListVo(records, page.getPageSize());
    }


    @Override
    public PageListVo<ArticleDTO> queryArticlesByUserAndType(Long userId, PageParam pageParam, HomeSelectEnum select) {
        List<ArticleDO> records = null;
        if (select == HomeSelectEnum.ARTICLE) {
            // 用户的文章列表
            records = articleDao.listArticlesByUserId(userId, pageParam);
        } else if (select == HomeSelectEnum.READ) {
            // 用户的阅读记录
            List<Long> articleIds = userFootService.queryUserReadArticleList(userId, pageParam);
            records = CollectionUtils.isEmpty(articleIds) ? Collections.emptyList() : articleDao.listByIds(articleIds);
            records = sortByIds(articleIds, records);
        } else if (select == HomeSelectEnum.COLLECTION) {
            // 用户的收藏列表
            List<Long> articleIds = userFootService.queryUserCollectionArticleList(userId, pageParam);
            records = CollectionUtils.isEmpty(articleIds) ? Collections.emptyList() : articleDao.listByIds(articleIds);
            records = sortByIds(articleIds, records);
        }

        if (CollectionUtils.isEmpty(records)) {
            return PageListVo.emptyVo();
        }
        return buildArticleListVo(records, pageParam.getPageSize());
    }

    /**
     * fixme @楼仔 这个排序逻辑看着像是有问题的样子
     *
     * @param articleIds
     * @param records
     * @return
     */
    private List<ArticleDO> sortByIds(List<Long> articleIds, List<ArticleDO> records) {
        List<ArticleDO> articleDOS = new ArrayList<>();
        Map<Long, ArticleDO> articleDOMap = records.stream().collect(Collectors.toMap(ArticleDO::getId, t -> t));
        articleIds.forEach(articleId -> {
            if (articleDOMap.containsKey(articleId)) {
                articleDOS.add(articleDOMap.get(articleId));
            }
        });
        return articleDOS;
    }

    @Override
    public PageListVo<ArticleDTO> buildArticleListVo(List<ArticleDO> records, long pageSize) {
        List<ArticleDTO> result = records.stream().map(this::fillArticleRelatedInfo).collect(Collectors.toList());
        return PageListVo.newVo(result, pageSize);
    }

    /**
     * 补全文章的阅读计数、作者、分类、标签等信息
     *
     * @param record
     * @return
     */
    private ArticleDTO fillArticleRelatedInfo(ArticleDO record) {
        ArticleDTO dto = ArticleConverter.toDto(record);
        // 分类信息
        dto.getCategory().setCategory(categoryService.queryCategoryName(record.getCategoryId()));
        // 标签列表
        dto.setTags(articleTagDao.queryArticleTagDetails(record.getId()));
        // 阅读计数统计
        dto.setCount(countService.queryArticleCountInfoByArticleId(record.getId()));
        // 作者信息
        BaseUserInfoDTO author = userService.queryBasicUserInfo(dto.getAuthor());
        dto.setAuthorName(author.getUserName());
        dto.setAuthorAvatar(author.getPhoto());
        return dto;
    }

    @Override
    public PageListVo<SimpleArticleDTO> queryHotArticlesForRecommend(PageParam pageParam) {
        List<SimpleArticleDTO> list = articleDao.listHotArticles(pageParam);
        return PageListVo.newVo(list, pageParam.getPageSize());
    }

    @Override
    public int queryArticleCount(long authorId) {
        return articleDao.countArticleByUser(authorId);
    }

    @Override
    public Long getArticleCount() {
        return articleDao.countArticle();
    }
}
