package com.github.liuyueyi.forum.web.front.user;

import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liueyueyi.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.liueyueyi.forum.api.model.vo.user.UserSaveReq;
import com.github.liuyueyi.forum.service.comment.dto.CommentTreeDTO;
import com.github.liuyueyi.forum.service.user.dto.UserHomeDTO;
import com.github.liuyueyi.forum.service.user.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 用户
 * @author lvmenglou
 * @date : 2022/8/3 10:56
 **/
@Controller
@RequestMapping(path = "user")
@Slf4j
public class UserController {

    @Resource
    private UserServiceImpl userService;

    /**
     * 保存用户（TODO：异常需要捕获）
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping(path = "save_user")
    public String saveUser(UserSaveReq req) throws Exception {
        userService.saveUser(req);
        return "";
    }


    /**
     * 删除用户（TODO：异常需要捕获）
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @PostMapping(path = "delete_user")
    public String deleteUser(Long userId) throws Exception {
        userService.deleteUser(userId);
        return "";
    }

    /**
     * 保存用户详情（TODO：异常需要捕获）
     *
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping(path = "save_userinfo")
    public String saveUserInfo(UserInfoSaveReq req) throws Exception {
        userService.saveUserInfo(req);
        return "";
    }

    /**
     * 删除用户详情（TODO：异常需要捕获）
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @PostMapping(path = "delete_userinfo")
    public String deleteUserInfo(Long userId) throws Exception {
        userService.deleteUserInfo(userId);
        return "";
    }

    /**
     * 获取用户主页信息（TODO：异常需要捕获）
     *
     * @param userId
     * @return
     */
    @GetMapping(path = "home")
    public String getUserHomeDTO(Long userId, Model model) throws Exception {
        UserHomeDTO userHomeDTO = userService.getUserHomeDTO(userId);
        model.addAttribute("user_home", userHomeDTO);
        return "biz/user/home";
    }

}
