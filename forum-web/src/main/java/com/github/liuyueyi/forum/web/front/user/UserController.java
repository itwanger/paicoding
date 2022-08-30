package com.github.liuyueyi.forum.web.front.user;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.FollowSelectEnum;
import com.github.liueyueyi.forum.api.model.enums.FollowTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.HomeSelectEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.TagSelectDTO;
import com.github.liueyueyi.forum.api.model.vo.comment.dto.UserFollowListDTO;
import com.github.liueyueyi.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.UserRelationReq;
import com.github.liueyueyi.forum.api.model.vo.user.UserSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.dto.UserHomeDTO;
import com.github.liuyueyi.forum.core.permission.Permission;
import com.github.liuyueyi.forum.core.permission.UserRole;
import com.github.liuyueyi.forum.service.article.impl.ArticleServiceImpl;
import com.github.liuyueyi.forum.service.user.impl.UserRelationServiceImpl;
import com.github.liuyueyi.forum.service.user.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 用户
 * fixme url 签名改一下, 用户注册、取消，登录、登出
 *
 * @author lvmenglou
 * @date : 2022/8/3 10:56
 **/
@Controller
@Permission(role = UserRole.LOGIN)
@RequestMapping(path = "user")
@Slf4j
public class UserController {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private UserRelationServiceImpl userRelationService;

    @Resource
    private ArticleServiceImpl articleService;

    private static final List<String> homeSelectTags = Arrays.asList("article", "read", "follow", "collection");
    private static final List<String> followSelectTags = Arrays.asList("follow", "fans");

    /**
     * 保存用户
     * <p>
     * fixme 用户注册，如公众号的登录方式，需要给用户补齐基本的个人信息
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping(path = "saveUser")
    public String saveUser(UserSaveReq req) {
        userService.saveUser(req);
        return "";
    }

    /**
     * 保存用户详情
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping(path = "saveUserInfo")
    public String saveUserInfo(UserInfoSaveReq req) {
        userService.saveUserInfo(req);
        return "";
    }

    /**
     * 保存用户关系
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping(path = "saveUserRelation")
    public String saveUserRelation(UserRelationReq req) throws Exception {
        userRelationService.saveUserRelation(req);
        return "";
    }

    /**
     * 获取用户主页信息（TODO：异常需要捕获）
     *
     * @return
     */
    @GetMapping(path = "home")
    public String getUserHome(Model model, HttpServletRequest request) {

        Long userId = ReqInfoContext.getReqInfo().getUserId();
//        userId = 5L; // for test

        String homeSelectType = request.getParameter("homeSelectType");
        if (homeSelectType == null || homeSelectType.equals(Strings.EMPTY)) {
            homeSelectType = HomeSelectEnum.ARTICLE.getCode();
        }

        UserHomeDTO userHomeDTO = userService.getUserHomeDTO(userId);
        List<TagSelectDTO> homeSelectTags = homeSelectTags(homeSelectType);
        userHomeSelectList(homeSelectType, userId, request, model);

        model.addAttribute("homeSelectType", homeSelectType);
        model.addAttribute("homeSelectTags", homeSelectTags);
        model.addAttribute("userHome", userHomeDTO);
        return "biz/user/home";
    }

    /**
     * 返回Home页选择列表标签
     *
     * @param selectType
     * @return
     */
    private List<TagSelectDTO> homeSelectTags(String selectType) {
        List<TagSelectDTO> tagSelectDTOS = new ArrayList<>();
        homeSelectTags.forEach(tag -> {
            TagSelectDTO tagSelectDTO = new TagSelectDTO();
            tagSelectDTO.setSelectType(tag);
            tagSelectDTO.setSelectDesc(HomeSelectEnum.formCode(tag).getDesc());
            tagSelectDTO.setSelected(selectType.equals(tag) ? Boolean.TRUE : Boolean.FALSE);
            tagSelectDTOS.add(tagSelectDTO);
        });
        return tagSelectDTOS;
    }

    /**
     * 返回关注用户选择列表标签
     *
     * @param selectType
     * @return
     */
    private List<TagSelectDTO> followSelectTags(String selectType) {
        List<TagSelectDTO> tagSelectDTOS = new ArrayList<>();
        followSelectTags.forEach(tag -> {
            TagSelectDTO tagSelectDTO = new TagSelectDTO();
            tagSelectDTO.setSelectType(tag);
            tagSelectDTO.setSelectDesc(FollowSelectEnum.formCode(tag).getDesc());
            tagSelectDTO.setSelected(selectType.equals(tag) ? Boolean.TRUE : Boolean.FALSE);
            tagSelectDTOS.add(tagSelectDTO);
        });
        return tagSelectDTOS;
    }

    /**
     * 返回选择列表
     *
     * @param homeSelectType
     * @param userId
     * @param model
     */
    private void userHomeSelectList(String homeSelectType, Long userId, HttpServletRequest request, Model model) {
        PageParam pageParam = PageParam.newPageInstance(1L, 10L);
        if (homeSelectType.equals(HomeSelectEnum.ARTICLE.getCode())) {
            ArticleListDTO articleListDTO = articleService.getArticleListByUserId(userId, pageParam);
            model.addAttribute("homeSelectList", articleListDTO);
        } else if (homeSelectType.equals(HomeSelectEnum.READ.getCode())) {
            ArticleListDTO articleListDTO = articleService.getReadArticleListByUserId(userId, pageParam);
            model.addAttribute("homeSelectList", articleListDTO);
        }  else if (homeSelectType.equals(HomeSelectEnum.COLLECTION.getCode())) {
            ArticleListDTO articleListDTO = articleService.getCollectionArticleListByUserId(userId, pageParam);
            model.addAttribute("homeSelectList", articleListDTO);
        } else if (homeSelectType.equals(HomeSelectEnum.FOLLOW.getCode())) {

            // 关注用户与被关注用户
            String followSelectType = request.getParameter("followSelectType");
            if (followSelectType == null || followSelectType.equals(Strings.EMPTY)) {
                followSelectType = FollowTypeEnum.FOLLOW.getCode();
            }

            // 获取选择标签
            List<TagSelectDTO> followSelectTags = followSelectTags(followSelectType);

            if (followSelectType.equals(FollowTypeEnum.FOLLOW.getCode())) {
                UserFollowListDTO userFollowListDTO = userRelationService.getUserFollowList(userId, pageParam);
                model.addAttribute("followList", userFollowListDTO);
            } else {
                UserFollowListDTO userFollowListDTO = userRelationService.getUserFansList(userId, pageParam);
                model.addAttribute("fansList", userFollowListDTO);
            }
            model.addAttribute("followSelectType", followSelectType);
            model.addAttribute("followSelectTags", followSelectTags);
        }
    }

}
