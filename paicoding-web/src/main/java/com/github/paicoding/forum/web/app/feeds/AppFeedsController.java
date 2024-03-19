package com.github.paicoding.forum.web.app.feeds;

import com.github.paicoding.forum.api.model.vo.PageListVo;
import com.github.paicoding.forum.api.model.vo.PageParam;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.feed.FeedSaveReq;
import com.github.paicoding.forum.api.model.vo.feed.dto.FeedInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.feed.service.FeedReadService;
import com.github.paicoding.forum.service.feed.service.FeedWriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * app广场信息流
 *
 * @author YiHui
 * @date 2024/3/18
 */
@Api(tags = {"APP广场"}, value = "app广场信息流相关接口")
@RestController
@RequestMapping(path = "app/feed")
public class AppFeedsController {
    @Autowired
    private FeedReadService feedReadService;

    @Autowired
    private FeedWriteService feedWriteService;


    /**
     * 发表or更新feed
     *
     * @return
     */
    @PostMapping(path = "save")
    @Permission(role = UserRole.LOGIN)
    @ApiOperation(tags = "APP广场", value = "feed保存", notes = "feed保存")
    public ResVo<Boolean> save(@RequestBody FeedSaveReq req) {
        feedWriteService.save(req);
        return ResVo.ok(true);
    }


    @ApiOperation(tags = "APP广场", value = "feed流", notes = "动态流")
    @GetMapping(path = "list")
    public ResVo<PageListVo<FeedInfoDTO>> list(@RequestParam(value = "type", required = false) Integer type,
                                               @RequestParam("page") Long page,
                                               @RequestParam("size") Long size) {
        PageParam pageParam = PageParam.buildPageParam(page, size);
        List<FeedInfoDTO> list;
        if (type == null || type == 0) {
            // 点赞排序
            list = feedReadService.queryFeedsByPraiseCount(pageParam);
        } else if (type == 1) {
            // 时间
            list = feedReadService.queryFeedsByTime(pageParam);
        } else {
            // 只看关注
            list = feedReadService.queryFeedsByFollowedUser(pageParam);
        }

        return ResVo.ok(PageListVo.newVo(list, pageParam.getPageSize()));
    }
}
