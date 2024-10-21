export interface ArticleTagType{
  tagId: number
  tag: string
  /**
   * 1是已发布，0是未发布
   */
  status: number
  selected: boolean
}
