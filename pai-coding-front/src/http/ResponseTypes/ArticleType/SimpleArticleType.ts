export interface SimpleArticleType {
  id: number
  title: string
  // 专栏id
  columnId: number
  column: string
  // 文章排序
  sort: number
  createTime: string
  /**
   * 0 免费阅读
   * 1 要求登录阅读
   * 2 限时免费，若当前时间超过限时免费期，则调整为登录阅读
   *
   */
  readType: number
}
