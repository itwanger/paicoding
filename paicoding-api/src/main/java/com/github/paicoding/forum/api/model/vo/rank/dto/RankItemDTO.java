package com.github.paicoding.forum.api.model.vo.rank.dto;

import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 排行榜信息
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Accessors(chain = true)
public class RankItemDTO {

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 评分
     */
    private Integer score;

    /**
     * 用户
     */
    private SimpleUserInfoDTO user;
}
