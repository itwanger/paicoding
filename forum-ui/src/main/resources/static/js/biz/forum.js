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
const doGetNextPageText = function (url, params, listId, callback, errorCallBack) {
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
const followAction = function(e) {
  const targetUserId = e.dataset.userId;
  let followed = e.dataset.followed !== 'true';
  const params = {
    "userId": targetUserId,
    "followed": followed ,
  }
  console.log("点击关注or取消:", e.dataset);
  post("/user/api/saveUserRelation", params, function (data) {
    console.log("返回结果:", data);
    if (followed) {
      e.innerText = "取消关注";
    } else {
      e.innerText = "关注";
    }
    e.dataset.followed = String(followed);
  });
}


/**
 * 生成目录
 * @param selector markdown对应的html内容标签
 * @param el 生成的目录应该放的位置
 */
const genTocMenu = function genToc(selector, el) {
  const tocs = document.querySelector(selector).children
  const reg = new RegExp('[H]\\d')
  const list = document.createDocumentFragment()
  const style = document.createElement('style')
  style.innerHTML = `
    .toc{
      height: 100%;
      color:$menuTextActive;
      padding:12px;
    }
    .toc h1 {cursor: pointer;margin-bottom: 10px;font-size: 20px;}
    .toc h2 {cursor: pointer; padding-left: 12px;margin-bottom: 5px;font-size: 18px;}
    .toc h3 {cursor: pointer; padding-left: 24px;margin-bottom: 5px;font-size: 16px;}
    .toc h4 {cursor: pointer; padding-left: 36px;margin-bottom: 5px;font-size: 14px;}
    .toc h5 {cursor: pointer; padding-left: 48px;margin-bottom: 5px;font-size: 12px;}
    .toc h6 {cursor: pointer; padding-left: 60px;margin-bottom: 5px;font-size: 12px;}
    .toc h1:hover {text-decoration: underline;}
    .toc h2:hover {text-decoration: underline;}
    .toc h3:hover {text-decoration: underline;}
    .toc h4:hover {text-decoration: underline;}
    .toc h5:hover {text-decoration: underline;}
    .toc h6:hover {text-decoration: underline;}
  `
  for (let index = 0; index < tocs.length; index++) {
    const item = tocs[index]
    if (reg.test(item.nodeName)) {
      list.appendChild(item.cloneNode(true))
    }
  }
  document.querySelector(el).appendChild(style)
  document.querySelector(el).appendChild(list)
  document.querySelector(el).addEventListener('click', function(e) {
    if (reg.test(e.target.nodeName)) {
      const id = e.target.children[0].id
      document.getElementById(id).scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      })
    }
  })
}