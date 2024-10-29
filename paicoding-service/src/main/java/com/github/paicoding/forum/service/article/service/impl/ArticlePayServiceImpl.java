package com.github.paicoding.forum.service.article.service.impl;

import com.github.paicoding.forum.api.model.enums.pay.PayStatusEnum;
import com.github.paicoding.forum.api.model.exception.ExceptionUtil;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticlePayInfoDTO;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.id.IdUtil;
import com.github.paicoding.forum.service.article.conveter.PayConverter;
import com.github.paicoding.forum.service.article.repository.dao.ArticlePayDao;
import com.github.paicoding.forum.service.article.repository.entity.ArticleDO;
import com.github.paicoding.forum.service.article.repository.entity.ArticlePayRecordDO;
import com.github.paicoding.forum.service.article.service.ArticlePayService;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * @author YiHui
 * @date 2024/10/29
 */
@Service
public class ArticlePayServiceImpl implements ArticlePayService {
    @Autowired
    private ArticleReadService articleReadService;
    @Autowired
    private UserService userService;

    @Autowired
    private ArticlePayDao articlePayDao;

    @Override
    public boolean hasPayed(Long article, Long currentUerId) {
        ArticlePayRecordDO dbRecord = articlePayDao.queryRecordByArticleId(article, currentUerId);
        if (dbRecord == null) {
            return false;
        }

        return PayStatusEnum.SUCCEED.getStatus().equals(dbRecord.getPayStatus());
    }

    /**
     * 唤起支付
     *
     * @param articleId     文章
     * @param currentUserId 当前用户
     */
    public ArticlePayInfoDTO toPay(Long articleId, Long currentUserId) {
        ArticlePayRecordDO dbRecord = articlePayDao.queryRecordByArticleId(articleId, currentUserId);
        if (dbRecord == null) {
            // 不存在时，创建一个
            dbRecord = createPayRecord(articleId, currentUserId);
        } else if (PayStatusEnum.FAIL.getStatus().equals(dbRecord.getPayStatus())) {
            // 对于原是支付失败的场景，再次支付时，充值相关信息
            dbRecord.setNotifyTime(null);
            dbRecord.setPayStatus(PayStatusEnum.NOT_PAY.getStatus());
            articlePayDao.updateById(dbRecord);
        }

        // 收款用户信息
        ArticlePayInfoDTO dto = PayConverter.toPay(dbRecord);
        BaseUserInfoDTO receiveUserInfo = userService.queryBasicUserInfo(dbRecord.getReceiveUserId());
        dto.setPayQrCodeMap(PayConverter.formatPayCode(receiveUserInfo.getPayCode()));
        return dto;
    }

    private ArticlePayRecordDO createPayRecord(Long articleId, Long currentUserId) {
        // fixme 这里需要做分布式防止重复写入
        ArticleDO articleDO = articleReadService.queryBasicArticle(articleId);
        if (articleDO == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, articleId);
        }

        ArticlePayRecordDO record = new ArticlePayRecordDO();
        record.setArticleId(articleId);
        record.setReceiveUserId(articleDO.getUserId());
        record.setPayUserId(currentUserId);
        record.setPayStatus(PayStatusEnum.NOT_PAY.getStatus());
        record.setNotifyTime(null);
        record.setNotes("");
        record.setId(IdUtil.genId());
        articlePayDao.save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePaying(Long payId, Long currentUserId) {
        ArticlePayRecordDO record = articlePayDao.selectForUpdate(payId);
        if (record.getPayUserId().equals(currentUserId)) {
            // 用户不一致，不支持更新
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR);
        }

        // 更新为已支付
        if (PayStatusEnum.NOT_PAY.getStatus().equals(record.getPayStatus())) {
            record.setPayStatus(PayStatusEnum.PAYING.getStatus());
            record.setUpdateTime(new Date());
            return articlePayDao.updateById(record);
        }

        return true;
    }

    /**
     * 更新支付状态
     *
     * @param payId
     * @param payStatus
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePayStatus(Long payId, Integer payStatus) {
        ArticlePayRecordDO dbRecord = articlePayDao.selectForUpdate(payId);
        if (dbRecord == null) {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "支付记录:" + payId);
        }
        if (Objects.equals(payStatus, dbRecord.getPayStatus())) {
            // 幂等
            return true;
        }

        if (PayStatusEnum.SUCCEED.getStatus().equals(dbRecord.getPayStatus())) {
            // 已经是支付成功，则不更新
            return true;
        } else {
            // 更新原来的支付状态为最新的结果
            dbRecord.setPayStatus(payStatus);
            dbRecord.setUpdateTime(new Date());
            return articlePayDao.updateById(dbRecord);
        }
    }
}
