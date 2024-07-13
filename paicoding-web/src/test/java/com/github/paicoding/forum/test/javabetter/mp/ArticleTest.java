package com.github.paicoding.forum.test.javabetter.mp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.service.article.repository.dao.ArticleDao;
import com.github.paicoding.forum.service.article.repository.dao.TagDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.TagDO;
import com.github.paicoding.forum.service.notify.repository.dao.NotifyMsgDao;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserMapper;
import com.github.paicoding.forum.test.BasicTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleTest extends BasicTest {
    @Autowired
    private ArticleDao articleDao;

    // 标签
    @Autowired
    private TagDao tagDao;

    @Autowired
    private UserDao userDao;

    @Resource
    private UserMapper userMapper;

    @Resource
    private NotifyMsgDao notifyMsgDao;

    @Test
    public void testSelectById() {
        BaseMapper<ArticleDO> baseMapper = articleDao.getBaseMapper();
        ArticleDTO dto = articleDao.queryArticleDetail(15L);
        Map<String, Object> map = new HashMap<>();
        map.put("id", 15L);
        List<ArticleDO> dtoList = baseMapper.selectByMap(map);
        System.out.println(dto);
    }

    @Test
    public void testWrapper() {
        QueryWrapper<TagDO> wrapper = new QueryWrapper<>();
        // 包含“j”且状态是已发布
        wrapper.like("tag_name", "j").eq("status", 1);
        BaseMapper<TagDO> baseMapper = tagDao.getBaseMapper();
        List<TagDO> tagList = baseMapper.selectList(wrapper);
        tagList.forEach(System.out::println);
    }

    @Test
    public void testWrapperPlus() {
        QueryWrapper<TagDO> wrapper = new QueryWrapper<>();
        // 包含“j”且状态是已发布
        wrapper.select("tag_name","status").like("tag_name", "j").eq("status", 1);
        BaseMapper<TagDO> baseMapper = tagDao.getBaseMapper();
        List<TagDO> tagList = baseMapper.selectList(wrapper);
        tagList.forEach(System.out::println);
    }

    // 测试自定义 SQL
    @Test
    public void testCustomSql() {
        UserDO userDO =  userMapper.getByThirdAccountId("demoUser1234");
        System.out.println(userDO);
    }

    @Test
    public void testUpdateXml() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        notifyMsgDao.getBaseMapper().updateNoticeRead(ids);
    }
}
