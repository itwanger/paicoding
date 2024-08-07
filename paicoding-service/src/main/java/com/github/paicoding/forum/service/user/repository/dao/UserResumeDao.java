package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.api.model.enums.YesOrNoEnum;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.user.UserResumeReq;
import com.github.paicoding.forum.service.user.repository.entity.ResumeDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserResumeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserResumeDao
 *
 * @author YiHui
 * @date 2024/08/07
 */
@Repository
public class UserResumeDao extends ServiceImpl<UserResumeMapper, ResumeDO> {

    /**
     * 查询用户的简历列表
     *
     * @param userId
     * @return
     */
    public List<ResumeDO> queryUserResumes(Long userId) {
        return lambdaQuery().eq(ResumeDO::getUserId, userId)
                .eq(ResumeDO::getDeleted, YesOrNoEnum.NO.getCode())
                .list();
    }


    public List<ResumeDO> listResumes(UserResumeReq req) {
        LambdaQueryWrapper<ResumeDO> query = Wrappers.lambdaQuery();
        // 设置用户过滤规则
        if (req.getUserId() != null) {
            query.eq(ResumeDO::getUserId, req.getUserId());
        }

        // 设置类型检索
        if (req.getType() != null) {
            query.eq(ResumeDO::getType, req.getType());
        }

        // 设置排序规则
        if (req.getSort() == null || req.getSort() == 0) {
            query.orderByAsc(ResumeDO::getCreateTime);
        } else {
            query.orderByDesc(ResumeDO::getCreateTime);
        }
        // 分页
        query.last(PageParam.getLimitSql(req));
        return baseMapper.selectList(query);
    }


}
