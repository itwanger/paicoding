export interface NoticeMsgType {
  msgId : number;

  /**
   * 消息关联的主体，如文章、评论
   */
  relatedId: string;

  /**
   * 关联信息
   */
  relatedInfo: string;

  /**
   * 发起消息的用户id
   */
  operateUserId: number;

  /**
   * 发起消息的用户名
   */
  operateUserName: string;

  /**
   * 发起消息的用户头像
   */
  operateUserPhoto: string;

  /**
   * 消息类型：0-默认，1-评论，2-回复，3-点赞，4-收藏，5-关注，6-系统消息
   */
  type: number;

  /**
   * 消息正文
   */
  msg: string;

  /**
   * 1 已读/ 0 未读
   */
  state: number;

  /**
   * 消息产生时间
   */
  createTime: string;
}
