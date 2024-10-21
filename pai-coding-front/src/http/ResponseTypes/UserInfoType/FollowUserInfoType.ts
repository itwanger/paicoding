/**
 * 关注者用户信息
 */
export interface FollowUserInfoType {
  /**
   * 当前登录的用户与这个用户之间的关联关系id
   */
  relationId: number

  /**
   * true 表示当前登录用户关注了这个用户
   * false 标识当前登录用户没有关注这个用户
   */
  followed: boolean

  /**
   * 用户id
   */
  userId: number

  /**
   * 用户名
   */
  userName: string

  /**
   * 用户头像
   */
  avatar: string
}
