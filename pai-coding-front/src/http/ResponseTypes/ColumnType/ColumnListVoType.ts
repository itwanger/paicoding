import type { BasicPageType } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { SideBarItem } from '@/http/ResponseTypes/SideBarItemType'

/**
 *          "columnId": "1",
 *          "column": "一灰灰的专栏",
 *          "introduction": "这里是小灰灰的技术专栏，欢迎关注",
 *          "cover": "https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4ba0bc79579c488eb79df93cecd12390~tplv-k3u1fbpfcp-watermark.image",
 *          "publishTime": "1663174800000",
 *          "section": 0,
 *          "state": 1,
 *          "nums": 100,
 *          "type": 2,
 *          "freeStartTime": "1671674400000",
 *          "freeEndTime": "1679277600000",
 *          "author": "1",
 *          "authorName": "管理员",
 *          "authorAvatar": "https://xuyifei-oss.oss-cn-beijing.aliyuncs.com/tech-pai/images/pexels-james-wheeler-1574181.jpg",
 *          "authorProfile": "码农",
 *          "count": {
 *            "praiseCount": 0,
 *            "readCount": 1,
 *            "collectionCount": 0,
 *            "commentCount": 0,
 *            "articleCount": 3,
 *            "totalNums": 100
 *          }
 */
export interface ColumnVoType {
  columnId: string
  column: string
  introduction: string
  cover: string
  publishTime: string
  section: number
  state: number
  nums: number
  type: number
  freeStartTime: string
  freeEndTime: string
  author: string
  authorName: string
  authorAvatar: string
  authorProfile: string
  count: {
    praiseCount: number
    readCount: number
    collectionCount: number
    commentCount: number
    articleCount: number
    totalNums: number
  }
}

export interface ColumnListVoTypeResponse {
  columns?: any
  columnPage: BasicPageType<ColumnVoType>
  sideBarItems: SideBarItem[]
}

export const  defaultColumnVoResponse: ColumnListVoTypeResponse = {
  columns: [],
  columnPage: {
    records: [],
    total: 0,
    size: 0,
    current: 0,
    orders: [],
    optimizeCountSql: false,
    searchCount: false,
    maxLimit: 0,
    countId: '',
    pages: 0
  },
  sideBarItems: []
}
