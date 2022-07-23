package com.github.liuyueyi.forum.web.controller;

import com.github.liuyueyi.forum.service.comment.impl.CommentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * 评论接口controller
 * @author lvmenglou
 * @date : 2022/4/22 10:56
 **/
@Controller
@RequestMapping("/api/v1/forum/comment")
@Slf4j
public class ConmmentController {

    @Resource
    private CommentServiceImpl commentService;

}
