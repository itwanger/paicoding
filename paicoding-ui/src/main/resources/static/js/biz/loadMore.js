/**
 * 触底加载下一页的通用方法
 * @param loadMoreSelector 选择器
 * @param url 向后端请求下一页的url
 * @param params 传参
 * @param listId 存放下一页内容的标签id
 */
const loadMore = function (loadMoreSelector, url, params, listId, callback) {
  let lastReqCondition = ""
  let isNeedMore = true
  const handleScroll = () => {
    const scrollEle = document.querySelector("html")

    const scrollTop = scrollEle.scrollTop
    //变量windowHeight是可视区的高度
    const windowHeight = scrollEle.clientHeight
    //变量scrollHeight是滚动条的总高度
    const scrollHeight = scrollEle.scrollHeight
    if (!isNeedMore) return false

    //一般来说需要提前触发:scrollTop+windowHeight + 200 >=scrollHeight
    if (scrollTop + windowHeight + 100 >= scrollHeight) {
      // 模拟加载新的数据
      let newReqCondition = params["category"] + "_" + params["page"]
      if (newReqCondition === lastReqCondition) {
        // 和上次的请求参数相同，做一个幂等处理
        return
      }
      lastReqCondition = newReqCondition
      console.log("触底了，当前请求加载下一页参数：", params)
      nextPageText(
        url,
        params,
        listId,
        (hasMore) => {
          if (!hasMore) {
            isNeedMore = false
          }
          params["page"] = params["page"] + 1
          if (callback) {
            callback()
          }
        },
        () => {
          lastReqCondition = ""
        }
      )
    }
  }
  window.addEventListener("scroll", handleScroll, true)
}
