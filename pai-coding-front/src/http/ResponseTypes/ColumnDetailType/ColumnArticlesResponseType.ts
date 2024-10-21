import type { ArticleCommentType } from '@/http/ResponseTypes/CommentType/ArticleCommentType'
import type { SimpleArticleType } from '@/http/ResponseTypes/ArticleType/SimpleArticleType'
import type { ArticleOtherType } from '@/http/ResponseTypes/ArticleType/ArticleOtherType'
import { defaultArticleOther } from '@/http/ResponseTypes/ArticleType/ArticleOtherType'
import { defaultArticleComment } from '@/http/ResponseTypes/CommentType/ArticleCommentType'
import { type ArticleType, defaultArticle } from '@/http/ResponseTypes/ArticleType/ArticleType'

export interface ColumnArticlesResponseType{
  // 当前专栏的id
  column: number
  // 当前查看的文章的id
  section: number
  article: ArticleType
  /**
   * 0 免费阅读
   * 1 要求登录阅读
   * 2 限时免费，若当前时间超过限时免费期，则调整为登录阅读
   *
   */
  readType: number
  comments: ArticleCommentType[]
  hotComment: ArticleCommentType
  articleList: SimpleArticleType[]
  other: ArticleOtherType

}

export const defaultColumnArticlesResponse: ColumnArticlesResponseType = {
  column: 0,
  section: 0,
  article: {...defaultArticle},
  readType: 0,
  comments: [],
  hotComment: defaultArticleComment,
  articleList: [],
  other: defaultArticleOther,
}
