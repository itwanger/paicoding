package com.github.paicoding.forum.service.user.service.resume;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.enums.resume.ResumeTypeEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReplayReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.ResumeDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserResumeInfoDTO;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.converter.UserResumeConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserResumeDao;
import com.github.paicoding.forum.service.user.repository.entity.ResumeDO;
import com.github.paicoding.forum.service.user.service.UserResumeService;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户简历服务
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Slf4j
@Service
public class UserResumeServiceImpl implements UserResumeService {
    @Autowired
    private UserResumeDao userResumeDao;
    @Autowired
    private UserService userService;
    @Autowired
    private ResumeNotifyHelper resumeNotifyHelper;

    @Override
    public Boolean saveResume(UserResumeSaveReq req) {
        req.setUserId(ReqInfoContext.getReqInfo().getUserId());
        preCheckForUpdateUserResume(req);

        ResumeDO resume = new ResumeDO();
        resume.setId(req.getResumeId());
        resume.setUserId(req.getUserId());
        resume.setMark(req.getMark() == null ? "" : req.getMark());
        resume.setResumeName(req.getResumeName());
        resume.setResumeUrl(req.getResumeUrl());
        resume.setReplayEmail(req.getReplayEmail());
        resume.setUpdateTime(new Date(System.currentTimeMillis()));
        resume.setEmailState(-1);
        boolean ans = userResumeDao.saveOrUpdate(resume);
        if (ans && req.getResumeId() == null) {
            resumeNotifyHelper.notifyToUser(resume);
        }
        return ans;
    }

    /**
     * 上传简历的校验：
     * - 上传： 没有在途的简历时，才允许新传
     * - 更新： 简历必须是未处理状态，才支持更新
     *
     * @param req
     */
    private void preCheckForUpdateUserResume(UserResumeSaveReq req) {
        // 1. 更新简历
        // 简历存在，表示执行更新操作
        if (req.getResumeId() != null) {
            ResumeDO resume = userResumeDao.getById(req.getResumeId());
            if (resume == null) {
                // 简历不存在
                throw ExceptionUtil.of(StatusEnum.USER_RESUME_NOT_EXISTS);
            }
            if (!Objects.equals(resume.getUserId(), req.getUserId())) {
                // 用户不匹配，拒绝修改别人的简历~ 通常来说不会出现这种情况，如果有大神直接修改请求接口传参，则可能出现这种问题，所以需要进行做一个鉴权
                throw ExceptionUtil.of(StatusEnum.FORBID_ERROR);
            }

            if (!Objects.equals(resume.getType(), ResumeTypeEnum.UNPROCESS.getType())) {
                throw ExceptionUtil.of(StatusEnum.SELF_DEFINE_ERROR, "仅未处理的简历才支持修改哦~");
            }
            return;
        }


        // 2. 上传简历
        // 查询用户的简历，判断是否存在 未处理 - 处理中 的简历，若存在，则只支持修改，不支持重新上传
        List<ResumeDO> list = userResumeDao.queryUserResumes(req.getUserId());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (ResumeDO resume : list) {
            if (!Objects.equals(resume.getType(), ResumeTypeEnum.DONE.getType())) {
                throw ExceptionUtil.of(StatusEnum.SELF_DEFINE_ERROR, "您已经有简历再处理中了，暂不支持重新上传哦~");
            }
        }
    }


    /**
     * 简历下载，更新状态
     *
     * @param resumeId 简历id
     * @return
     */
    @Override
    public Boolean downloadResume(Long resumeId) {
        ResumeDO resume = userResumeDao.getById(resumeId);
        if (resume == null) {
            throw ExceptionUtil.of(StatusEnum.USER_RESUME_NOT_EXISTS);
        }
        if (!Objects.equals(resume.getType(), ResumeTypeEnum.UNPROCESS.getType())) {
            return true;
        }

        if (!UserRole.adminUser(ReqInfoContext.getReqInfo().getUser().getRole())
                && Objects.equals(ReqInfoContext.getReqInfo().getUserId(), resume.getUserId())) {
            // 非超管用户, 自己下载自己的简历，不更新状态
            return true;
        }

        resume.setType(ResumeTypeEnum.PROCESSING.getType());
        resume.setUpdateTime(new Date(System.currentTimeMillis()));
        boolean ans = userResumeDao.updateById(resume);
        if (ans) {
            resumeNotifyHelper.notifyToUser(resume);
        }
        return ans;
    }

    @Override
    public Boolean deleteResume(Long resumeId) {
        ResumeDO resume = userResumeDao.getById(resumeId);
        if (resume == null) {
            throw ExceptionUtil.of(StatusEnum.USER_RESUME_NOT_EXISTS);
        }
        BaseUserInfoDTO loginUser = ReqInfoContext.getReqInfo().getUser();
        if (Objects.equals(loginUser.getUserId(), resume.getUserId())
                || UserRole.ADMIN.name().equalsIgnoreCase(loginUser.getRole())) {
            // 管理员 || 拥有者，可以删除简历
            resume.setDeleted(YesOrNoEnum.YES.getCode());
            resume.setUpdateTime(new Date(System.currentTimeMillis()));
            return userResumeDao.updateById(resume);
        } else {
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR);
        }
    }

    /**
     * 回复
     *
     * @param req
     * @return
     */
    @Override
    public Boolean replayResume(UserResumeReplayReq req) {
        ResumeDO resume = userResumeDao.getById(req.getResumeId());
        resume.setReplay(req.getReplay());
        resume.setReplayUrl(req.getReplayUrl());
        resume.setType(ResumeTypeEnum.DONE.getType());
        resume.setUpdateTime(new Date(System.currentTimeMillis()));
        userResumeDao.updateById(resume);

        // 邮件回复用户
        long now = System.currentTimeMillis();
        resumeNotifyHelper.notifyToUser(resume);
        long end = System.currentTimeMillis();
        log.info("邮件通知耗时: {}", end - now);
        return true;
    }

    /**
     * 查询用户最近的一个简历
     *
     * @param userId
     * @return
     */
    @Override
    public ResumeDTO getLatestResume(Long userId) {
        UserResumeReq req = new UserResumeReq();
        req.setUserId(userId);
        req.setPageSize(1);
        req.setPageNum(1);
        req.setSort(1);
        List<ResumeDO> list = userResumeDao.listResumes(req);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return UserResumeConverter.toResume(list.get(0));
    }

    @Override
    public List<UserResumeInfoDTO> listResumes(UserResumeReq req) {
        if (req.getUserId() == null && StringUtils.isNotBlank(req.getUname())) {
            List<SimpleUserInfoDTO> users = userService.searchUser(req.getUname());
            if (CollectionUtils.isEmpty(users)) {
                return Collections.emptyList();
            } else {
                req.setUsers(users.stream().map(SimpleUserInfoDTO::getUserId).collect(Collectors.toList()));
            }
        }

        List<ResumeDO> list = userResumeDao.listResumes(req);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        Set<Long> uids = list.stream().map(ResumeDO::getUserId).collect(Collectors.toSet());
        List<SimpleUserInfoDTO> users = userService.batchQuerySimpleUserInfo(uids);
        Map<Long, SimpleUserInfoDTO> userMap = users.stream().collect(Collectors.toMap(SimpleUserInfoDTO::getUserId, s -> s));

        List<UserResumeInfoDTO> result = new ArrayList<>(list.size());
        list.forEach(resume -> {
            UserResumeInfoDTO info = new UserResumeInfoDTO();
            info.setResume(UserResumeConverter.toResume(resume));
            info.setUser(userMap.get(resume.getUserId()));
            result.add(info);
        });
        return result;
    }

    @Override
    public long count(UserResumeReq req) {
        return userResumeDao.count(req);
    }
}
