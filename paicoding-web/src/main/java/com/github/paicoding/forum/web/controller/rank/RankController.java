package com.github.paicoding.forum.web.controller.rank;

import com.github.paicoding.forum.api.model.enums.rank.ActivityRankTimeEnum;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.rank.dto.RankInfoDTO;
import com.github.paicoding.forum.api.model.vo.rank.dto.RankItemDTO;
import com.github.paicoding.forum.service.rank.service.UserActivityRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 排行榜
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Controller
public class RankController {
    @Autowired
    private UserActivityRankService userActivityRankService;

    /**
     * 活跃用户排行榜
     *
     * @param time
     * @param model
     * @return
     */
    @RequestMapping(path = "/rank/{time}")
    public String rank(@PathVariable(value = "time") String time, Model model) {
        ActivityRankTimeEnum rankTime = ActivityRankTimeEnum.nameOf(time);
        if (rankTime == null) {
            rankTime = ActivityRankTimeEnum.MONTH;
        }
        List<RankItemDTO> list = userActivityRankService.queryRankList(rankTime, 30);
        RankInfoDTO info = new RankInfoDTO();
        info.setItems(list);
        info.setTime(rankTime);
        ResVo<RankInfoDTO> vo = ResVo.ok(info);
        model.addAttribute("vo", vo);
        return "views/rank/index";
    }
}
