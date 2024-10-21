
// ========= 子评论 =========
import type { BaseCommentType } from '@/http/ResponseTypes/CommentType/BaseCommentType'

export interface SubCommentType extends BaseCommentType{
  parentContent: string;
}
