const post = function (path, data, callback) {
  $.ajax({
    method: "POST",
    url: path,
    contentType: "application/json",
    data: JSON.stringify(data),
    success: function (data) {
      console.log("data", data)
      if (!data || !data.status || data.status.code != 0) {
        // 出现了
        console.log("出现了异常:", data.status.msg)
        toastr.error(data.status.msg)
      } else if (callback) {
        callback(data.result)
      }
    },
    error: function (data) {
      toastr.error(data, "出现bug了，热心反馈下吧!")
    },
  })
}

const get = function (url, params, callback) {
  $.get(url, params, function (data) {
    console.log("response: ", data)
    if (!data || !data.status || data.status.code != 0) {
      // 出现了
      console.log("出现了异常:", data.status.msg)
      toastr.error(data.status.msg)
      return
    } else if (callback) {
      callback(data.result)
    }
  })
}

const loadScript = function (url, callback) {
  const secScript = document.createElement("script")
  if (secScript.readyState) {
    // IE
    secScript.onreadystatechange = function () {
      if (
        secScript.readyState === "loaded" ||
        secScript.readyState === "complete"
      ) {
        secScript.onreadystatechange = null
        callback()
      }
    }
  } else {
    // 其他浏览器
    secScript.onload = function () {
      callback()
    }
  }
  secScript.setAttribute("type", "text/javascript")
  secScript.setAttribute("src", url)
  document.body.insertBefore(secScript, document.body.lastChild)
}

const loadLink = function (url) {
  let headHTML = document.getElementsByTagName("head")[0].innerHTML
  headHTML += '<link href="' + url + '" rel="stylesheet">'
  document.getElementsByTagName("head")[0].innerHTML = headHTML
}

//建立一?可存取到?file的url
const getObjectURL = function (file) {
  let url = null

  if (window.createObjectURL != undefined) {
    // basic
    url = window.createObjectURL(file)
  } else if (window.URL != undefined) {
    // mozilla(firefox)
    url = window.URL.createObjectURL(file)
  } else if (window.webkitURL != undefined) {
    // webkit or chrome
    url = window.webkitURL.createObjectURL(file)
  }
  return url
}

/**
 * 请求获取下一页内容
 * @param url
 * @param params
 * @param listId
 * @param btnId
 * @param callback
 */
const doGetNextPage = function (url, params, listId, btnId, callback) {
  $.get(url, params, function (data) {
    console.log("response: ", data)
    const result = data.result
    $(`#${listId}`).append(result.html)
    if (!result.hasMore) {
      $(`#${btnId}`).hide()
    } else {
      $(`#${btnId}`).show()
    }

    if (callback) {
      callback("true")
    }
  })
}
const doGetNextPageText = function (
  url,
  params,
  listId,
  callback,
  errorCallBack
) {
  $.get(url, params, function (data) {
    console.log("response: ", data)
    const result = data.result
    $(`#${listId}`).append(result.html)
    if (!result.hasMore) {
      callback(false)
    }
    if (callback) {
      callback(true)
    }
  }).fail(errorCallBack)
}

/**
 * 翻页
 * @param url 请求下一页的url
 * @param params 传参
 * @param listId 填充数据的标签
 * @param btnId 下一页触发的按钮
 * @param callback 回调方法
 */
const nextPage = function (url, params, listId, btnId, callback) {
  $(`#${btnId}`).click(function () {
    doGetNextPage(url, params, listId, btnId, callback)
  })
}
const nextPageText = function (url, params, listId, callback, errorCallback) {
  doGetNextPageText(url, params, listId, callback, errorCallback)
}

/**
 * 关注or取消关注
 */
const followAction = function (e) {
  const targetUserId = e.dataset.userId
  let followed = e.dataset.followed !== "true"
  const params = {
    userId: targetUserId,
    followed: followed,
  }
  console.log("点击关注or取消:", e.dataset)
  post("/user/api/saveUserRelation", params, function (data) {
    console.log("返回结果:", data)
    if (followed) {
      e.innerText = "取消关注"
    } else {
      e.innerText = "关注"
    }
    e.dataset.followed = String(followed)
  })
}

/**
 * 生成目录
 * @param selector markdown对应的html内容标签
 * @param el 生成的目录应该放的位置
 */
const genTocMenu = function genToc(selector, el) {
  let DEFAULT = {
    lineHeight: 28, // 每个菜单的行高是 28
    moreHeight: 10, // 菜单左侧的线比菜单多出的高度
    surplusHeight: 180, // 除了菜单高度+留白高度
    delay: 200, // 防抖的延迟时间
    duration: 200, // 滚动的动画持续时间
    toTopDistance: 80, // 距离视口顶部多少高度之内时候触发高亮
    selector: ".headerlink", // 文章内容中标题标签的 selector
  }
  // // 滑动监听
  let stickyFlag = 0
  let oldDocContentTop = 0

  // tocs
  const reg = new RegExp("[H]\\d")
  $(selector)
    .children()
    .each(function (index, element) {
      if (reg.test(element.nodeName)) {
        $(element).append(
          $(
            "<a href='javascript:;' class='headerlink' title='" +
              $(element).text() +
              "'></a>"
          )
        )
      }
    })

  let arContentAnchor = document.querySelectorAll(DEFAULT.selector),
    catalogLength = arContentAnchor.length,
    maxCatalogCount = 0, // 视口内能容纳的最大目录个数
    viewPortHeight = 0, // 当前视口的高度
    marginTop = 0, // 菜单的初始滚动距离
    defaultDirec = "bottom", // 默认滚动方向
    lastSH = 0, // 获取页面初始滚动距离
    lastOnIndex = 0, // 上次高亮的目录索引
    catalogBody = [], // .arCatalog-body
    catalogDl = null, // .arCatalog-body dl
    catalogDd = [], // .arCatalog-body dd
    initBodyTop = 0, // 目录可视区域的 top
    initDlBottom = 0, // 目录 dl 的 bottom
    firstDdTop = 0, // 第一个 dd 的 top
    bodyMidBottom = 0, // 目录可视区域的中间位置的 dd 的 bottom
    bodyBCR = null, // 目录可视区域的边界值
    hasStopSetHighlight = false, // 在点击目录子项的时候直接高亮当前目录，而不通过 scroll 事件触发 setHighlight 函数
    docContentTop = 0

  if (catalogLength === 0) {
    $(".toc-container").remove()
    return
  }

  initCatalog()

  window.addEventListener(
    "scroll",
    function () {
      debounce(setHighlight, DEFAULT.delay)()
      debounce(resetStatus, DEFAULT.delay)()
    },
    false
  )

  if (catalogLength > maxCatalogCount) {
    window.addEventListener(
      "scroll",
      function () {
        debounce(scrollCatalog, DEFAULT.delay)()
      },
      false
    )
  }

  initMenu()
  controlMenu()

  window.addEventListener(
    "resize",
    function (e) {
      debounce(initCatalog, DEFAULT.delay)()
      initMenu()
      controlMenu()
    },
    false
  )

  function initMenu() {
    const articleDom = document.querySelector(".article-info-wrap")
    const articleDomRight =
      articleDom.getBoundingClientRect() &&
      articleDom.getBoundingClientRect().right
    $(".toc-container").css("left", articleDomRight + 20)
  }

  // 处理目录的现实隐藏
  function controlMenu(windowScrollTop) {
    docContentTop = document
      .querySelector("#toc-container-position")
      .getBoundingClientRect().top
    if (docContentTop < 30) {
      $(".toc-container").show()
    } else {
      $(".toc-container").hide()
    }
  }

  window.addEventListener(
    "scroll",
    function (e) {
      const windowScrollTop = $(window).scrollTop()
      controlMenu(windowScrollTop)
    },
    false
  )

  function initCatalog() {
    let tempHeight = window.innerHeight

    if (viewPortHeight !== tempHeight) {
      viewPortHeight = tempHeight
      maxCatalogCount = Math.floor(
        (viewPortHeight - DEFAULT.surplusHeight) / DEFAULT.lineHeight
      )

      generateCatalog()

      catalogLength = arContentAnchor.length
      lastSH = window.pageYOffset
      catalogBody = document.querySelector(".arCatalog-body")
      catalogDl = document.querySelector(".arCatalog dl")
      catalogDd = document.querySelectorAll(".arCatalog dd")
      bodyBCR = catalogBody.getBoundingClientRect()
      initBodyTop = bodyBCR.top
      initDlBottom = initDlBottom || catalogDl.getBoundingClientRect().bottom
      ;(firstDdTop = firstDdTop || catalogDd[0]?.getBoundingClientRect()?.top),
        (bodyMidBottom =
          initBodyTop + Math.ceil(maxCatalogCount / 2) * DEFAULT.lineHeight)

      // 给目录子项绑定事件
      catalogDd.forEach((curr, index) => {
        curr.addEventListener(
          "click",
          function (e) {
            e.preventDefault()
            hasStopSetHighlight = true
            document.querySelector(".arCatalog .on").classList.remove("on")
            catalogDd[index].classList.add("on")
            lastOnIndex = index
            let currTop = arContentAnchor[index].getBoundingClientRect().top
            scrollToDest(currTop + window.pageYOffset - DEFAULT.toTopDistance)
          },
          false
        )
      })
    }
  }

  // 防抖：触发高频事件 n 秒后只会执行一次，如果 n 秒内事件再次触发，则会重新计时。
  function debounce(fn, delay = 200) {
    return function (args) {
      const _this = this
      clearTimeout(fn.id)
      fn.id = setTimeout(function () {
        fn.apply(_this, args)
      }, delay)
    }
  }

  function generateCatalog() {
    let catalogHeight =
      arContentAnchor.length > maxCatalogCount
        ? maxCatalogCount * DEFAULT.lineHeight
        : arContentAnchor.length * DEFAULT.lineHeight

    let retStr = `
						<div class="arCatalog">
							<div class="arCatalog-line" style="height: ${
                catalogHeight + DEFAULT.moreHeight
              }px"></div>
							<div class="arCatalog-body" style="max-height: ${catalogHeight}px; height: ${catalogHeight}px">
								<dl style="margin-top: ${marginTop}px">`
    let h2Index = 0,
      h3Index = 1,
      acIndex = "",
      tagName = "",
      index = 0

    for (let currNode of arContentAnchor) {
      tagName = currNode.parentElement.tagName
      if (tagName === "H2") {
        acIndex = ++h2Index
        h3Index = 1
        className = "arCatalog-tack1"
      } else if (tagName === "H3") {
        acIndex = `${h2Index}.${h3Index++}`
        className = "arCatalog-tack2"
      } else {
        acIndex = ""
        className = "arCatalog-tack3"
      }
      retStr += `
						<dd class="${className} ${index++ === lastOnIndex ? "on" : ""}">
							<a href="#">${currNode.title}</a>
							<span class="arCatalog-dot"></span>
						</dd>`
    }
    retStr += `</dl></div></div>`

    $(el).html(retStr)
  }

  // 自动滚动目录树，使得当前高亮目录在可视范围内
  function scrollCatalog() {
    let currentCatalog = document.querySelector(".arCatalog .on")

    let curr = currentCatalog.getBoundingClientRect(),
      list = catalogDl.getBoundingClientRect()

    if (defaultDirec === "bottom") {
      // 向下滚动
      if (
        curr.bottom + (maxCatalogCount / 2) * DEFAULT.lineHeight <=
        bodyBCR.bottom
      ) {
        // 上半部分
        // 不滚动
      } else if (curr.bottom - bodyMidBottom < list.bottom - bodyBCR.bottom) {
        // 中位以下
        marginTop +=
          -Math.floor((curr.bottom - bodyMidBottom) / DEFAULT.lineHeight) *
          DEFAULT.lineHeight
      } else if (bodyBCR.bottom <= list.bottom) {
        // 当剩余滚动距离
        marginTop = bodyBCR.bottom - initDlBottom
      }
    } else {
      // 向上滚动
      if (
        bodyBCR.top + (maxCatalogCount / 2) * DEFAULT.lineHeight <=
        curr.top
      ) {
        // 不滚动
      } else if (bodyMidBottom - curr.top < bodyBCR.top - list.top) {
        marginTop +=
          Math.floor((bodyMidBottom - curr.top) / DEFAULT.lineHeight) *
          DEFAULT.lineHeight
      } else if (list.top <= bodyBCR.top) {
        marginTop = 0
      }

      if (curr.top < 60 && $(".widget").parent().hasClass("right-container")) {
        $(".widget").parent().removeClass("right-container")
      }
    }

    catalogDl.style.marginTop = marginTop + "px"
  }

  // 动画实现滚动到目标位置
  function scrollToDest(destScrollTop) {
    let startTime
    let currScrollTop = window.pageYOffset
    function step(timestamp) {
      if (!startTime) {
        startTime = timestamp
      }
      const elapsed = Math.round(timestamp - startTime)
      const distance =
        elapsed *
          ((Math.floor(destScrollTop) - currScrollTop) / DEFAULT.duration) +
        currScrollTop

      document.documentElement.scrollTop = document.body.scrollTop = distance

      if (elapsed < DEFAULT.duration) {
        window.requestAnimationFrame(step)
      }
    }
    window.requestAnimationFrame(step)
  }

  // 高亮当前目录s
  function setHighlight() {
    defaultDirec = getScrollDirection()

    if (hasStopSetHighlight) {
      return
    }
    let { scrollTop } = document.scrollingElement

    let curr = document.querySelector(".arCatalog .on")

    let onIndex = [].indexOf.call(catalogDd, curr), // 当前高亮索引
      nextOnIndex = onIndex // 滚动后高亮索引
    curr?.classList?.remove("on")

    let scrollHeight =
      document.documentElement.scrollHeight || document.body.scrollHeight
    if (
      arContentAnchor[catalogLength - 1]?.getBoundingClientRect().top <=
        DEFAULT.toTopDistance ||
      window.innerHeight + window.pageYOffset === scrollHeight
    ) {
      // 尾部
      lastOnIndex = catalogLength - 1
      catalogDd[lastOnIndex].classList.add("on")
    } else if (scrollTop <= firstDdTop) {
      // 顶部
      catalogDd[0].classList.add("on")
      lastOnIndex = 0
    } else {
      // 中间：使用缓存，直接从上一次索引（onIndex）位置开始查找
      if (defaultDirec === "bottom") {
        while (nextOnIndex < catalogLength) {
          let currTop =
            arContentAnchor[nextOnIndex]?.getBoundingClientRect().top
          if (currTop > DEFAULT.toTopDistance && nextOnIndex > 0) {
            nextOnIndex--
            break
          }
          nextOnIndex++
        }
      } else {
        while (nextOnIndex >= 0) {
          let currTop = arContentAnchor[nextOnIndex].getBoundingClientRect().top
          if (currTop <= DEFAULT.toTopDistance) {
            break
          }
          nextOnIndex--
        }
      }
      nextOnIndex =
        nextOnIndex === catalogLength
          ? nextOnIndex - 1
          : nextOnIndex < 0
          ? 0
          : nextOnIndex
      lastOnIndex = nextOnIndex
      catalogDd[nextOnIndex]?.classList.add("on")
    }
  }

  // 获取最近一次页面的滚动方向
  function getScrollDirection() {
    let sh = window.pageYOffset,
      ret = "bottom"
    if (sh < lastSH) {
      ret = "top"
    }
    lastSH = sh
    return ret
  }

  function resetStatus() {
    if (hasStopSetHighlight) {
      hasStopSetHighlight = false
    }
  }
}

/** 倒计时 */
const showtime = function (endTime) {
  let nowtime = new Date() //获取当前时间
  let lefttime = endTime - nowtime.getTime() //距离结束时间的毫秒数
  if (lefttime <= 0) {
    return ""
  }
  let leftd = Math.floor(lefttime / (1000 * 60 * 60 * 24)), //计算天数
    lefth = Math.floor((lefttime / (1000 * 60 * 60)) % 24), //计算小时数
    leftm = Math.floor((lefttime / (1000 * 60)) % 60), //计算分钟数
    lefts = Math.floor((lefttime / 1000) % 60) //计算秒数

  if (lefth < 10) {
    lefth = "0" + lefth
  }

  if (leftm < 10) {
    leftm = "0" + leftm
  }

  if (lefts < 10) {
    lefts = "0" + lefts
  }
  return "剩余 " + leftd + " 天 " + lefth + ":" + leftm + ":" + lefts //返回倒计时的字符串
}

const checkFileSize = function (file) {
  if (file.size > 1024 * 1024 * 8) {
    toastr.error("图片不能超过5M!")
    return false
  }
  return true
}


function getCookie(name){<!-- -->
  var strcookie = document.cookie;//获取cookie字符串
  var arrcookie = strcookie.split("; ");//分割
  //遍历匹配
  for ( var i = 0; i < arrcookie.length; i++) {
    var arr = arrcookie[i].split("=");
    if (arr[0] == name){
      return arr[1];
    }
  }
  return "";
}

// 放在 thymeleaf 页面中正则表达式会报错，直接移动到 js 文件中就好了。
function prettyCode(content) {
  // 处理 Markdown 图片标签
  content = content.replace(/!\[([^\]]+)\]\(([^)]+)\)/g, function(match, p1, p2) {
    console.log('图片匹配:', match, 'Alt:', p1, 'URL:', p2); // 调试输出
    return '<img src="' + escapeHtml(p2) + '" alt="' + escapeHtml(p1) + '"><br/><br/>';
  });

  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, function(match, p1, p2) {
    console.log('链接匹配:', match, 'Text:', p1, 'URL:', p2); // 调试输出
    return '<a target="_blank" href="' + escapeHtml(p2) + '">' + escapeHtml(p1) + '</a>';
  });

  // 处理 Markdown 加粗标签
  content = content.replace(/\*\*([^*]+)\*\*/g, function(match, p1) {
    console.log('加粗匹配:', match, 'Text:', p1); // 调试输出
    return '<strong>' + escapeHtml(p1) + '</strong>';
  });

  return content.replace(/```(\w*)\s*([\s\S]*?)```/gs, function(match, p1, p2) {
    return '<pre><code class="' + p1 + '">' + escapeHtml(p2) + '</code></pre>';
  });
}