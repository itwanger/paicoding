package com.github.paicoding.forum.service.user.service.resume;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.resume.ResumeTypeEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReplayReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReq;
import com.github.paicoding.forum.api.model.vo.user.UserResumeSaveReq;
import com.github.paicoding.forum.api.model.vo.user.dto.UserResumeDTO;
import com.github.paicoding.forum.service.user.converter.UserResumeConverter;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.dao.UserResumeDao;
import com.github.paicoding.forum.service.user.repository.entity.ResumeDO;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.UserResumeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 用户简历服务
 *
 * @author YiHui
 * @date 2024/8/7
 */
@Service
public class UserResumeServiceImpl implements UserResumeService {
    @Autowired
    private UserResumeDao userResumeDao;
    @Autowired
    private UserDao userDao;

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
        return userResumeDao.saveOrUpdate(resume);
    }

    /**
     * 上传简历的校验：
     * - 上传： 没有在途的简历时，才允许新传
     * - 更新： 简历必须是未处理状态，才支持更新
     *
     * @param req
     */
    private boolean preCheckForUpdateUserResume(UserResumeSaveReq req) {
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
            return true;
        }


        // 2. 上传简历
        // 查询用户的简历，判断是否存在 未处理 - 处理中 的简历，若存在，则只支持修改，不支持重新上传
        List<ResumeDO> list = userResumeDao.queryUserResumes(req.getUserId());
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        for (ResumeDO resume : list) {
            if (!Objects.equals(resume.getType(), ResumeTypeEnum.DONE.getType())) {
                throw ExceptionUtil.of(StatusEnum.SELF_DEFINE_ERROR, "您已经有简历再处理中了，暂不支持重新上传哦~");
            }
        }
        return true;
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

        resume.setType(ResumeTypeEnum.PROCESSING.getType());
        resume.setUpdateTime(new Date(System.currentTimeMillis()));
        boolean ans = userResumeDao.updateById(resume);
        if (ans) {
            // todo 发送邮件，通知用户邮件开始处理了
        }
        return ans;
    }


    /**
     * 回复
     *
     * @param req
     * @return
     */
    @Override
    public UserResumeDTO replayResume(UserResumeReplayReq req) {
        ResumeDO resume = userResumeDao.getById(req.getResumeId());
        resume.setReplay(req.getReplay());
        resume.setReplayUrl(req.getReplayUrl());
        resume.setType(ResumeTypeEnum.DONE.getType());
        resume.setUpdateTime(new Date(System.currentTimeMillis()));
        userResumeDao.updateById(resume);

        // todo 发送邮件，通知用户
        return UserResumeConverter.toResume(resume);
    }

    /**
     * 查询列表
     *
     * @param req
     * @return
     */
    @Override
    public List<UserResumeDTO> listResume(UserResumeReq req) {
        if (req.getUserId() == null && StringUtils.isNotBlank(req.getUname())) {
            UserDO user = userDao.getUserByUserName(req.getUname());
            if (user != null) {
                req.setUserId(user.getId());
            } else {
                return Collections.emptyList();
            }
        }
        List<ResumeDO> list = userResumeDao.listResumes(req);
        return UserResumeConverter.batchToResume(list);
    }
}
