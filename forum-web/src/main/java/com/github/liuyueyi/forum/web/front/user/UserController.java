package com.github.liuyueyi.forum.web.front.user;

import com.github.liueyueyi.forum.api.model.context.ReqInfoContext;
import com.github.liueyueyi.forum.api.model.enums.FollowTypeEnum;
import com.github.liueyueyi.forum.api.model.enums.UserHomeSelectEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.article.dto.ArticleListDTO;
import com.github.liueyueyi.forum.api.model.vo.article.dto.UserSelectDTO;
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

    private static final List<String> selectTags = Arrays.asList("article", "read", "follow", "collection");

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
     * 获取用户主页信息（TODO：异常需要捕获）
     *
     * @return
     */
    @GetMapping(path = "home")
    public String getUserHome(Model model, HttpServletRequest request) {

        Long userId = ReqInfoContext.getReqInfo().getUserId();
//        userId = 5L; // for test

        String selectType = request.getParameter("selectType");
        if (selectType == null || selectType.equals(Strings.EMPTY)) {
            selectType = UserHomeSelectEnum.ARTICLE.getCode();
        }

        UserHomeDTO userHomeDTO = userService.getUserHomeDTO(userId);
        List<UserSelectDTO> userSelectDTOS = userHomeSelectTags(selectType);
        userHomeSelectList(selectType, userId, request, model);

        model.addAttribute("homeSelectType", selectType);
        model.addAttribute("homeSelectTags", userSelectDTOS);
        model.addAttribute("userHome", userHomeDTO);
        return "biz/user/home";
    }

    /**
     * 返回选择列表标签
     *
     * @param selectType
     */
    private List<UserSelectDTO> userHomeSelectTags(String selectType) {
        List<UserSelectDTO> userSelectDTOS = new ArrayList<>();
        selectTags.forEach(tag -> {
            UserSelectDTO userSelectDTO = new UserSelectDTO();
            userSelectDTO.setSelectType(tag);
            userSelectDTO.setSelectDesc(UserHomeSelectEnum.formCode(tag).getDesc());
            userSelectDTO.setSelected(selectType.equals(tag) ? Boolean.TRUE : Boolean.FALSE);
            userSelectDTOS.add(userSelectDTO);
        });
        return userSelectDTOS;
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
        if (homeSelectType.equals(UserHomeSelectEnum.ARTICLE.getCode())) {
            ArticleListDTO articleListDTO = articleService.getArticleListByUserId(userId, pageParam);
            model.addAttribute("homeSelectList", articleListDTO);
        } else if (homeSelectType.equals(UserHomeSelectEnum.READ.getCode())) {
            ArticleListDTO articleListDTO = articleService.getReadArticleListByUserId(userId, pageParam);
            model.addAttribute("homeSelectList", articleListDTO);
        }  else if (homeSelectType.equals(UserHomeSelectEnum.COLLECTION.getCode())) {
            ArticleListDTO articleListDTO = articleService.getCollectionArticleListByUserId(userId, pageParam);
            model.addAttribute("homeSelectList", articleListDTO);
        } else if (homeSelectType.equals(UserHomeSelectEnum.FOLLOW.getCode())) {

            // 关注用户与被关注用户
            String selectType = request.getParameter("followType");
            if (selectType == null || selectType.equals(Strings.EMPTY)) {
                selectType = FollowTypeEnum.FOLLOW.getCode();
            }

            if (selectType.equals(FollowTypeEnum.FOLLOW.getCode())) {
                UserFollowListDTO userFollowListDTO = userRelationService.getUserFollowList(userId, pageParam);
                model.addAttribute("followList", userFollowListDTO);
            } else {
                UserFollowListDTO userFollowListDTO = userRelationService.getUserFansList(userId, pageParam);
                model.addAttribute("fansList", userFollowListDTO);
            }
            model.addAttribute("followType", selectType);
        }
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
}
