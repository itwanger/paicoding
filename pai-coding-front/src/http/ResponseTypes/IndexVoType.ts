// 定义标签的类型
import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'
import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import type { ArticleCategoryType } from '@/http/ResponseTypes/CategoryType/ArticleCategoryType'


// 定义轮播图项的类型
interface CarouselItem {
  Name: string;
  imgUrl: string;
  actionUrl: string;
}

// 定义主接口类型
export interface IndexVoResponse {
  categories: ArticleCategoryType[];
  currentCategory: string;
  categoryId: string;
  topArticles: ArticleType[];
  user: any | null;
  sideBarItems: SideBarItem[];
  homeCarouselList: CarouselItem[];
}

export const defaultIndexVoResponse = {
  categories: [],
  currentCategory: '',
  categoryId: '',
  topArticles: [],
  user: null,
  sideBarItems: [],
  homeCarouselList: [],
}
