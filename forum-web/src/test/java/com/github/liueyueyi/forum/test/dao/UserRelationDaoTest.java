package com.github.liueyueyi.forum.test.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.test.BasicTest;
import com.github.liuyueyi.forum.service.article.dto.TagDTO;
import com.github.liuyueyi.forum.service.article.impl.CategoryServiceImpl;
import com.github.liuyueyi.forum.service.article.impl.TagServiceImpl;
import com.github.liuyueyi.forum.service.article.repository.entity.CategoryDO;
import com.github.liuyueyi.forum.service.article.repository.entity.TagDO;
import com.github.liuyueyi.forum.service.comment.dto.UserFollowDTO;
import com.github.liuyueyi.forum.service.user.UserRelationService;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserRelationMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author YiHui
 * @date 2022/7/20
 */
@Slf4j
public class UserRelationDaoTest extends BasicTest {

    @Autowired
    private UserRelationService userRelationService;

    @Test
    public void testUserRelation() {
        List<UserFollowDTO> userFollowDTOS = userRelationService.getUserRelationList(1L, PageParam.newPageInstance(1L, 10L));
        log.info("query userFollowDTOS: {}", userFollowDTOS);
    }

}
