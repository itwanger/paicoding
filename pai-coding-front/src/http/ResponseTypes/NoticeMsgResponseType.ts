import { type BasicPageType, defaultBasicPage } from '@/http/ResponseTypes/PageType/BasicPageType'
import type { NoticeMsgType } from '@/http/ResponseTypes/NoticeType/NoticeMsgType'

export interface NoticeMsgResponseType {
  list: BasicPageType<NoticeMsgType>
  unreadCountMap: {
    [key: string]: number;
  }
  /**
   * TODO 当前选中的消息类型，为了兼容后端，后续可以优化
   */
  selectType: string
}


export const defaultNoticeMsgResponse: NoticeMsgResponseType = {
  list: defaultBasicPage,
  unreadCountMap: {
    comment: 0,
    reply: 0,
    praise: 0,
    collect: 0,
    follow: 0,
    system: 0
  },
  selectType: ''
}
