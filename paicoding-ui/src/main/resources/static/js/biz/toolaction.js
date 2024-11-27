// 文章点赞
const praiseArticle = function (articleId, action, callback) {
    // 2 点赞， 4 取消点赞
    const type = action ? 2 : 4;
    $.get('/article/api/favor?articleId=' + articleId + "&type=" + type, function (data) {
        console.log("response:", data);
        if (!data || !data.status || data.status.code !== 0) {
            toastr.error(data.message);
        } else if (callback) {
            callback(data.result);
        }
    });
}

// 评论点赞
const praiseComment = function (e,login) {
    console.log("this:===>", e);
    if (!login) {return;}

    const commentId = e.dataset.commentId;
    const action = e.dataset.praised == 'true' ? false : true
    let priaseCount = parseInt(e.dataset.praiseCount);

    // 2 点赞， 4 取消点赞
    const type = action ? 2 : 4;
    console.log("action=", action , "-->", e.dataset)
    $.get('/comment/api/favor?commentId=' + commentId + "&type=" + type, function (data) {
        console.log("response:", data);
        if (!data || !data.status || data.status.code !== 0) {
            toastr.error(data.message);
        }

        let priaseCount = parseInt(e.dataset.praiseCount);
        if(type == 2) {
            e.classList.add('active');
            priaseCount += 1;
        } else {
            e.classList.remove('active');
            priaseCount -= 1;

        }

        // 更新点赞数，以及是否点赞的状态
        e.dataset.praiseCount = priaseCount;
        e.dataset.praised = action;
        if (priaseCount > 0) {
            e.getElementsByTagName('span')[0].innerText = parseInt(priaseCount);
        } else {
            e.getElementsByTagName('span')[0].innerText = '点赞';
        }
    });
}

// 文章收藏
const collectArticle = function (articleId, action, callback) {
    // 3 收藏， 5 取消收藏
    const type = action ? 3 : 5;
    $.get('/article/api/favor?articleId=' + articleId + "&type=" + type, function (data) {
        console.log("response:", data);
        if (!data || !data.status || data.status.code !== 0) {
            toastr.error(data.message);
        } else if (callback) {
            callback(data.result);
        }
    });
}