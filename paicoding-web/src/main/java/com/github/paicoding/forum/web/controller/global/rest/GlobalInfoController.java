package com.github.paicoding.forum.web.controller.global.rest;

import com.github.paicoding.forum.web.global.vo.ResultVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: tech-pai
 * @description:
 * @author: XuYifei
 * @create: 2024-07-05
 */

@RestController
@RequestMapping("api/global")
public class GlobalInfoController {
    /**
     * 用于单独提供一个接口，用于前端获取全局信息
     * @return
     */
    @GetMapping("info")
    public ResultVo<String> getGlobalInfo() {
        return ResultVo.ok("ok");
    }
}
