// import type { UserInfoTagSelectType } from '@/http/ResponseTypes/UserInfoType/UserInfoTagSelectType'
// import { type BasicPageType, defaultBasicPage } from '@/http/ResponseTypes/PageType/BasicPageType'
// import type { FollowUserInfoType } from '@/http/ResponseTypes/UserInfoType/FollowUserInfoType'
// import { defaultUser, type UserStatisticInfo } from '@/http/ResponseTypes/UserInfoType/UserStatisticInfoType'
// import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
//
// export interface UserHomeInfoResponseType {
//   /**
//    * 当前选择的选项卡
//    */
//   homeSelectType: string
//   /**
//    * 所有的选项卡
//    */
//   homeSelectTags: UserInfoTagSelectType[]
//   /**
//    * 关注列表/粉丝列表
//    */
//   followList: BasicPageType<FollowUserInfoType>
//
//   /**
//    * 关注列表/粉丝列表
//    * 取值为 follow 或者 fans
//    */
//   followSelectType: string
//   /**
//    * 全部选项
//    */
//   followSelectTags: UserInfoTagSelectType[]
//   /**
//    * 用户信息
//    */
//   userHome: UserStatisticInfo
//   /**
//    * 文章列表
//    */
//   homeSelectList: BasicPageType<ArticleType>
// }
//
// export const defaultUserHomeInfo: UserHomeInfoResponseType = {
//   homeSelectType: '',
//   homeSelectTags: [],
//   followList: defaultBasicPage,
//   followSelectType: '',
//   followSelectTags: [],
//   userHome: defaultUser,
//   homeSelectList: defaultBasicPage
// }

import { defaultUser, type UserStatisticInfo } from '@/http/ResponseTypes/UserInfoType/UserStatisticInfoType'

// 用户基本信息
export interface UserHomeInfoResponseType {
  /**
   * 用户信息
   */
  userHome: UserStatisticInfo
}

export const defaultUserHomeInfo: UserHomeInfoResponseType = {
  userHome: defaultUser,
}
