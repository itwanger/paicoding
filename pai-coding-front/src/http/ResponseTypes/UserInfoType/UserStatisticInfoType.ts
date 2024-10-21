import type { BaseUserInfo } from '@/http/ResponseTypes/UserInfoType/BaseUserInfoType'

export interface UserStatisticInfo extends BaseUserInfo{
  /**
   * 关注数
   */
  followCount: number;
  /**
   * 粉丝数
   */
  fansCount: number;
  /**
   * 加入天数
   */
  joinDayCount: number;
  /**
   * 已发布文章数
   */
  articleCount: number;
  /**
   * 文章点赞数
   */
  praiseCount: number;
  /**
   * 文章被阅读数
   */
  readCount: number;
  /**
   * 文章被收藏数
   */
  collectionCount: number;
  /**
   * 是否关注当前用户
   */
  followed: boolean;
  /**
   * 身份信息完整度百分比
   */
  infoPercent: number;
  /**
   * 创造历程
   */
  yearArticleList: Array<{
    year: string;
    articleCount: number;
  }>;
}

export const defaultUser: UserStatisticInfo = {
  userName: '',
  followCount: 0,
  fansCount: 0,
  joinDayCount: 0,
  articleCount: 0,
  praiseCount: 0,
  readCount: 0,
  collectionCount: 0,
  followed: false,
  infoPercent: 0,
  yearArticleList: []
}
