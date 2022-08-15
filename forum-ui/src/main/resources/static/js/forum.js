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
