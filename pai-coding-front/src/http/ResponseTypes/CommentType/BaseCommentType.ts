export interface BaseCommentType{
  commentContent: string;
  commentId: string;
  commentTime: string;
  praiseCount: number;
  praised: boolean;
  userId: string;
  userName: string;
  userPhoto: string
}

export const defaultBaseComment: BaseCommentType = {
  commentContent: '',
  commentId: '',
  commentTime: '',
  praiseCount: 0,
  praised: false,
  userId: '',
  userName: '',
  userPhoto: ''
}
