package com.github.paicoding.forum.service.user.service.resume;

import com.github.paicoding.forum.api.model.enums.resume.ResumeEmailStateEnum;
import com.github.paicoding.forum.api.model.enums.resume.ResumeTypeEnum;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReq;
import com.github.paicoding.forum.core.async.AsyncExecute;
import com.github.paicoding.forum.core.util.EmailUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.user.repository.dao.UserResumeDao;
import com.github.paicoding.forum.service.user.repository.entity.ResumeDO;
import com.sun.org.apache.xpath.internal.functions.FuncTrue;
import io.lettuce.core.internal.Futures;
import nonapi.io.github.classgraph.utils.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 简历通知辅助类
 *
 * @author YiHui
 * @date 2024/8/9
 */
@Component
public class ResumeNotifyHelper {
    @Autowired
    private UserResumeDao userResumeDao;

    /**
     * 异步通知用户简历处理状态
     *
     * @param resume
     * @return
     */
    @AsyncExecute(backRun = true, timeOut = 30)
    public Future<Boolean> notifyToUser(ResumeDO resume) {
        boolean ans;
        ResumeEmailStateEnum stateEnum;
        if (Objects.equals(resume.getType(), ResumeTypeEnum.PROCESSING.getType())) {
            // 回复用户简历正在进行修改中
            ans = notifyProcessingToUser(resume);
            stateEnum = ResumeEmailStateEnum.PROCESSING_REPLAY;
        } else if (Objects.equals(resume.getType(), ResumeTypeEnum.DONE.getType())) {
            // 回复修改后的简历给用户
            ans = notifyDoneToUser(resume);
            stateEnum = ResumeEmailStateEnum.DONE_REPLAY;
        } else {
            // 用户首次提交，发送回复给用户
            ans = notifyResumeReceiveMsg(resume);
            stateEnum = ResumeEmailStateEnum.UPLOAD_REPLAY;
        }

        if (ans) {
            ans = userResumeDao.updateEmailState(resume.getId(), stateEnum);
        }
        return new AsyncResult<>(ans);
    }

    private boolean notifyResumeReceiveMsg(ResumeDO resume) {
        String title = "【技术派】您的简历已接收";

        // 查询总共有多少待处理的简历
        // fixme 正常应该查询当前简历前面未处理的简历数量，这里没有加这个限制，主要是因为再用户提交简历时，就来查询了，再并发不高的场景下，统计上基本不会有偏差
        UserResumeReq req = new UserResumeReq();
        req.setType(ResumeTypeEnum.UNPROCESS.getType());
        long cnt = userResumeDao.count(req);
        cnt = Math.max(cnt - 1, 0);

        String content = "您的简历已收到，前方尚有 <span color=\"red\">" + cnt + "</span> 封简历待处理，请耐心等待";
        return EmailUtil.sendMail(title, resume.getReplayEmail(), content);
    }

    private boolean notifyProcessingToUser(ResumeDO resume) {
        String title = "【技术派】您的简历正在处理中了~";
        String content = "简历正在仔细阅读中，预计半天内会有回复；请注意查看邮件哦~";
        return EmailUtil.sendMail(title, resume.getReplayEmail(), content);
    }

    private boolean notifyDoneToUser(ResumeDO resume) {
        String title = "【技术派】请查收修改后的简历";
        String content = resume.getReplay();

        String attach = resume.getReplayUrl();
        if (!attach.startsWith("http")) {
            attach = (String) ReflectionUtils.getFieldVal(SpringUtil.getBean("globalViewConfig"), "host", false) + attach;
        }
        return EmailUtil.sendMail(title, resume.getReplayEmail(), content, "(改)" + resume.getResumeName(), attach);
    }

}
