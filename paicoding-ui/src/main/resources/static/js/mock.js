$('#getToken').click(function () {
    $.ajax({
        method: 'POST',
        url: "/wx/callback",
        contentType: 'application/xml',
        data: "<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[demoUser1234]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[login]]></Content><MsgId>11111111</MsgId></xml>",
        success: function (data) {
            console.log("data", data);
            if (!data) {
                toastr.error(data.message);
            } else {
                $('#testOutput').html("<bold>" + $(data).find("Content").text() + "</bold>");
            }
        },
        error: function (data) {
            toastr.error(data);
        }
    });
})

$('#mockLogin').click(function () {
    console.log("一键登录！！！");
    const code = this.dataset.verifyCode;
    $.ajax({
        method: 'POST',
        url: "/wx/callback",
        contentType: 'application/xml',
        data: "<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[demoUser1234]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + code + "]]></Content><MsgId>11111111</MsgId></xml>",
        success: function (data) {
            console.log("success data", data);
            // 登录请求成功后，等待1秒让后端处理完成，然后刷新页面
            setTimeout(function() {
                if (window.location.pathname === "/login") {
                    window.location.href = "/";
                } else {
                    window.location.reload();
                }
            }, 1000);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log("Error: " +textStatus + ", " + errorThrown);
            toastr.error("登录失败，请重试");
        }
    });
})

$('#mockLogin2').click(function () {
    console.log("随机新用户！！！");
    const code = this.dataset.verifyCode;
    let randUid = 'demoUser_' + Math.round(Math.random() * 100);
    $.ajax({
        method: 'POST',
        url: "/wx/callback",
        contentType: 'application/xml',
        data: "<xml><URL><![CDATA[https://hhui.top]]></URL><ToUserName><![CDATA[一灰灰blog]]></ToUserName><FromUserName><![CDATA[" + randUid + "]]></FromUserName><CreateTime>1655700579</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + code + "]]></Content><MsgId>11111111</MsgId></xml>",
        success: function (data) {
            console.log("success data", data);
            // 登录请求成功后，等待1秒让后端处理完成，然后刷新页面
            setTimeout(function() {
                if (window.location.pathname === "/login") {
                    window.location.href = "/";
                } else {
                    window.location.reload();
                }
            }, 1000);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log("Error: " +textStatus + ", " + errorThrown);
            toastr.error("登录失败，请重试");
        }
    });
})