interface SiteInfo {
  cdnImgStyle: string;
  websiteRecord: string;
  pageSize: number;
  websiteName: string;
  websiteLogoUrl: string;
  websiteFaviconIconUrl: string;
  contactMeWxQrCode: string;
  contactMeStarQrCode: string;
  contactMeTitle: string;
  wxLoginUrl: string;
  host: string;
  welcomeInfo: string;
  starInfo: string;
  oss: string;
  needLoginArticleReadCount: string;
}

interface SiteStatisticInfo {
  day: string | null;
  path: string | null;
  pv: number;
  uv: number;
}

interface Ogp {
  key: string;
  val: string;
  Val?: string; // 可能是拼写错误的字段名，需要兼容
}

export interface UserInfo{
  company: string;
  createTime: string;
  deleted: number;
  extend: string;
  id: string;
  photo: string;
  position: string;
  profile: string;
  region: string;
  role: string;
  starStatus: string;
  updateTime: string;
  userId: string;
  userName: string;
}

export interface GlobalResponse {
  siteInfo: SiteInfo;
  siteStatisticInfo: SiteStatisticInfo;
  todaySiteStatisticInfo: SiteStatisticInfo;
  env: string;
  isLogin: boolean;
  user: UserInfo; // 根据实际情况替换 `any` 为具体的用户类型
  msgNum: number | null;
  onlineCnt: number;
  currentDomain: string;
  ogp: Ogp[];
  jsonLd: string;
}


export const defaultGlobalResponse: GlobalResponse = {
  siteInfo: {
    cdnImgStyle: '',
    websiteRecord: '',
    pageSize: 0,
    websiteName: '',
    websiteLogoUrl: '',
    websiteFaviconIconUrl: '',
    contactMeWxQrCode: '',
    contactMeStarQrCode: '',
    contactMeTitle: '',
    wxLoginUrl: '',
    host: '',
    welcomeInfo: '',
    starInfo: '',
    oss: '',
    needLoginArticleReadCount: '',
  },
  siteStatisticInfo: {
    day: null,
    path: null,
    pv: 0,
    uv: 0,
  },
  todaySiteStatisticInfo: {
    day: '',
    path: null,
    pv: 0,
    uv: 0,
  },
  env: '',
  isLogin: false,
  user: {
    company: '',
    createTime: '',
    deleted: 0,
    extend: '',
    id: '',
    photo: '',
    position: '',
    profile: '',
    region: '',
    role: '',
    starStatus: '',
    updateTime: '',
    userId: '',
    userName: '',
  },
  msgNum: null,
  onlineCnt: 0,
  currentDomain: '',
  ogp: [],
  jsonLd: '',
};

export interface CommonResponse<T=any> {
  global: GlobalResponse;
  result: T;
  status: {code: number, msg: string};
  redirect: boolean
}


export const requestResponse: CommonResponse = {
  global: defaultGlobalResponse,
  result: {},
  status: {code: -1, msg: ''},
  redirect: false
}
