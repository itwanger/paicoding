import type { SimpleUserInfo } from '@/http/ResponseTypes/UserInfoType/SimpleUserInfoType'
import type { ArticleCategoryType } from '@/http/ResponseTypes/CategoryType/ArticleCategoryType'

interface Tag {
  tagId: string;
  tag: string;
  status: number | null;
  selected: boolean | null;
}


// 定义文章的统计信息类型
interface ArticleCount {
  praiseCount: number;
  readCount: number;
  collectionCount: number;
  commentCount: number;
}

export interface ArticleType{
  articleId: string;
  /**
   * 文章类型：1-博文，2-问答
   */
  articleType: number;
  /**
   * 作者uid
   */
  author: string;
  authorName: string;
  authorAvatar: string;
  title: string;
  shortTitle: string;
  summary: string;
  /**
   * 封面
   */
  cover: string;
  content: string;
  /**
   * 文章来源
   *
   * @see DocumentSourceTypeEnum
   */
  sourceType: string;
  sourceUrl: string;
  status: number;
  officalStat: number;
  toppingStat: number;
  creamStat: number;
  createTime: string;
  lastUpdateTime: string;
  category: ArticleCategoryType;
  tags: Tag[];
  praised: boolean | null;
  commented: boolean | null;
  collected: boolean | null;
  count: ArticleCount;
  praisedUsers: SimpleUserInfo[] | null;
}

export const defaultArticle: ArticleType = {
  articleId: '',
  articleType: 0,
  author: '',
  authorName: '',
  authorAvatar: '',
  title: '',
  shortTitle: '',
  summary: '',
  cover: '',
  content: '',
  sourceType: '',
  sourceUrl: '',
  status: 0,
  officalStat: 0,
  toppingStat: 0,
  creamStat: 0,
  createTime: '',
  lastUpdateTime: '',
  category: {
    categoryId: 0,
    category: '',
    rank: 0,
    status: 0,
  },
  tags: [],
  praised: false,
  commented: false,
  collected: false,
  count: {
    praiseCount: 0,
    readCount: 0,
    collectionCount: 0,
    commentCount: 0
  },
  praisedUsers: []

}
