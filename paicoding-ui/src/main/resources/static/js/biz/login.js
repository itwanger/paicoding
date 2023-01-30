// $('#logoutBtn').click(function () {
//     $.ajax({
//         url: "/logout", dataType: "json", type: "get", success: function (data) {
//             toastr.success("已退出登录")
//             window.location.href = "/";
//         }
//     })
// })

$('#loginBtn').click(function () {
    const code = $('#loginCode').val();
    console.log("开始登录：" + code);
    $.ajax({
        url: "/login?code=" + code,    //请求的url地址
        dataType: "json",   //返回格式为json
        async: false,//请求是否异步，默认为异步，这也是ajax重要特性
        type: "POST",   //请求方式
        success: function (data) {
            //请求成功时处理
            console.log("response data:", data);
            if (!data || !data.status || data.status.code !== 0) {
                toastr.error(data.status.msg);
            } else {
                // 登录成功，刷新
                if (window.location.pathname === "/login") {
                    window.location.href = "/";
                } else {
                    // 刷新当前页面
                    window.location.reload();
                }
                toastr.success("登录成功");
            }
        }, error: function () {
            //请求出错处理
            toastr.error("登录错误");
        }
    });
});