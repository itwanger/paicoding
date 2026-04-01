// 文本选择功能
let selectedText = '';
let selectionRange = null;
let toSaveSelection = null;
// 保存被隐藏的侧边栏元素
let hiddenSidebars = [];
// 标记是否正在处理评论图标点击
let isHandlingCommentIcon = false;
let quoteSidebarGlobalEventsBound = false;

function getHighlightSidebar() {
    return document.getElementById('quoteCommentSidebar');
}

function getSidebarHost() {
    const sidebar = getHighlightSidebar();
    return sidebar ? sidebar.parentElement : null;
}

function normalizeHighlightSidebarState() {
    const sidebar = getHighlightSidebar();
    if (!sidebar) {
        return;
    }

    // 清理可能导致整个侧栏被意外隐藏的临时类名。
    sidebar.classList.remove('__web-inspector-hide-shortcut__');
    sidebar.style.visibility = 'visible';

    const widget = sidebar.querySelector('.widget');
    if (widget) {
        widget.style.visibility = 'visible';
    }
}

function syncSidebarCompanions() {
    // 触发目录/PDF 侧栏的滚动态刷新，让划词评论模式即时接管右侧区域。
    window.dispatchEvent(new Event('scroll'));
}

function toggleHighlightSidebarMode(active) {
    const sidebar = getHighlightSidebar();
    const host = getSidebarHost();

    if (host) {
        host.classList.toggle('highlight-sidebar-active', !!active);
        if (active) {
            if (!hiddenSidebars.length) {
                hiddenSidebars = Array.from(host.children)
                    .filter(el => el !== sidebar)
                    .map(el => ({
                        element: el,
                        display: el.style.display || ''
                    }));
            }

            hiddenSidebars.forEach(item => {
                item.element.style.display = 'none';
            });
        } else if (hiddenSidebars.length) {
            hiddenSidebars.forEach(item => {
                item.element.style.display = item.display;
            });
            hiddenSidebars = [];
        }
    }

    if (sidebar) {
        normalizeHighlightSidebarState();
        sidebar.style.display = active ? 'block' : 'none';
        sidebar.style.visibility = active ? 'visible' : 'hidden';
        if (active) {
            sidebar.style.position = 'sticky';
            sidebar.style.top = '';
        }
    }

    if (typeof window.adjustContentWidth === 'function') {
        window.adjustContentWidth();
    }

    syncSidebarCompanions();
}

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
                const modal = document.getElementById('quoteCommentModal');
                modal.innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">${data.result}</div>`;
                decorateHighlightThread(modal);
                if (window.autoExpandSingleReplyWraps) {
                    window.autoExpandSingleReplyWraps(modal);
                }
            } else {
                const sidebar = getHighlightSidebar();
                sidebar.innerHTML = data.result;
                decorateHighlightThread(sidebar);
                normalizeHighlightSidebarState();
                if (window.autoExpandSingleReplyWraps) {
                    window.autoExpandSingleReplyWraps(sidebar);
                }
            }
        } else {
            console.log('请求数据异常!', data);
        }
    }).fail(function () {

    });
}

function decorateHighlightThread(container) {
    if (!container) {
        return;
    }

    const widget = container.querySelector('.widget');
    if (!widget) {
        return;
    }

    widget.classList.add('highlight-comment-thread');
    if (widget.id === 'commentList') {
        widget.id = 'highlightCommentList';
    }

    const title = widget.querySelector('.com-nav-bar-title');
    const isSidebarContainer = container.id === 'quoteCommentSidebar';
    if (title && (!title.parentElement || !title.parentElement.classList.contains('highlight-comment-thread__head-main'))) {
        const existingHead = title.parentElement && title.parentElement.classList.contains('highlight-comment-thread__head')
            ? title.parentElement
            : null;
        const head = existingHead || document.createElement('div');
        head.className = 'highlight-comment-thread__head';
        const headMain = document.createElement('div');
        headMain.className = 'highlight-comment-thread__head-main';

        if (!existingHead) {
            title.parentNode.insertBefore(head, title);
        }

        head.appendChild(headMain);
        headMain.appendChild(title);

        const desc = document.createElement('p');
        desc.className = 'highlight-comment-thread__desc';
        desc.textContent = '围绕这段划线内容的讨论';
        headMain.appendChild(desc);
    }

    const head = widget.querySelector('.highlight-comment-thread__head');
    if (head && isSidebarContainer && !head.querySelector('.highlight-comment-thread__close')) {
        const closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'highlight-comment-thread__close';
        closeBtn.setAttribute('aria-label', '关闭划线评论');
        closeBtn.textContent = '×';
        head.appendChild(closeBtn);
    }

    const quoteContent = widget.querySelector('.quote-content');
    const quoteText = quoteContent ? quoteContent.querySelector('.quote-text') : null;
    if (quoteContent && quoteText && !quoteContent.querySelector('.highlight-comment-thread__quote-label')) {
        const label = document.createElement('div');
        label.className = 'highlight-comment-thread__quote-label';
        label.textContent = '划线片段';
        quoteContent.insertBefore(label, quoteText);
    }

    const count = 1 + Number(widget.querySelector('.expand-replies-wrap')?.dataset.commentCount || 0);
    const titleNode = widget.querySelector('.com-nav-bar-title');
    if (titleNode && count > 0 && isSidebarContainer) {
        titleNode.textContent = `划线评论（${count}）`;
    }
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
        const sidebar = getHighlightSidebar();

        if (sidebar) {
            toggleHighlightSidebarMode(true);

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
        const sidebar = getHighlightSidebar();
        // 重新初始化这块内容
        sidebar.innerHTML = `<div class="widget">
                <div class="highlight-comment-thread__head">
                  <div class="highlight-comment-thread__head-main">
                    <h3 class="com-nav-bar-title">划词评论</h3>
                    <p class="highlight-comment-thread__desc">围绕这段划线内容的讨论</p>
                  </div>
                  <button type="button" class="highlight-comment-thread__close" aria-label="关闭划线评论">×</button>
                </div>
                <div class="quote-content">
                  <div class="highlight-comment-thread__quote-label">划线片段</div>
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
            toggleHighlightSidebarMode(true);

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
    const sidebar = getHighlightSidebar();
    if (sidebar) {
        toggleHighlightSidebarMode(false);
    }
}

// 隐藏其他侧边栏
function hideOtherSidebars() {
    hiddenSidebars = [];
}

// 恢复其他侧边栏
function showOtherSidebars() {
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
    if (!quoteSidebarGlobalEventsBound) {
        quoteSidebarGlobalEventsBound = true;

        // 添加点击页面其他地方隐藏引用评论侧边栏的功能
        document.addEventListener('click', function (e) {
            const sidebar = getHighlightSidebar();
            const commentInput = document.getElementById('quoteCommentInput');
            const aiBotBtn = document.getElementById('sideAiBotBtn');
            const aiBotDropdown = document.getElementById('sideAiBotDropdown');

            if (e.target.closest('.highlight-comment-thread__close')) {
                hideQuoteCommentSidebar();
                e.stopPropagation();
                return;
            }

            if (aiBotBtn && aiBotBtn.contains(e.target)) {
                if (aiBotDropdown) {
                    aiBotDropdown.style.display = aiBotDropdown.style.display === 'block' ? 'none' : 'block';
                }
                e.stopPropagation();
                return;
            }

            if (e.target.classList.contains('ai-bot-option')) {
                const botType = e.target.getAttribute('data-bot');

                if (commentInput && aiBotDropdown) {
                    let prefix = '';
                    if (botType === 'hater') {
                        prefix = '@杠精派 ';
                    } else if (botType === 'smart') {
                        prefix = '@派聪明 ';
                    }

                    const currentValue = commentInput.value;
                    if (!currentValue.startsWith(prefix)) {
                        commentInput.value = prefix + currentValue;
                    }

                    aiBotDropdown.style.display = 'none';
                    commentInput.focus();
                    commentInput.dispatchEvent(new Event('input'));
                }
                e.stopPropagation();
                return;
            }

            if (aiBotDropdown && aiBotDropdown.style.display === 'block' && (!aiBotDropdown.contains(e.target))) {
                aiBotDropdown.style.display = 'none';
            }
        });
    }

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
                const sidebar = getHighlightSidebar();
                sidebar.innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">${data.html}</div>`;
                decorateHighlightThread(sidebar);
            } else {
                const sidebar = getHighlightSidebar();
                sidebar.innerHTML = data.html;
                decorateHighlightThread(sidebar);
            }
        });
    });
}

if (isMobileDevice()) {
    initQuoteModalEvent();
} else {
    initQuoteEvent();
}
