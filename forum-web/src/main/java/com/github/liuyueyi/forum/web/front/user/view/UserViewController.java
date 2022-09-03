package com.github.liuyueyi.forum.web.front.user.view;

import com.github.liueyueyi.forum.api.model.enums.FollowSelectEnum;
import com.github.liueyueyi.forum.api.model.enums.FollowTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.HomeSelectEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagSelectDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowListDTO;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.liuyueyi.forum.service.article.service.impl.ArticleReadServiceImpl;
import com.github.liuyueyi.forum.service.user.service.relation.UserRelationServiceImpl;
import com.github.liuyueyi.forum.service.user.service.user.UserServiceImpl;
import com.github.liuyueyi.forum.web.front.user.vo.UserHomeVo;
import com.github.liuyueyi.forum.web.global.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 用户注册、取消，登录、登出
 *
 * @author lvmenglou
 * @date : 2022/8/3 10:56
 **/
@Controller
@RequestMapping(path = "user")
@Slf4j
public class UserViewController extends BaseController {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private UserRelationServiceImpl userRelationService;

    @Resource
    private ArticleReadServiceImpl articleReadService;

    private static final List<String> homeSelectTags = Arrays.asList("article", "read", "follow", "collection");
    private static final List<String> followSelectTags = Arrays.asList("follow", "fans");

    /**
     * 获取用户主页信息
     *
     * @return
     */
    @GetMapping(path = "home")
    public String getUserHome(@RequestParam(name = "userId") Long userId,
                              @RequestParam(name = "homeSelectType", required = false) String homeSelectType,
                              @RequestParam(name = "followSelectType", required = false) String followSelectType,
                              Model model) {
        UserHomeVo vo = new UserHomeVo();
        vo.setHomeSelectType(StringUtils.isBlank(homeSelectType) ? HomeSelectEnum.ARTICLE.getCode() : homeSelectType);
        vo.setFollowSelectType(StringUtils.isBlank(followSelectType) ? FollowTypeEnum.FOLLOW.getCode() : followSelectType);

        UserStatisticInfoDTO userInfo = userService.queryUserInfoWithStatistic(userId);
        vo.setUserHome(userInfo);

        List<TagSelectDTO> homeSelectTags = homeSelectTags(vo.getHomeSelectType());
        vo.setHomeSelectTags(homeSelectTags);

        userHomeSelectList(vo, userId);
        model.addAttribute("vo", vo);
        return "biz/user/home";
    }

    /**
     * 返回Home页选择列表标签
     *
     * @param selectType
     * @return
     */
    private List<TagSelectDTO> homeSelectTags(String selectType) {
        List<TagSelectDTO> tags = new ArrayList<>();
        homeSelectTags.forEach(tag -> {
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

    /**
     * 返回选择列表
     *
     * @param vo
     * @param userId
     */
    private void userHomeSelectList(UserHomeVo vo, Long userId) {
        PageParam pageParam = PageParam.newPageInstance();
        HomeSelectEnum select = HomeSelectEnum.fromCode(vo.getHomeSelectType());
        if (select == null) {
            return;
        }

        switch (select) {
            case ARTICLE:
            case READ:
            case COLLECTION:
                ArticleListDTO dto = articleReadService.queryArticlesByUserAndType(userId, pageParam, select);
                vo.setHomeSelectList(dto);
                return;
            case FOLLOW:
                // 关注用户与被关注用户
                // 获取选择标签
                List<TagSelectDTO> followSelectTags = followSelectTags(vo.getFollowSelectType());
                vo.setFollowSelectTags(followSelectTags);
                initFollowFansList(vo, userId, pageParam);
                return;
            default:
        }
    }

    private void initFollowFansList(UserHomeVo vo, long userId, PageParam pageParam) {
        if (vo.getHomeSelectType().equals(FollowTypeEnum.FOLLOW.getCode())) {
            UserFollowListDTO userFollowListDTO = userRelationService.getUserFollowList(userId, pageParam);
            vo.setFollowList(userFollowListDTO);
            vo.setFansList(UserFollowListDTO.emptyInstance());
        } else {
            UserFollowListDTO userFollowListDTO = userRelationService.getUserFansList(userId, pageParam);
            vo.setFansList(userFollowListDTO);
            vo.setFollowList(UserFollowListDTO.emptyInstance());
        }
    }
}
