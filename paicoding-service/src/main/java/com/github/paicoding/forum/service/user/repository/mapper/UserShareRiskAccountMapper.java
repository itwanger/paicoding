package com.github.paicoding.forum.service.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.paicoding.forum.service.user.repository.entity.UserShareRiskAccountDO;
import org.apache.ibatis.annotations.Insert;

/**
 * 疑似共享账号状态
 *
 * @author Codex
 * @date 2026/5/12
 */
public interface UserShareRiskAccountMapper extends BaseMapper<UserShareRiskAccountDO> {
    @Insert("insert into user_share_risk_account("
            + "user_id, login_name, star_number,"
            + " kickout_count, login_success_count, device_count, ip_count,"
            + " last_kickout_time, last_active_time,"
            + " risk_level, risk_reason,"
            + " forbidden, forbid_until, forbid_reason, forbid_operator_id,"
            + " recent_days, last_release_at, last_handle_reason) values("
            + " #{userId}, #{loginName}, #{starNumber},"
            + " #{kickoutCount}, #{loginSuccessCount}, #{deviceCount}, #{ipCount},"
            + " #{lastKickoutTime}, #{lastActiveTime},"
            + " #{riskLevel}, #{riskReason},"
            + " #{forbidden}, #{forbidUntil}, #{forbidReason}, #{forbidOperatorId},"
            + " #{recentDays}, #{lastReleaseAt}, #{lastHandleReason})"
            + " on duplicate key update"
            + " login_name = values(login_name),"
            + " star_number = values(star_number),"
            + " kickout_count = values(kickout_count),"
            + " login_success_count = values(login_success_count),"
            + " device_count = values(device_count),"
            + " ip_count = values(ip_count),"
            + " last_kickout_time = values(last_kickout_time),"
            + " last_active_time = values(last_active_time),"
            + " risk_level = values(risk_level),"
            + " risk_reason = values(risk_reason),"
            + " forbidden = values(forbidden),"
            + " forbid_until = values(forbid_until),"
            + " forbid_reason = values(forbid_reason),"
            + " forbid_operator_id = values(forbid_operator_id),"
            + " recent_days = values(recent_days),"
            + " last_release_at = coalesce(values(last_release_at), last_release_at),"
            + " last_handle_reason = values(last_handle_reason)")
    int upsert(UserShareRiskAccountDO account);
}
