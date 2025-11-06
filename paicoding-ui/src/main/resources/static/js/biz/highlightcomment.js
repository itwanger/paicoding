// 文本选择功能
let selectedText = '';
let selectionRange = null;
let toSaveSelection = null;
// 保存被隐藏的侧边栏元素
let hiddenSidebars = [];
// 标记是否正在处理评论图标点击
let isHandlingCommentIcon = false;

// 检测是否为移动设备
function isMobileDevice() {
    return window.innerWidth <= 768;
}

// 监听文本选择事件
document.addEventListener('selectionchange', function () {
    // 如果正在处理评论图标点击，忽略选择变化
    if (isHandlingCommentIcon) {
        // console.debug('正在处理评论图标点击，忽略选择变化');
        return;
    }

    const selection = window.getSelection();
    if (selection.toString().trim() !== '' && isElementInArticleContent(selection.anchorNode)) {
        selectedText = selection.toString().trim();
        selectionRange = selection.getRangeAt(0);
        // console.debug('检测到文本选择:', selectedText); // 调试信息
        // 不再直接显示侧边栏，而是在文本附近显示评论图标
        showCommentIcon(selectionRange);
    } else if (selection.toString().trim() === '') {
        // console.debug('文本选择已清除'); // 调试信息
        // 隐藏评论图标
        hideCommentIcon();
    }
});

// 添加鼠标按下事件监听器，防止在图标上按下鼠标时清除选择
document.addEventListener('mousedown', function (e) {
    const commentIcon = document.getElementById('comment-icon');
    if (commentIcon && commentIcon.contains(e.target)) {
        // console.debug('在评论图标上按下鼠标');
        e.preventDefault();
        isHandlingCommentIcon = true;

        // 短暂设置标记，确保点击事件能正常处理
        setTimeout(() => {
            isHandlingCommentIcon = false;
        }, 300);
    }
}, true);

// 检查选中的元素是否在文章内容区域内
function isElementInArticleContent(element) {
    if (!element) return false;
    const articleContent = document.getElementById('articleContent');
    const result = articleContent && articleContent.contains(element);
    // console.debug('元素是否在文章内容区域内:', result); // 调试信息
    return result;
}

// 显示评论图标
function showCommentIcon(range) {
    // 移除已存在的评论图标
    hideCommentIcon();

    // 判断选中的区域标签，是否在支持的标签范围内
    if (!canShowCommentIcon || !canShowCommentIcon(range)) {
        return;
    }

    // 创建评论图标
    const commentIcon = document.createElement('div');
    commentIcon.id = 'comment-icon';
    commentIcon.innerHTML = '🤖';


    // 获取选中文本的位置
    const rect = range.getBoundingClientRect();
    if (isMobileDevice()) {
        commentIcon.style.top = (window.scrollY + rect.top - 10) + 'px';
    } else {
        commentIcon.style.top = (window.scrollY + rect.top - 30) + 'px';
    }
    commentIcon.style.left = (window.scrollX + rect.right) + 'px';

    // 添加到页面
    document.body.appendChild(commentIcon);

    // 确保图标已添加到DOM后再绑定事件
    setTimeout(() => {
        const icon = document.getElementById('comment-icon');
        if (icon) {
            icon.addEventListener('click', handleCommentIconClick);
            icon.addEventListener('mousedown', function (e) {
                e.preventDefault(); // 防止鼠标按下时清除选择
            });
        }
    }, 0);
}

// 评论图标点击处理函数
function handleCommentIconClick(e) {
    e.stopPropagation();
    e.preventDefault();

    // 保存当前选择的文本范围
    const savedRange = selectionRange;
    toSaveSelection = rangeToElementJSON(savedRange);
    console.log('需要保存的划线内容:', savedRange, toSaveSelection);

    // 设置标记，防止选择变化事件隐藏图标
    isHandlingCommentIcon = true;

    // 检测设备类型，选择显示方式
    showQuoteCommentForm(selectedText)

    hideCommentIcon();

    // 重新应用选择，保持文本选中状态，并添加下划线样式
    setTimeout(() => {
        if (savedRange) {
            // 为选中的文本添加下划线样式
            applyUnderlineToSelection(savedRange);
        }

        // 重置标记
        isHandlingCommentIcon = false;
    }, 50);

    // 聚焦到评论输入框
    setTimeout(function () {
        const commentInput = isMobileDevice() ? document.getElementById('quoteCommentInputModal') : document.getElementById('quoteCommentInput');
        if (commentInput) {
            commentInput.focus();
        }
    }, 100);
}



// 初始化文本的划线内容
function initUnderlineToSelection(commentList) {
    for (let i = 0; i < commentList.length; i++) {
        initUnderLine(commentList[i])
    }
}

function initUnderLine(comment) {
    const commentText = comment.highlight;
    if (commentText) {
        let range = elementJSONToRange(commentText)
        if (range) {
            try {
                // 创建一个新的范围来包装选中的文本
                const newNode = document.createElement('span');
                newNode.style.textDecoration = 'underline';
                newNode.style.textDecorationColor = '#ff8721';
                newNode.style.textDecorationStyle = 'solid';
                newNode.style.textDecorationThickness = '2px';
                newNode.className = 'selected-text-highlight';
                // 添加评论ID属性，用于点击时获取评论数据
                newNode.setAttribute('data-comment-id', comment.commentId);

                // 使用 surroundContents 方法包装选中的内容
                range.surroundContents(newNode);

                // 绑定点击事件
                newNode.addEventListener('click', function (e) {
                    e.stopPropagation();
                    showQuoteCommentWithComments(comment.commentId)
                });
            } catch (e) {
                console.debug('无法为选中文本添加下划线样式:', e);
            }
        }
    }
}


// 点击划线内容,加载评论数据
function loadCommentData(commentId, isModal) {
    // 调用后端API获取评论数据
    $.get('/comment/api/listTopComment?commentId=' + commentId, function (data) {
        if (data && data.status && data.status.code === 0) {
            // 处理获取到的评论数据
            if (isModal) {
                document.getElementById('quoteCommentModal').innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">${data.result}</div>`;
            } else {
                document.getElementById('quoteCommentSidebar').innerHTML = data.result;
            }
        } else {
            console.log('请求数据异常!', data);
        }
    }).fail(function () {

    });
}


// 为选中的文本添加下划线样式
function applyUnderlineToSelection(range) {
    try {
        // 创建一个新的范围来包装选中的文本
        const newNode = document.createElement('span');
        newNode.style.textDecoration = 'underline';
        newNode.style.textDecorationColor = '#ff8721';
        newNode.style.textDecorationStyle = 'solid';
        newNode.style.textDecorationThickness = '2px';
        newNode.style.backgroundColor = 'rgba(255, 105, 0, 0.08)';
        newNode.className = 'selected-text-highlight';

        // 使用 surroundContents 方法包装选中的内容
        range.surroundContents(newNode);

        // 保存高亮元素的引用，以便后续可以移除
        if (!window.highlightedElements) {
            window.highlightedElements = [];
        }
        window.highlightedElements.push(newNode);

        // 绑定点击事件
        newNode.addEventListener('click', function (e) {
            e.stopPropagation();
            const selectedText = range.toString();
            if (newNode.hasAttribute('data-comment-id')) {
                // 如果当前节点,存在 data-comment-id, 则调用 showQuoteCommentWithComments
                showQuoteCommentWithComments(newNode.getAttribute('data-comment-id'))
            } else {
                // 首次划线，显示输入评论框
                showQuoteCommentForm(selectedText)
            }
        });
    } catch (e) {
        console.debug('无法为选中文本添加下划线样式:', e);
        // 如果 surroundContents 失败，使用另一种方法
        // 注意：不要重新声明range变量，使用传入的range参数
        if (range) {
            try {
                const selectedContent = range.extractContents();
                const span = document.createElement('span');
                span.style.textDecoration = 'underline';
                span.style.textDecorationColor = '#ff6900';
                span.style.textDecorationStyle = 'wavy';
                span.className = 'selected-text-highlight';
                span.appendChild(selectedContent);
                range.insertNode(span);

                // 保存高亮元素的引用
                if (!window.highlightedElements) {
                    window.highlightedElements = [];
                }
                window.highlightedElements.push(span);

                // 绑定点击事件
                span.addEventListener('click', function (e) {
                    e.stopPropagation();
                    const selectedText = span.textContent;
                    showQuoteCommentForm(selectedText)
                });
            } catch (e2) {
                console.debug('第二种方法也失败了:', e2);
            }
        }
    }
}

// 显示引用的评论信息
function showQuoteCommentWithComments(commentId) {
    if (isMobileDevice()) {
        const modal = document.getElementById('quoteCommentModal');

        if (modal) {
            // 显示引用评论弹窗并加载评论数据 (移动端)
            $('#quoteCommentModal').modal('show');

            // 清空输入框
            const commentInput = document.getElementById('quoteCommentInputModal');
            if (commentInput) {
                commentInput.value = '';
            }

            const submitBtn = document.getElementById('submitQuoteCommentModal');
            if (submitBtn) {
                submitBtn.disabled = true;
            }

            // 加载评论数据
            loadCommentData(commentId, true);
        }
    } else {
        // 显示引用评论侧边栏并加载评论数据
        const sidebar = document.getElementById('quoteCommentSidebar');

        if (sidebar) {
            // 隐藏其他侧边栏
            hideOtherSidebars();
            // 显示侧边栏
            sidebar.style.display = 'block';
            sidebar.style.visibility = 'visible';
            sidebar.style.position = 'sticky';
            sidebar.style.top = '20px';

            // 清空输入框
            const commentInput = document.getElementById('quoteCommentInput');
            if (commentInput) {
                commentInput.value = '';
            }

            const submitBtn = document.getElementById('submitQuoteComment');
            if (submitBtn) {
                submitBtn.disabled = true;
            }

            // 加载评论数据
            loadCommentData(commentId, false);
        }
    }
}


// 在隐藏引用评论侧边栏时移除高亮
const originalHideQuoteCommentSidebar = hideQuoteCommentSidebar;
hideQuoteCommentSidebar = function () {
    // 移除高亮样式 - 但保留新增的高亮
    if (window.highlightedElements) {
        // 只移除不是新增评论的高亮
        window.highlightedElements = window.highlightedElements.filter(element => {
            if (element.classList.contains('new-highlight')) {
                // 保留新增的高亮
                return true;
            } else {
                // 移除其他高亮
                if (element.parentNode) {
                    while (element.firstChild) {
                        element.parentNode.insertBefore(element.firstChild, element);
                    }
                    element.parentNode.removeChild(element);
                }
                return false;
            }
        });
    }
    // 调用原始函数
    originalHideQuoteCommentSidebar();
};

// 隐藏评论图标
function hideCommentIcon() {
    const commentIcon = document.getElementById('comment-icon');
    if (commentIcon) {
        commentIcon.removeEventListener('click', handleCommentIconClick);
        commentIcon.remove();
    }
}

function showQuoteCommentForm(text) {
    // 首次划线，显示输入评论框
    if (isMobileDevice()) {
        // 移动端，使用弹窗的方式显示输入框
        const modal = document.getElementById('quoteCommentModal');
        modal.innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">引用评论</h5>
            <button type="button" class="close" data-dismiss="modal">
              <span>&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <div class="quote-content">
              <div class="quote-text" id="quotedTextModal"></div>
              <div class="quote-comment-form">
                <textarea id="quoteCommentInputModal" placeholder="写下您的评论..." class="form-control"></textarea>
                <button id="submitQuoteCommentModal" class="c-btn c-btn-primary mt-2" disabled>提交评论</button>
              </div>
            </div>
          </div>
        </div>
      </div>`
        initQuoteModalEvent();
        const quotedText = document.getElementById('quotedTextModal');
        const commentInput = document.getElementById('quoteCommentInputModal');
        const submitBtn = document.getElementById('submitQuoteCommentModal');

        if (quotedText) {
            quotedText.textContent = text;
        }

        if (commentInput) {
            commentInput.value = '';
        }

        if (submitBtn) {
            submitBtn.disabled = true;
        }

        if (modal) {
            // 显示弹窗
            $('#quoteCommentModal').modal('show');

            // 监听模态框关闭事件
            $('#quoteCommentModal').off('hidden.bs.modal').on('hidden.bs.modal', function () {
                // 模态框关闭时，清除选中的文本
                hideQuoteCommentSidebar();
            });
        }
    } else {
        // pc，侧边栏的方式显示输入框
        const sidebar = document.getElementById('quoteCommentSidebar');
        // 重新初始化这块内容
        sidebar.innerHTML = `<div class="widget">
                <h3 class="com-nav-bar-title">划词评论</h3>
                <div class="quote-content">
                  <div class="quote-text" id="quotedText">${text}</div>
                  <div class="quote-comment-form comment-input-container">
                    <textarea id="quoteCommentInput" placeholder="写下您的评论，可选择@派聪明或者@杠精派..." class="form-control"></textarea>

                    <div class="comment-toolbar">
                      <div class="toolbar-left">
                        <div class="ai-bot-selector" id="sideAiBotSelector">
                          <button type="button" class="ai-bot-btn" id="sideAiBotBtn">
                            🤖
                          </button>
                          <div class="ai-bot-dropdown" id="sideAiBotDropdown" style="top: 100%; bottom: auto;">
                            <div class="ai-bot-option" data-bot="hater">杠精派</div>
                            <div class="ai-bot-option" data-bot="smart">派聪明</div>
                          </div>
                        </div>
                      </div>
                      <div class="toolbar-right">
                        <span class="comment-count"><span id="sideCommentCount">0</span>/512</span>
                        <button id="submitQuoteComment" class="c-btn c-btn-primary mt-2" disabled>提交评论</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>`

        // 重新绑定监听事件
        initQuoteEvent();
        if (sidebar) {
            // 隐藏其他侧边栏
            hideOtherSidebars();
            // 显示侧边栏 - 使用更可靠的方式
            sidebar.style.display = 'block';
            sidebar.style.visibility = 'visible';

            // 确保侧边栏在视图中
            sidebar.style.position = 'sticky';
            sidebar.style.top = '20px';

            // 清空输入框
            const commentInput = document.getElementById('quoteCommentInput');
            if (commentInput) {
                commentInput.value = '';
            }

            const submitBtn = document.getElementById('submitQuoteComment');
            if (submitBtn) {
                submitBtn.disabled = true;
            }

        } else {
            // 再次尝试查找元素
            setTimeout(() => {
                const retrySidebar = document.getElementById('quoteCommentSidebar');
                const retryQuoteText = document.getElementById('quotedText');
                console.log('重试查找元素:', retrySidebar, retryQuoteText);
            }, 100);
        }
    }
}

// 隐藏引用评论侧边栏
function hideQuoteCommentSidebar() {
    const sidebar = document.getElementById('quoteCommentSidebar');
    if (sidebar) {
        sidebar.style.display = 'none';
        sidebar.style.visibility = 'hidden';
        // 恢复其他侧边栏
        showOtherSidebars();
    }
}

// 隐藏其他侧边栏
function hideOtherSidebars() {
    // 获取所有需要隐藏的侧边栏元素
    const sidebars = document.querySelectorAll('.layout-side > .right-container, .layout-side > div:not(#quoteCommentSidebar)');
    if (hiddenSidebars && hiddenSidebars.length > 0) {
        console.log('已经隐藏了，无需再次隐藏~');
        return;
    }

    hiddenSidebars = [];

    sidebars.forEach(function (sidebar) {
        // 排除引用评论侧边栏本身
        if (sidebar.id !== 'quoteCommentSidebar' && sidebar.style.display !== 'none') {
            hiddenSidebars.push({
                element: sidebar,
                display: sidebar.style.display || getComputedStyle(sidebar).display
            });
            sidebar.style.display = 'none';
        }
    });
}

// 恢复其他侧边栏
function showOtherSidebars() {
    hiddenSidebars.forEach(function (item) {
        item.element.style.display = item.display;
    });
    hiddenSidebars = [];
}

// 初始化弹窗事件
function initQuoteModalEvent() {
    // 监听引用评论输入框
    const commentInput = document.getElementById('quoteCommentInputModal');
    const submitBtn = document.getElementById('submitQuoteCommentModal');

    if (commentInput && submitBtn) {
        commentInput.addEventListener('input', function () {
            submitBtn.disabled = this.value.trim() === '';
        });

        // 提交引用评论
        submitBtn.addEventListener('click', function () {
            const commentContent = commentInput.value.trim();

            if (commentContent === '') {
                toastr.error("评论内容不能为空");
                return;
            }

            // 提交评论
            const params = {
                articleId: articleId,
                commentContent: commentContent,
                highlight: toSaveSelection,
            };

            console.log('准备提交评论信息:', params)
            post("/comment/api/highlightComment", params, function (data) {
                // 为新增的评论添加持久化高亮标记
                if (window.highlightedElements && window.highlightedElements.length > 0) {
                    const lastHighlight = window.highlightedElements[window.highlightedElements.length - 1];
                    // 添加评论ID属性，用于点击时获取评论数据
                    lastHighlight.setAttribute('data-comment-id', data.commentId);
                    lastHighlight.classList.add('new-highlight');
                    // 绑定点击事件
                    lastHighlight.addEventListener('click', function (e) {
                        e.stopPropagation();
                        // 支持点击之后查看详情页
                        showQuoteCommentWithComments(data.commentId);
                    });
                }

                // 显示成功消息
                toastr.success("评论发表成功");
                document.getElementById('quoteCommentModal').innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">${data.html}</div>`;
            });
        });
    }
}
function initQuoteEvent() {
    // 添加点击页面其他地方隐藏引用评论侧边栏的功能
    document.addEventListener('click', function (e) {
        const sidebar = document.getElementById('quoteCommentSidebar');
        const commentInput = document.getElementById('quoteCommentInput');

        // 如果点击的不是侧边栏内部元素，也不是评论输入框，则隐藏侧边栏
        if (sidebar && sidebar.style.display !== 'none' && !sidebar.contains(e.target) && e.target !== commentInput) {
            hideQuoteCommentSidebar();
        }

        // 添加 AI 机器人下拉框控制
        const aiBotBtn = document.getElementById('sideAiBotBtn');
        const aiBotDropdown = document.getElementById('sideAiBotDropdown');

        if (aiBotBtn && aiBotBtn.contains(e.target)) {
            // 切换下拉框显示状态
            if (aiBotDropdown) {
                aiBotDropdown.style.display = 'block';
            }
            e.stopPropagation();
        } else if (aiBotDropdown && aiBotDropdown.style.display === 'block') {
            // 点击其他地方隐藏下拉框
            aiBotDropdown.style.display = 'none';
        }

        if (e.target.classList.contains('ai-bot-option')) {
            const botType = e.target.getAttribute('data-bot');
            const commentInput = document.getElementById('quoteCommentInput');
            const aiBotDropdown = document.getElementById('sideAiBotDropdown');

            if (commentInput && aiBotDropdown) {
                // 根据机器人类型设置前缀文本
                let prefix = '';
                if (botType === 'hater') {
                    prefix = '@杠精派 ';
                } else if (botType === 'smart') {
                    prefix = '@派聪明 ';
                }

                // 在输入框前缀添加机器人文本
                const currentValue = commentInput.value;
                if (!currentValue.startsWith(prefix)) {
                    commentInput.value = prefix + currentValue;
                }

                // 隐藏下拉框
                aiBotDropdown.style.display = 'none';

                // 聚焦到输入框
                commentInput.focus();
                // 触发 input 事件以更新按钮状态和计数
                commentInput.dispatchEvent(new Event('input'));
            }
        }
    });

    // 监听引用评论输入框
    document.getElementById('quoteCommentInput')?.addEventListener('input', function () {
        const submitBtn = document.getElementById('submitQuoteComment');
        submitBtn.disabled = this.value.trim() === '';

        // 更新文本计数
        const commentCount = document.getElementById('sideCommentCount');
        if (commentCount) {
            commentCount.textContent = this.value.length;
        }
    });

    // 提交引用评论
    document.getElementById('submitQuoteComment')?.addEventListener('click', function () {
        const commentInput = document.getElementById('quoteCommentInput');
        const commentContent = commentInput.value.trim();

        if (commentContent === '') {
            toastr.error("评论内容不能为空");
            return;
        }

        // 提交评论
        const params = {
            articleId: articleId,
            commentContent: commentContent,
            highlight: toSaveSelection,
        };

        console.log('准备提交评论信息:', params)
        post("/comment/api/highlightComment", params, function (data) {
            // 为新增的评论添加持久化高亮标记
            if (window.highlightedElements && window.highlightedElements.length > 0) {
                const lastHighlight = window.highlightedElements[window.highlightedElements.length - 1];
                // 添加评论ID属性，用于点击时获取评论数据
                lastHighlight.setAttribute('data-comment-id', data.commentId);
                lastHighlight.classList.add('new-highlight');
                // 绑定点击事件
                lastHighlight.addEventListener('click', function (e) {
                    e.stopPropagation();
                    // 支持点击之后查看详情页
                    showQuoteCommentWithComments(data.commentId)
                });
            }

            // 显示成功消息
            toastr.success("评论发表成功");
            if (isMobileDevice()) {
                document.getElementById('quoteCommentSidebar').innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">${data.html}</div>`;
            } else {
                document.getElementById('quoteCommentSidebar').innerHTML = data.html;
            }
        });
    });
}

if (isMobileDevice()) {
    initQuoteModalEvent();
} else {
    initQuoteEvent();
}