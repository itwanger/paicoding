// AI机器人评论功能扩展
// 此文件专注于AI评论相关的特殊逻辑，通用评论输入框功能已在comment-input.js中实现

$(document).ready(function () {
    // 如果需要对特定的AI评论功能进行扩展，可以在这里添加
    // 例如：特殊的AI响应处理、AI评论样式调整等

    // 示例：为AI生成的评论添加特殊标识
    $('.ai-generated-comment').each(function () {
        // 可以在这里添加AI评论特有的处理逻辑
    });
});
// AI机器人评论功能

$(document).ready(function () {
    // AI机器人选择器相关元素
    const $aiBotBtn = $('#aiBotBtn');
    const $aiBotDropdown = $('#aiBotDropdown');
    const $aiBotOptions = $('.ai-bot-option');
    const $commentTextarea = $('#commentContent');
    const $commentCount = $('#commentCount');
    const $commentBtn = $('#commentBtn');
    const $commentContainer = $('.comment-input-container');

    // 点击机器人按钮显示/隐藏下拉菜单
    $aiBotBtn.on('click', function (e) {
        e.stopPropagation();
        $aiBotDropdown.toggleClass('show');

        // 确保下拉菜单在视窗内显示
        if ($aiBotDropdown.hasClass('show')) {
            const buttonRect = $aiBotBtn[0].getBoundingClientRect();
            const dropdownRect = $aiBotDropdown[0].getBoundingClientRect();
            const viewportHeight = window.innerHeight;

            // 重置位置样式
            $aiBotDropdown.css({
                'top': '100%',
                'bottom': 'auto'
            });

            // 如果下拉菜单超出视窗底部，则向上显示
            if (buttonRect.bottom + dropdownRect.height > viewportHeight) {
                $aiBotDropdown.css({
                    'top': 'auto',
                    'bottom': '100%'
                });
            }
        }
    });

    // 点击页面其他地方隐藏下拉菜单
    $(document).on('click', function (e) {
        if (!$(e.target).closest('#aiBotSelector').length) {
            $aiBotDropdown.removeClass('show');
        }
    });

    // 选择AI机器人选项
    $aiBotOptions.on('click', function () {
        const botType = $(this).data('bot');
        const botName = botType === 'hater' ? '杠精派' : '派聪明';

        // 在文本框中添加@机器人标签
        const currentText = $commentTextarea.val();
        const tag = `@${botName} `;

        if (!currentText.includes(tag)) {
            $commentTextarea.val(tag + currentText);
            updateCommentCount();
        }

        // 隐藏下拉菜单
        $aiBotDropdown.removeClass('show');

        // 聚焦到文本框
        $commentTextarea.focus();
    });

    // 实时更新字符计数
    $commentTextarea.on('input', function () {
        updateCommentCount();
    });

    // 当输入框获得焦点时，更新容器样式
    $commentTextarea.on('focus', function () {
        $commentContainer.addClass('focus');
    });

    // 当输入框失去焦点时，更新容器样式
    $commentTextarea.on('blur', function () {
        $commentContainer.removeClass('focus');
    });

    // 更新字符计数函数
    function updateCommentCount() {
        const currentLength = $commentTextarea.val().length;
        $commentCount.text(currentLength);

        // 根据是否有内容启用/禁用评论按钮
        if (currentLength > 0) {
            $commentBtn.removeClass('c-btn-disabled').prop('disabled', false);
        } else {
            $commentBtn.addClass('c-btn-disabled').prop('disabled', true);
        }
    }

    // 初始化字符计数
    updateCommentCount();
});