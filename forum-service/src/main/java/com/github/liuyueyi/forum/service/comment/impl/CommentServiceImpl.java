package com.github.liuyueyi.forum.service.comment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.liueyueyi.forum.api.model.enums.CommentStatEnum;
import com.github.liueyueyi.forum.api.model.enums.DocumentTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.PraiseStatEnum;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.comment.CommentSaveReq;
import com.github.liuyueyi.forum.service.article.repository.entity.ArticleDO;
import com.github.liuyueyi.forum.service.article.repository.mapper.ArticleMapper;
import com.github.liuyueyi.forum.service.comment.CommentService;
import com.github.liuyueyi.forum.service.comment.converter.CommentConverter;
import com.github.liuyueyi.forum.service.comment.dto.CommentTreeDTO;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.comment.repository.mapper.CommentMapper;
import com.github.liuyueyi.forum.service.user.impl.UserFootServiceImpl;
import com.github.liuyueyi.forum.service.user.repository.entity.UserFootDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserFootMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 评论Service
 *
 * @author louzai
 * @date 2022-07-24
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private CommentConverter commentConverter;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserFootServiceImpl userFootService;

    @Override
    public Map<Long, CommentTreeDTO> getCommentList(Long articleId, PageParam pageSearchReq) {

        // 1. 获取当前分页的评论
        PageParam pageParam = PageParam.newPageInstance(pageSearchReq.getPageNum(), pageSearchReq.getPageSize());
        List<CommentDO> commentFirstLevelList = getCommentList(articleId, pageParam, 0L);
        if (commentFirstLevelList.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, CommentDO> commentFirstLevelMap = new HashMap<>();
        commentFirstLevelList.forEach(commentDO -> commentFirstLevelMap.put(commentDO.getId(), commentDO));

        // 2. 获取所有评论，并过滤 1 级评论，且不在分页中的数据
        pageParam = PageParam.newPageInstance(1L, Long.MAX_VALUE);
        List<CommentDO> commentAllList = getCommentList(articleId, pageParam, null);
        if (commentAllList.isEmpty()) {
            return new HashMap<>();
        }

        List<CommentDO> commentBasicList = new ArrayList<>();
        for (CommentDO commentDO : commentAllList) {
            if (commentDO.getParentCommentId() == 0 && !commentFirstLevelMap.containsKey(commentDO.getId())) {
                continue;
            }
            commentBasicList.add(commentDO);
        }

        // 3. 组建一棵树
        Map<Long, CommentTreeDTO> deptTreeMap = new HashMap<>();
        commentBasicList.forEach(commentDO -> deptTreeMap.put(commentDO.getId(), commentConverter.toDTO(commentDO)));
        Set<Long> commentIdSet = new HashSet<>(deptTreeMap.keySet());
        Map<Long, CommentTreeDTO> commentTreeMap = getCommentTree(deptTreeMap, commentIdSet, 0L);

        // 4. 填充每一个节点的信息
        fillCommentTree(commentTreeMap);
        return commentTreeMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentSaveReq commentSaveReq) throws Exception {

        // 保存评论
        if (commentSaveReq.getCommentId() == null || commentSaveReq.getCommentId() == 0) {
            CommentDO commentDO = commentConverter.toDo(commentSaveReq);
            commentMapper.insert(commentDO);
            ArticleDO articleDO = articleMapper.selectById(commentSaveReq.getArticleId());
            if (articleDO == null) {
                throw new Exception("该文章ID不存在");
            }
            userFootService.saveCommentFoot(commentSaveReq, commentDO.getId(), articleDO.getUserId());
            return commentDO.getId();
        }

        // 更新评论
        CommentDO commentDO = commentMapper.selectById(commentSaveReq.getCommentId());
        if (commentDO == null) {
            throw new Exception("未查询到该评论");
        }
        commentMapper.updateById(commentConverter.toDo(commentSaveReq));
        return commentSaveReq.getCommentId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) throws Exception {
        CommentDO commentDO = commentMapper.selectById(commentId);
        if (commentDO == null) {
            throw new Exception("未查询到该评论");
        }
        commentMapper.deleteById(commentId);
        userFootService.deleteCommentFoot(commentDO);
    }

    /**
     * 查询用户信息
     *
     * @param userId
     * @return
     */
    private UserInfoDO getUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userInfoMapper.selectOne(query);
    }

    /**
     * 获取评论列表
     *
     * @param pageParam
     * @param parentCommentId
     * @return
     */
    private List<CommentDO> getCommentList(Long articleId, PageParam pageParam, Long parentCommentId) {
        QueryWrapper<CommentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(parentCommentId != null, CommentDO::getParentCommentId, parentCommentId)
                .eq(CommentDO::getArticleId, articleId)
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(CommentDO::getId);
        return commentMapper.selectList(queryWrapper);
    }

    /**
     * 将评论构建成一棵树
     * 功能说明：将评论列表，构建成一颗树，如果列表中数据有多余的，可以直接剔除
     *
     * @param commentMap    需要构件树的评论列表
     * @param notDeleteSet  未删除标记
     * @param rootCommentId 根节点的ID
     * @return 一棵树
     */
    private Map<Long, CommentTreeDTO> getCommentTree(Map<Long, CommentTreeDTO> commentMap, Set<Long> notDeleteSet, Long rootCommentId) {
        Map<Long, CommentTreeDTO> commentTree = new HashMap<>();
        for (Map.Entry<Long, CommentTreeDTO> entry : commentMap.entrySet()) {
            Long commentId = entry.getKey();
            CommentTreeDTO commentInfo = entry.getValue();
            // 如果已经删除，直接跳过
            if (!notDeleteSet.contains(commentId)) {
                continue;
            }
            // 找到根节点的一个孩子，获取这个孩子节点的树
            if (commentInfo.getParentCommentId().equals(rootCommentId)) {
                // 删除已经处理过的节点
                notDeleteSet.remove(commentId);
                // 构造该节点的孩子节点树
                Map<Long, CommentTreeDTO> childrenCommentTree = this.getCommentTree(commentMap, notDeleteSet, commentId);
                if (childrenCommentTree.size() > 0) {
                    commentInfo.setCommentChilds(childrenCommentTree);
                }
                // 添加一颗子树
                commentTree.put(commentId, commentInfo);
            }
        }
        return commentTree;
    }

    /**
     * 填充评论的基础数据
     *
     * @param commentTreeMap
     */
    private void fillCommentTree(Map<Long, CommentTreeDTO> commentTreeMap) {
        for (Map.Entry<Long, CommentTreeDTO> entry : commentTreeMap.entrySet()) {
            CommentTreeDTO commentInfo = entry.getValue();
            UserInfoDO userInfoDO = getUserInfoByUserId(commentInfo.getUserId());
            if (userInfoDO == null) {
                // 如果用户注销，给一个默认的用户
                commentInfo.setUserName("默认用户"); // TODO: 后续再优化
                commentInfo.setUserPhoto("");
                commentInfo.setCommentCount(0);
                commentInfo.setCommentChilds(new HashMap<>());
                continue;
            }
            commentInfo.setUserName(userInfoDO.getUserName());
            commentInfo.setUserPhoto(userInfoDO.getPhoto());
            commentInfo.setCommentCount(commentInfo.getCommentChilds().size());

            Long praistCount = userFootService.queryCommentPraiseCount(userInfoDO.getId());
            commentInfo.setPraiseCount(praistCount.intValue());
            fillCommentTree(commentInfo.getCommentChilds());
        }
    }
}
