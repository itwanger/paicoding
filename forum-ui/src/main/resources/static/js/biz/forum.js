const post = function (path, data, callback) {
    $.ajax({
        method: 'POST',
        url: path,
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (data) {
            console.log("data", data);
            if (!data || !data.status || data.status.code != 0) {
                toastr.error(data.message);
            } else if (callback) {
                callback(data.result);
            }
        },
        error: function (data) {
            toastr.error(data);
        }
    });
};

const loadScript = function (url, callback) {
    const secScript = document.createElement("script");
    if (secScript.readyState) { // IE
        secScript.onreadystatechange = function () {
            if (secScript.readyState === 'loaded' || secScript.readyState === 'complete') {
                secScript.onreadystatechange = null;
                callback();
            }
        }
    } else { // 其他浏览器
        secScript.onload = function () {
            callback();
        }
    }
    secScript.setAttribute("type", "text/javascript");
    secScript.setAttribute("src", url);
    document.body.insertBefore(secScript, document.body.lastChild);
};

const loadLink = function (url) {
    let headHTML = document.getElementsByTagName('head')[0].innerHTML;
    headHTML += '<link href="' + url + '" rel="stylesheet">';
    document.getElementsByTagName('head')[0].innerHTML = headHTML;
};
