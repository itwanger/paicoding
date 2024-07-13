/**
 * 触底加载下一页的通用方法
 * @param loadMoreSelector 选择器
 * @param url 向后端请求下一页的url
 * @param params 传参
 * @param listId 存放下一页内容的标签id
 * @param callback 回调函数
 */
const loadMore = function (loadMoreSelector, url, params, listId, callback) {
  let lastReqCondition = "" // 上一次请求条件
  let isNeedMore = true // 是否需要加载更多的标志

  // 滚动事件处理函数
  const handleScroll = () => {
    const scrollEle = document.querySelector("html") // 获取滚动元素

    const scrollTop =  window.pageYOffset || scrollEle.scrollTop  || document.body.scrollTop // 已滚动的距离
    const windowHeight = scrollEle.clientHeight // 可视区域的高度
    const scrollHeight = scrollEle.scrollHeight // 滚动条的总高度

    if (!isNeedMore) return false // 如果不需要加载更多，直接返回

    if (scrollTop + windowHeight + params["triggerThreshold"] >= scrollHeight) {
      // 生成本次请求的条件字符串
      let newReqCondition = params["category"] + "_" + params["page"]
      if (newReqCondition === lastReqCondition) {
        // 如果本次请求条件与上次相同，则不重复请求
        return
      }
      lastReqCondition = newReqCondition // 更新最后一次请求条件

      // 请求下一页数据
      nextPageText(
          url,
          params,
          listId,
          (hasMore) => {
            if (!hasMore) {
              isNeedMore = false // 更新是否需要加载更多的标志
            }
            params["page"] = params["page"] + 1 // 更新请求参数中的页码
            if (callback) {
              callback() // 执行回调函数
            }
          },
          () => {
            lastReqCondition = "" // 请求失败时重置最后一次请求条件
          }
      )
    }
  }
  window.addEventListener("scroll", handleScroll, true) // 添加滚动事件监听器
}