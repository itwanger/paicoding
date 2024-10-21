import type { ArticleType } from '@/http/ResponseTypes/ArticleType/ArticleType'
import type { ArticleCategoryType } from '@/http/ResponseTypes/CategoryType/ArticleCategoryType'
import type { ArticleTagType } from '@/http/ResponseTypes/TagType/ArticleTagType'

export interface ArticleEditResponseType{
  article: ArticleType,
  categories: ArticleCategoryType[],
  tags: ArticleTagType[]
}
