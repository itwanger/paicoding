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

function doMockLogin(code, fromUserName) {
    if (!code) {
        toastr.error("验证码还没准备好，请稍后再试");
        return;
    }

    $.ajax({
        method: 'POST',
        url: "/wx/mock/login",
        dataType: "json",
        data: {
            code: code,
            fromUserName: fromUserName
        },
        success: function (data) {
            console.log("success data", data);
            if (!data || !data.status || data.status.code !== 0) {
                toastr.error(data && data.status ? data.status.msg : "登录失败，请重试");
                return;
            }
            toastr.success("登录成功");
            if (window.location.pathname === "/login") {
                window.location.href = "/";
            } else {
                window.location.reload();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log("Error: " +textStatus + ", " + errorThrown);
            toastr.error("登录失败，请重试");
        }
    })
}

$('#mockLogin').click(function () {
    console.log("一键登录！！！");
    const code = this.dataset.verifyCode;
    doMockLogin(code, 'demoUser1234');
})

$('#mockLogin2').click(function () {
    console.log("随机新用户！！！");
    const code = this.dataset.verifyCode;
    let randUid = 'demoUser_' + Math.round(Math.random() * 100);
    doMockLogin(code, randUid);
})
