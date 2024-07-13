package com.github.paicoding.forum.service.rank.service.impl;

import com.github.paicoding.forum.api.model.enums.rank.ActivityRankTimeEnum;
import com.github.paicoding.forum.api.model.vo.rank.dto.RankItemDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.SimpleUserInfoDTO;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.core.util.DateUtil;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.service.rank.service.UserActivityRankService;
import com.github.paicoding.forum.service.rank.service.model.ActivityScoreBo;
import com.github.paicoding.forum.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class UserActivityRankServiceImpl implements UserActivityRankService {
    private static final String ACTIVITY_SCORE_KEY = "activity_rank_";

    @Autowired
    private UserService userService;

    /**
     * 当天活跃度排行榜
     *
     * @return 当天排行榜key
     */
    private String todayRankKey() {
        return ACTIVITY_SCORE_KEY + DateUtil.format(DateTimeFormatter.ofPattern("yyyyMMdd"), System.currentTimeMillis());
    }

    /**
     * 本月排行榜
     *
     * @return 月度排行榜key
     */
    private String monthRankKey() {
        return ACTIVITY_SCORE_KEY + DateUtil.format(DateTimeFormatter.ofPattern("yyyyMM"), System.currentTimeMillis());
    }

    /**
     * 添加活跃分
     *
     * @param userId
     * @param activityScore
     */
    @Override
    public void addActivityScore(Long userId, ActivityScoreBo activityScore) {
        if (userId == null) {
            return;
        }

        // 1. 计算活跃度(正为加活跃,负为减活跃)
        String field;
        int score = 0;
        if (activityScore.getPath() != null) {
            field = "path_" + activityScore.getPath();
            score = 1;
        } else if (activityScore.getArticleId() != null) {
            field = activityScore.getArticleId() + "_";
            if (activityScore.getPraise() != null) {
                field += "praise";
                score = BooleanUtils.isTrue(activityScore.getPraise()) ? 2 : -2;
            } else if (activityScore.getCollect() != null) {
                field += "collect";
                score = BooleanUtils.isTrue(activityScore.getCollect()) ? 2 : -2;
            } else if (activityScore.getRate() != null) {
                // 评论回复
                field += "rate";
                score = BooleanUtils.isTrue(activityScore.getRate()) ? 3 : -3;
            } else if (BooleanUtils.isTrue(activityScore.getPublishArticle())) {
                // 发布文章
                field += "publish";
                score += 10;
            }
        } else if (activityScore.getFollowedUserId() != null) {
            field = activityScore.getFollowedUserId() + "_follow";
            score = BooleanUtils.isTrue(activityScore.getFollow()) ? 2 : -2;
        } else {
            return;
        }

        final String todayRankKey = todayRankKey();
        final String monthRankKey = monthRankKey();
        // 2. 幂等：判断之前是否有更新过相关的活跃度信息
        final String userActionKey = ACTIVITY_SCORE_KEY + userId + DateUtil.format(DateTimeFormatter.ofPattern("yyyyMMdd"), System.currentTimeMillis());
        Integer ans = RedisClient.hGet(userActionKey, field, Integer.class);
        if (ans == null) {
            // 2.1 之前没有加分记录，执行具体的加分
            if (score > 0) {
                // 记录加分记录
                RedisClient.hSet(userActionKey, field, score);
                // 个人用户的操作记录，保存一个月的有效期，方便用户查询自己最近31天的活跃情况
                RedisClient.expire(userActionKey, 31 * DateUtil.ONE_DAY_SECONDS);

                // 更新当天和当月的活跃度排行榜
                Double newAns = RedisClient.zIncrBy(todayRankKey, String.valueOf(userId), score);
                RedisClient.zIncrBy(monthRankKey, String.valueOf(userId), score);
                if (log.isDebugEnabled()) {
                    log.info("活跃度更新加分! key#field = {}#{}, add = {}, newScore = {}", todayRankKey, userId, score, newAns);
                }
                if (newAns <= score) {
                    // 由于上面只实现了日/月活跃度的增加，但是没有设置对应的有效期；为了避免持久保存导致redis占用较高；因此这里设定了缓存的有效期
                    // 日活跃榜单，保存31天；月活跃榜单，保存1年
                    // 为什么是 newAns <= score 才设置有效期呢？
                    // 因为 newAns 是用户当天的活跃度，如果发现和需要增加的活跃度 scopre 相等，则表明是今天的首次添加记录，此时设置有效期就比较符合预期了
                    // 但是请注意，下面的实现有两个缺陷：
                    //  1. 对于月的有效期，就变成了本月，每天的首次增加活跃度时，都会重新刷一下它的有效期，这样就和预期中的首次添加缓存时，设置有效期不符
                    //  2. 若先增加活跃度1，再减少活跃度1，然后再加活跃度1，同样会导致重新算了有效期
                    // 严谨一些的写法，应该是 先判断 key 的 ttl， 对于没有设置的才进行设置有效期，如下
                    Long ttl = RedisClient.ttl(todayRankKey);
                    if (!NumUtil.upZero(ttl)) {
                        RedisClient.expire(todayRankKey, 31 * DateUtil.ONE_DAY_SECONDS);
                    }
                    ttl = RedisClient.ttl(monthRankKey);
                    if (!NumUtil.upZero(ttl)) {
                        RedisClient.expire(monthRankKey, 12 * DateUtil.ONE_MONTH_SECONDS);
                    }
                }
            }
        } else if (ans > 0) {
            // 2.2 之前已经加过分，因此这次减分可以执行
            if (score < 0) {
                // 移除用户的活跃执行记录 --> 即移除用来做防重复添加活跃度的幂等键
                Boolean oldHave = RedisClient.hDel(userActionKey, field);
                if (BooleanUtils.isTrue(oldHave)) {
                    Double newAns = RedisClient.zIncrBy(todayRankKey, String.valueOf(userId), score);
                    RedisClient.zIncrBy(monthRankKey, String.valueOf(userId), score);
                    if (log.isDebugEnabled()) {
                        log.info("活跃度更新减分! key#field = {}#{}, add = {}, newScore = {}", todayRankKey, userId, score, newAns);
                    }
                }
            }
        }
    }

    @Override
    public RankItemDTO queryRankInfo(Long userId, ActivityRankTimeEnum time) {
        RankItemDTO item = new RankItemDTO();
        item.setUser(userService.querySimpleUserInfo(userId));

        String rankKey = time == ActivityRankTimeEnum.DAY ? todayRankKey() : monthRankKey();
        ImmutablePair<Integer, Double> rank = RedisClient.zRankInfo(rankKey, String.valueOf(userId));
        item.setRank(rank.getLeft());
        item.setScore(rank.getRight().intValue());
        return item;
    }

    @Override
    public List<RankItemDTO> queryRankList(ActivityRankTimeEnum time, int size) {
        String rankKey = time == ActivityRankTimeEnum.DAY ? todayRankKey() : monthRankKey();
        // 1. 获取topN的活跃用户
        List<ImmutablePair<String, Double>> rankList = RedisClient.zTopNScore(rankKey, size);
        if (CollectionUtils.isEmpty(rankList)) {
            return Collections.emptyList();
        }

        // 2. 查询用户对应的基本信息
        // 构建userId -> 活跃评分的map映射，用于补齐用户信息
        Map<Long, Integer> userScoreMap = rankList.stream().collect(Collectors.toMap(s -> Long.valueOf(s.getLeft()), s -> s.getRight().intValue()));
        List<SimpleUserInfoDTO> users = userService.batchQuerySimpleUserInfo(userScoreMap.keySet());

        // 3. 根据评分进行排序
        List<RankItemDTO> rank = users.stream()
                .map(user -> new RankItemDTO().setUser(user).setScore(userScoreMap.getOrDefault(user.getUserId(), 0)))
                .sorted((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()))
                .collect(Collectors.toList());

        // 4. 补齐每个用户的排名
        IntStream.range(0, rank.size()).forEach(i -> rank.get(i).setRank(i + 1));
        return rank;
    }
}
