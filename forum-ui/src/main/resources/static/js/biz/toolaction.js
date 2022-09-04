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
const praiseComment = function (commentId, action, callback) {
    // 2 点赞， 4 取消点赞
    const type = action ? 2 : 4;
    $.get('/comment/api/favor?commentId=' + commentId + "&type=" + type, function (data) {
        console.log("response:", data);
        if (!data || !data.status || data.status.code !== 0) {
            toastr.error(data.message);
        } else if (callback) {
            callback(data.result);
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
