package com.github.paicoding.forum.web.admin.rest;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.PageVo;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReplayReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserResumeInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.user.service.UserResumeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历管理端点
 *
 * @author YiHui
 * @date 2024/8/8
 */
@Api(value = "用户简历管理控制器", tags = "简历管理")
@Permission(role = UserRole.LOGIN)
@RestController
@RequestMapping(path = {"api/admin/resume", "admin/resume"})
public class ResumeSettingRestController {
    @Autowired
    private UserResumeService userResumeService;

    /**
     * 列表
     *
     * @param req
     * @return
     */
    @ApiOperation(value = "简历列表", notes = "查询简历列表")
    @PostMapping(path = "list")
    public ResVo<PageVo<UserResumeInfoDTO>> list(@RequestBody UserResumeReq req) {
        if (!NumUtil.upZero(req.getUserId())) {
            req.setUserId(null);
        }
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        if (!UserRole.ADMIN.name().equalsIgnoreCase(user.getRole())) {
            // 非管理员，只能查自己的简历；即指定了查询用户，也只返回自己的
            req.setUserId(user.getUserId());
            req.setUname(null);
        }
        req.autoInit();
        List<UserResumeInfoDTO> list = userResumeService.listResumes(req);
        long cnt = -1;
        if (req.getPageNum() == 1) {
            // 首页查询时，才需要统计一下总数； 非首页查询时，不返回总数
            cnt = userResumeService.count(req);
        }
        return ResVo.ok(PageVo.build(list, req.getPageSize(), req.getPageNum(), cnt));
    }


    /**
     * 回复
     *
     * @param replay
     * @return
     */
    @ApiOperation(value = "简历回复", notes = "管理员回复简历")
    @PostMapping(path = "replay")
    @Permission(role = UserRole.ADMIN)
    public ResVo<Boolean> replay(@RequestBody UserResumeReplayReq replay) {
        if (replay.getResumeId() == null) {
            throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "指定简历之后再回复把!");
        }

        return ResVo.ok(userResumeService.replayResume(replay));
    }


    @ApiOperation(value = "简历标记处理中", notes = "下载简历附件之后，自动标记状态为处理中")
    @GetMapping(path = "process")
    @Permission(role = UserRole.ADMIN)
    public ResVo<Boolean> markProcess(Long resumeId) {
        return ResVo.ok(userResumeService.downloadResume(resumeId));
    }


    @ApiOperation(value = "删除简历", notes = "删除自己的简历")
    @GetMapping(path = "delete")
    @Permission(role = UserRole.LOGIN)
    public ResVo<Boolean> deleteResume(Long resumeId) {
        return ResVo.ok(userResumeService.deleteResume(resumeId));
    }
}
