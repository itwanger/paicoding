export interface ArticleOtherType {
  readType: number;
  flip?: {
    prevHref: string;
    prevShow: boolean;
    nextHref: string;
    nextShow: boolean;
  }
}

export const defaultArticleOther: ArticleOtherType = {
  readType: 0,
  flip: {
    prevHref: '',
    prevShow: false,
    nextHref: '',
    nextShow: false,
  }
}
