// ========== 评论  ===========
import type { SubCommentType } from '@/http/ResponseTypes/CommentType/SubCommentType'
import type { BaseCommentType } from '@/http/ResponseTypes/CommentType/BaseCommentType'
import { defaultBaseComment } from '@/http/ResponseTypes/CommentType/BaseCommentType'

export interface ArticleCommentType extends BaseCommentType{
  commentCount: number;
  childComments: SubCommentType[];
}

export const defaultArticleComment: ArticleCommentType = {
  commentCount: 0,
  ...defaultBaseComment,
  childComments: []
}
