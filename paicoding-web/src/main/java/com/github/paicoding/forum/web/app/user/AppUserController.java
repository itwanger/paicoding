package com.github.paicoding.forum.web.app.user;

import com.github.paicoding.forum.api.model.enums.HomeSelectEnum;
import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.api.model.vo.comment.dto.CurrentCommentDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.comment.service.AppCommentReadService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.app.user.extend.AppUserServiceExtend;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author YiHui
 * @date 2024/3/16
 */
@Api(tags = {"APP用户"}, value = "app用户接口")
@RestController
@RequestMapping(path = "app/user")
public class AppUserController {
    @Resource
    private UserService userService;
    @Resource
    private ArticleReadService articleReadService;
    @Resource
    private AppCommentReadService appCommentReadService;
    @Resource
    private AppUserServiceExtend appUserServiceExtend;

    @ApiOperation(tags = "APP用户", value = "用户统计详情", notes = "用户信息，包含各种统计数据")
    @GetMapping(path = "/detail/{userId}")
    public ResVo<UserStatisticInfoDTO> info(@PathVariable("userId") Long userId) {
        UserStatisticInfoDTO user = userService.queryUserInfoWithStatistic(userId);
        return ResVo.ok(user);
    }

    /**
     * 基础用户信息
     *
     * @return
     */
    @ApiOperation(tags = "APP用户", value = "用户统计详情", notes = "用户信息，包含各种统计数据")
    @GetMapping(path = "/base/{userId}")
    public ResVo<SimpleUserInfoDTO> baseInfo(@PathVariable("userId") Long userId) {
        return null;
    }


    @ApiOperation(tags = "APP用户", value = "关注状态", notes = "当前登录用于与作者的关注关系")
    @GetMapping(path = "follow")
    public ResVo<Boolean> followStat(@RequestParam(name = "userId") Long userId) {
        if (!NumUtil.upZero(userId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS, "目标用户不合法or不存在");
        }
        Boolean status = userService.followed(userId);
        return ResVo.ok(status);
    }

    @ApiOperation(tags = "APP用户", value = "文章列表", notes = "用户发布的文章列表")
    @GetMapping(path = "articles")
    public ResVo<PageListVo<ArticleDTO>> articleList(@RequestParam("userId") Long userId,
                                                     @RequestParam(name = "page", required = false) Long page,
                                                     @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = PageParam.buildPageParam(page, size);
        PageListVo<ArticleDTO> list = articleReadService.queryArticlesByUserAndType(userId, pageParam, HomeSelectEnum.ARTICLE);
        return ResVo.ok(list);
    }

    @ApiOperation(tags = "APP用户", value = "评论列表", notes = "用户的评论信息")
    @GetMapping(path = "comments")
    public ResVo<PageListVo<CurrentCommentDTO>> commentList(@RequestParam(name = "userId") Long userId,
                                                            @RequestParam(name = "page", required = false) Long page,
                                                            @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = PageParam.buildPageParam(page, size);
        List<CurrentCommentDTO> list = appCommentReadService.queryLatestCommentsByUser(userId, pageParam);
        return ResVo.ok(PageListVo.newVo(list, pageParam.getPageSize()));
    }

    @ApiOperation(tags = "APP用户", value = "粉丝列表", notes = "用户的粉丝列表")
    @GetMapping(path = "fans")
    public ResVo<PageListVo<FollowUserInfoDTO>> fansList(@RequestParam(name = "userId") Long userId,
                                                         @RequestParam(name = "page", required = false) Long page,
                                                         @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = PageParam.buildPageParam(page, size);
        return ResVo.ok(appUserServiceExtend.queryFansList(userId, pageParam));
    }
}
