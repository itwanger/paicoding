// ============= 后端请求的地址 =============
// 后端接口地址

export const EXCEL_PROCESS_BASE_URL = "https://www.xuyifei.site:5000"

//
export const BASE_URL = "http://localhost:8081"
export const WS_URL = "ws://localhost:8081"


// 获得global信息还有siteInfo信息
export const INDEX_URL = "/index"

// 用户名密码登录
export const LOGIN_USER_NAME_URL = "/new/login/username"
// 退出登录
export const LOGOUT_URL = "/logout"

// 随机用户登录，需要模拟微信公众号平台给后端发请求的地址
export const MOCK_LOGIN_URL = "/wx/callback"

// 获取指定文章的详情
export const ARTICLE_DETAIL_URL = "article/api/data/detail"

// 获取文章对应的专栏信息
export const ARTICLE_COLUMN_RELATION_URL = "column/api/article"

// 文章点赞、收藏
export const ARTICLE_LIKE_COLLECT_URL = "article/api/favor"

// 评论点赞
export const COMMENT_LIKE_URL = "comment/api/favor"

// 提交评论
export const COMMENT_SUBMIT_URL = "comment/api/save"


// ############# 首页相关的请求 #############
// 获取指定category下的文章列表
export const CATEGORY_ARTICLE_LIST_URL = "article/api/articles/category"


// ############# column专栏相关的请求 #############
// 获取专栏列表，这里用/home是兼容管理系统中的代码
export const COLUMN_LIST_URL = "column/api/home"

// 获取专栏文章详情，后面要加上专栏id和文章id如：/column/api/1/1
export const COLUMN_DETAIL_URL = "column/api"

// ############# 文章分类相关的请求 #############
// 获取文章分类列表
export const CATEGORY_LIST_URL = "/api/category/list/all"

// ############# 文章标签相关的请求 #############
// 获取文章标签列表(未被标记为删除的)
export const TAG_LIST_URL = "/api/tag/list/all"
// 获取指定category下的 tags
export const ARTICLE_TAGS_URL = "/api/tag/list/category"

// ############# 文章相关的请求 #############
// 上传图片
export const FILE_UPLOAD_URL = "/image/upload"
// 上传/更新文章
export const ARTICLE_UPLOAD_URL = "/article/api/post"
// 更新（编辑）文章时获取的文章详情
export const ARTICLE_EDIT_URL = "/article/api/update"
// 删除文章
export const ARTICLE_DELETE_URL = "/article/api/delete"


// ############# 用户相关的请求 #############
// 获取用户信息
export const USER_INFO_URL = "/user/api/home"
// 获取用户文章列表
export const USER_ARTICLE_LIST_URL = "/user/api/articles"
// 获取用户浏览记录列表
export const USER_HISTORY_LIST_URL = "/user/api/history"
// 获取用户收藏列表
export const USER_STAR_LIST_URL = "/user/api/star"
// 获取用户关注的用户的列表
export const USER_FOLLOW_LIST_URL = "/user/api/follows"
// 获取用户粉丝列表
export const USER_FANS_LIST_URL = "/user/api/fans"
// 保存用户信息
export const USER_INFO_SAVE_URL = "/user/api/saveUserInfo"
// 关注/取关用户
export const USER_FOLLOW_URL = "/user/api/saveUserRelation"

// ############# 消息通知相关的请求 #############
// 获取未读通知
export const UNREAD_NOTICE_URL = "/notice/api/messages"

// ============= 全局信息获取的地址 =============
// 这里主要是有些页面在刷新时并不需要重新请求数据，所以在这里获取全局信息
export const GLOBAL_INFO_URL = "/api/global/info"

// ============= 前端跳转的地址 =============
export const WRITE_ARTICLE_URL = "/article/edit"

// ============= 额外的后端工具服务地址 =============
// excel处理地址
export const EXCEL_PROCESS_URL = "/tools/transfer"
