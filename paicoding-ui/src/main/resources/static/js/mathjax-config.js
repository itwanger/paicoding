const katexRender = function (tex, delimiter) {
    renderMathInElement(tex || document.body, {
        // customised options
        // 这里定义了一组自定义的定界符，用于识别和渲染数学公式
        delimiters: [
            // 每个定界符对象定义了左边界、右边界和显示模式
            {left: '$$', right: '$$', display: true},
            {left: '$', right: '$', display: false},
            {left: "\\(", right: "\\)", display: false},
            {left: "\\begin{equation}", right: "\\end{equation}", display: true},
            {left: "\\begin{align}", right: "\\end{align}", display: true},
            {left: "\\begin{alignat}", right: "\\end{alignat}", display: true},
            {left: "\\begin{gather}", right: "\\end{gather}", display: true},
            {left: "\\begin{CD}", right: "\\end{CD}", display: true},
            {left: "\\[", right: "\\]", display: true}
        ],

        // • rendering keys, e.g.:
        throwOnError : false
    });
}

document.addEventListener("DOMContentLoaded", function() {
    katexRender();
});