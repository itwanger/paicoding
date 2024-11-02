package com.github.paicoding.forum.web.controller.user.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.FollowSelectEnum;
import com.github.paicoding.forum.api.model.enums.HomeSelectEnum;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.TagSelectDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.api.model.vo.user.UserRelationReq;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.user.cahce.UserInfoCacheManager;
import com.github.paicoding.forum.service.user.service.relation.UserRelationServiceImpl;
import com.github.paicoding.forum.service.user.service.user.UserServiceImpl;
import com.github.paicoding.forum.web.controller.user.vo.UserHomeInfoVo;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author XuYifei
 * @date 2024/7/8
 */
@RestController
@RequestMapping(path = "user/api")
public class UserRestController {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private UserRelationServiceImpl userRelationService;

    @Resource
    private ArticleReadService articleReadService;

    @Resource
    private UserInfoCacheManager userInfoCacheManager;

    private static final List<String> homeSelectTags = Arrays.asList("article", "read", "follow", "collection");
    private static final List<String> followSelectTags = Arrays.asList("follow", "fans");


    /**
     * 保存用户关系
     *
     * @param req
     * @return
     * @throws Exception
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "saveUserRelation")
    public ResVo<Boolean> saveUserRelation(@RequestBody UserRelationReq req) {
        userRelationService.saveUserRelation(req);
        return ResVo.ok(true);
    }

    /**
     * 保存用户详情
     *
     * @param req
     * @return
     * @throws Exception
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "saveUserInfo")
    @Transactional(rollbackFor = Exception.class)
    public ResVo<Boolean> saveUserInfo(@RequestBody UserInfoSaveReq req) {
        if (req.getUserId() == null || !Objects.equals(req.getUserId(), ReqInfoContext.getReqInfo().getUserId())) {
            // 不能修改其他用户的信息
            return ResVo.fail(StatusEnum.FORBID_ERROR_MIXED, "无权修改");
        }
        userInfoCacheManager.delUserInfo(req.getUserId());
        userService.saveUserInfo(req);
        return ResVo.ok(true);
    }

    /**
     * 用户的文章列表翻页
     *
     * @param userId
     * @param homeSelectType
     * @return
     */
//    @GetMapping(path = "articleList")
//    public ResVo<NextPageHtmlVo> articleList(@RequestParam(name = "userId") Long userId,
//                                             @RequestParam(name = "homeSelectType") String homeSelectType,
//                                             @RequestParam("page") Long page,
//                                             @RequestParam(name = "pageSize", required = false) Long pageSize) {
//        HomeSelectEnum select = HomeSelectEnum.fromCode(homeSelectType);
//        if (select == null) {
//            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
//        }
//
//        if (pageSize == null) {
//            pageSize = PageParam.DEFAULT_PAGE_SIZE;
//        }
//        PageParam pageParam = PageParam.newPageInstance(page, pageSize);
//        PageListVo<ArticleDTO> dto = articleReadService.queryArticlesByUserAndType(userId, pageParam, select);
//        String html = templateEngineHelper.renderToVo("views/user/articles/index", "homeSelectList", dto);
//        return ResVo.ok(new NextPageHtmlVo(html, dto.getHasMore()));
//    }

//    @GetMapping(path = "followList")
//    public ResVo<NextPageHtmlVo> followList(@RequestParam(name = "userId") Long userId,
//                                            @RequestParam(name = "followSelectType") String followSelectType,
//                                            @RequestParam("page") Long page,
//                                            @RequestParam(name = "pageSize", required = false) Long pageSize) {
//        if (pageSize == null) {
//            pageSize = PageParam.DEFAULT_PAGE_SIZE;
//        }
//        PageParam pageParam = PageParam.newPageInstance(page, pageSize);
//        PageListVo<FollowUserInfoDTO> followList;
//        boolean needUpdateRelation = false;
//        if (followSelectType.equals(FollowTypeEnum.FOLLOW.getCode())) {
//            followList = userRelationService.getUserFollowList(userId, pageParam);
//        } else {
//            // 查询粉丝列表时，只能确定粉丝关注了userId，但是不能反向判断，因此需要再更新下映射关系，判断userId是否有关注这个用户
//            followList = userRelationService.getUserFansList(userId, pageParam);
//            needUpdateRelation = true;
//        }
//
//        Long loginUserId = ReqInfoContext.getReqInfo().getUserId();
//        if (!Objects.equals(loginUserId, userId) || needUpdateRelation) {
//            userRelationService.updateUserFollowRelationId(followList, userId);
//        }
//        String html = templateEngineHelper.renderToVo("views/user/follows/index", "followList", followList);
//        return ResVo.ok(new NextPageHtmlVo(html, followList.getHasMore()));
//    }


    /**
     * 获取用户主页信息，通常只有作者本人才能进入这个页面
     *
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "home")
    public ResultVo<UserHomeInfoVo> getUserHome(@RequestParam(name = "userId") Long userId) {
        UserHomeInfoVo vo = new UserHomeInfoVo();
        UserStatisticInfoDTO userInfo = userService.queryUserInfoWithStatistic(userId);
        vo.setUserHome(userInfo);

//        SpringUtil.getBean(SeoInjectService.class).initUserSeo(vo);
        return ResultVo.ok(vo);
    }

    /**
     * 获取用户主页信息的文章列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "articles")
    public ResultVo<IPage<ArticleDTO>> getUserArticles(@RequestParam(name = "userId") Long userId,
                                                      @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                      @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(articleReadService.queryArticlesByUserIdPagination(userId, currentPage, pageSize));
    }

    /**
     * 获取用户主页的阅读历史
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "history")
    public ResultVo<IPage<ArticleDTO>> getUserHistoryArticles(@RequestParam(name = "userId") Long userId,
                                                       @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                       @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(articleReadService.queryHistoryArticlesByUserIdPagination(userId, currentPage, pageSize));
    }

    /**
     * 获取用户主页的收藏列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "star")
    public ResultVo<IPage<ArticleDTO>> getUserStarArticles(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                              @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(articleReadService.queryStarArticlesByUserIdPagination(userId, currentPage, pageSize));
    }

    /**
     * 获取用户主页的关注列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "follows")
    public ResultVo<IPage<FollowUserInfoDTO>> getUserFollowed(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                              @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(userRelationService.getUserFollowListPagination(userId, currentPage, pageSize));
    }

    /**
     * 获取用户主页的粉丝列表
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "fans")
    public ResultVo<IPage<FollowUserInfoDTO>> getUserFans(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                              @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(userRelationService.getUserFansListPagination(userId, currentPage, pageSize));
    }


    /**
     * 返回Home页选择列表标签
     *
     * @param selectType
     * @param isAuthor true 表示当前为查看自己的个人主页
     * @return
     */
    private List<TagSelectDTO> homeSelectTags(String selectType, boolean isAuthor) {
        List<TagSelectDTO> tags = new ArrayList<>();
        homeSelectTags.forEach(tag -> {
            if (!isAuthor && "read".equals(tag)) {
                // 只有本人才能看自己的阅读历史
                return;
            }
            TagSelectDTO tagSelectDTO = new TagSelectDTO();
            tagSelectDTO.setSelectType(tag);
            tagSelectDTO.setSelectDesc(HomeSelectEnum.fromCode(tag).getDesc());
            tagSelectDTO.setSelected(selectType.equals(tag));
            tags.add(tagSelectDTO);
        });
        return tags;
    }

    /**
     * 返回关注用户选择列表标签
     *
     * @param selectType
     * @return
     */
    private List<TagSelectDTO> followSelectTags(String selectType) {
        List<TagSelectDTO> tags = new ArrayList<>();
        followSelectTags.forEach(tag -> {
            TagSelectDTO tagSelectDTO = new TagSelectDTO();
            tagSelectDTO.setSelectType(tag);
            tagSelectDTO.setSelectDesc(FollowSelectEnum.fromCode(tag).getDesc());
            tagSelectDTO.setSelected(selectType.equals(tag));
            tags.add(tagSelectDTO);
        });
        return tags;
    }
}
