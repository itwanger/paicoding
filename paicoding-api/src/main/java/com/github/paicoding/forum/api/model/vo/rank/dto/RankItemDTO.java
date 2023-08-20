package com.github.paicoding.forum.api.model.vo.rank.dto;

import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import lombok.Data;

/**
 * 排行榜信息
 *
 * @author YiHui
 * @date 2023/8/19
 */
@Data
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
