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
  const stateKey = buildLoadMoreStateKey(url, params, listId)
  const scrollEle = document.scrollingElement || document.documentElement

  if (!params["triggerThreshold"]) {
      // 定义一个检测是否为微信浏览器的函数
      const isWeixinBrowser = () => {
          const userAgent = navigator.userAgent.toLowerCase();
          return userAgent.includes('micromessenger');
      }
      let triggerThreshold = 100;
      // 如果是微信浏览器
      if (isWeixinBrowser()) {
          triggerThreshold = 400;
      }
      params["triggerThreshold"] = triggerThreshold;
  }

  restoreLoadMoreState(stateKey, params, listId, function (state) {
    lastReqCondition = state.lastReqCondition || ""
    isNeedMore = state.isNeedMore !== false
  })

  const saveState = function () {
    saveLoadMoreState(stateKey, params, listId, isNeedMore, lastReqCondition)
  }

  // 滚动事件处理函数
  const handleScroll = () => {
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
            saveState()
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
  window.addEventListener("pagehide", saveState)
  document.addEventListener("visibilitychange", function () {
    if (document.visibilityState === "hidden") {
      saveState()
    }
  })
  const listEle = document.getElementById(listId)
  if (listEle) {
    listEle.addEventListener("click", function (event) {
      const link = event.target.closest && event.target.closest("a[href]")
      if (link) {
        saveState()
      }
    }, true)
  }
}

const buildLoadMoreStateKey = function (url, params, listId) {
  const stateParams = {}
  Object.keys(params || {}).sort().forEach(function (key) {
    if (key !== "page" && key !== "triggerThreshold") {
      stateParams[key] = params[key]
    }
  })
  return [
    "loadMore",
    location.pathname,
    location.search,
    url,
    listId,
    JSON.stringify(stateParams)
  ].join(":")
}

const markLoadMoreHistoryState = function (stateKey) {
  if (!window.history || !window.history.replaceState) {
    return
  }
  const currentState = window.history.state || {}
  const loadMoreStateKeys = Object.assign({}, currentState.loadMoreStateKeys || {})
  loadMoreStateKeys[stateKey] = true
  window.history.replaceState(Object.assign({}, currentState, {
    loadMoreStateKeys: loadMoreStateKeys
  }), document.title, location.href)
}

const shouldRestoreLoadMoreState = function (stateKey) {
  const currentState = window.history && window.history.state
  return !!(currentState && currentState.loadMoreStateKeys && currentState.loadMoreStateKeys[stateKey])
}

const saveLoadMoreState = function (stateKey, params, listId, isNeedMore, lastReqCondition) {
  const listEle = document.getElementById(listId)
  if (!listEle) {
    return
  }

  try {
    const storage = window.sessionStorage
    if (!storage) {
      return
    }
    const scrollEle = document.scrollingElement || document.documentElement
    storage.setItem(stateKey, JSON.stringify({
      html: listEle.innerHTML,
      page: params["page"],
      isNeedMore: isNeedMore,
      lastReqCondition: lastReqCondition,
      scrollTop: window.pageYOffset || scrollEle.scrollTop || document.body.scrollTop || 0
    }))
    markLoadMoreHistoryState(stateKey)
  } catch (e) {
    console.warn("save load more state failed", e)
  }
}

const restoreLoadMoreState = function (stateKey, params, listId, callback) {
  if (!shouldRestoreLoadMoreState(stateKey)) {
    return
  }

  try {
    const storage = window.sessionStorage
    if (!storage) {
      return
    }
    const rawState = storage.getItem(stateKey)
    if (!rawState) {
      return
    }

    const state = JSON.parse(rawState)
    const listEle = document.getElementById(listId)
    if (!state || !listEle || !state.html) {
      return
    }

    listEle.innerHTML = state.html
    params["page"] = state.page || params["page"]
    if (callback) {
      callback(state)
    }

    requestAnimationFrame(function () {
      window.scrollTo(0, state.scrollTop || 0)
    })
  } catch (e) {
    console.warn("restore load more state failed", e)
  }
}
