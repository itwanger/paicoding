const post = function (path, data, callback) {
  $.ajax({
    method: "POST",
    url: path,
    contentType: "application/json",
    data: JSON.stringify(data),
    success: function (data) {
      console.log("data", data)
      if (!data || !data.status || data.status.code != 0) {
        toastr.error(data.message)
      } else if (callback) {
        callback(data.result)
      }
    },
    error: function (data) {
      toastr.error(data)
    },
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
const doGetNextPageText = function (url, params, listId, callback) {
  $.get(url, params, function (data) {
    console.log("response: ", data)
    const result = data.result
    $(`#${listId}`).append(result.html)
    if (!result.hasMore) {
      alert("没有更多了")
    }
    if (callback) {
      callback("true")
    }
  })
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
const nextPageText = function (url, params, listId, callback) {
  doGetNextPageText(url, params, listId, callback)
}
