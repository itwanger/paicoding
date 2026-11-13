// 文本选择功能
let selectionRange = null;
let toSaveSelection = null;
let highlightViewerId = '';
let relocatingComment = null;
let articleHighlights = [];
let highlightViewVersion = 0;
// 保存被隐藏的侧边栏元素
let hiddenSidebars = [];
// 标记是否正在处理评论图标点击
let isHandlingCommentIcon = false;
let quoteSidebarGlobalEventsBound = false;
let highlightAiActiveRequest = null;
const HIGHLIGHT_AI_REQUEST_TIMEOUT_MS = 185 * 1000;
const FORBID_NOT_LOGIN_CODE = 100403003;

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
    return window.innerWidth <= 991;
}

// 监听文本选择事件
document.addEventListener('selectionchange', function () {
    // 如果正在处理评论图标点击，忽略选择变化
    if (isHandlingCommentIcon) {
        // console.debug('正在处理评论图标点击，忽略选择变化');
        return;
    }

    const selection = window.getSelection();
    selectionRange = selection.rangeCount ? selection.getRangeAt(0) : null;
    if (selectionRange && selection.toString().trim() && canShowCommentIcon(selectionRange)) {
        showCommentIcon(selectionRange);
    } else {
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

// 显示评论图标
function showCommentIcon(range) {
    // 移除已存在的评论图标
    hideCommentIcon();

    // 创建评论图标
    const commentIcon = document.createElement('div');
    commentIcon.id = 'comment-icon';
    if (relocatingComment) {
        commentIcon.classList.add('highlight-relocate-icon');
        commentIcon.textContent = '定位到这里';
    } else {
        commentIcon.textContent = '🤖';
    }


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

    commentIcon.addEventListener('click', handleCommentIconClick);
}

// 评论图标点击处理函数
function handleCommentIconClick(e) {
    e.stopPropagation();
    e.preventDefault();

    const anchor = rangeToElementJSON(selectionRange);
    if (!anchor) return;
    if (relocatingComment) {
        saveRelocatedHighlight(anchor);
        hideCommentIcon();
        isHandlingCommentIcon = false;
        return;
    }
    toSaveSelection = anchor;
    showQuoteCommentForm(anchor.selectedText.trim());
    hideCommentIcon();
    renderArticleHighlights();
    isHandlingCommentIcon = false;

    // 聚焦到评论输入框
    setTimeout(function () {
        const commentInput = isMobileDevice() ? document.getElementById('quoteCommentInputModal') : document.getElementById('quoteCommentInput');
        if (commentInput) {
            commentInput.focus();
        }
    }, 100);
}



function initUnderlineToSelection(commentList, viewerId) {
    highlightViewerId = viewerId ? String(viewerId) : '';
    articleHighlights = (commentList || []).filter(comment => comment.highlight?.selectedText);
    const controls = document.getElementById('highlightControls');
    if (controls) {
        controls.onchange = renderArticleHighlights;
        // Ordinary articles replace the desktop sidebar with a bottom toolbar on small screens.
        const mobileBar = document.querySelector('.article-suspended-panel-md');
        if (mobileBar && !controls.dataset.editionPlacementBound) {
            controls.dataset.editionPlacementBound = 'true';
            const sidebar = controls.parentElement;
            const next = controls.nextSibling;
            const smallScreen = window.matchMedia('(max-width: 768px)');
            const placeFilter = () => {
                if (smallScreen.matches) mobileBar.append(controls);
                else sidebar.insertBefore(controls, next);
            };
            placeFilter();
            smallScreen.addEventListener('change', placeFilter);
        }
    }
    // Syntax highlighting can replace inline DOM during DOMContentLoaded.
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', renderArticleHighlights, {once: true});
    } else {
        renderArticleHighlights();
    }
}

function isOwnHighlight(comment) {
    return highlightViewerId && String(comment.userId) === highlightViewerId;
}

function highlightThreadLabel(comment) {
    const author = isOwnHighlight(comment) ? '我' : (comment.userName || '读者');
    return author + ' · ' + (comment.commentContent || comment.highlight.selectedText).slice(0, 50);
}

function renderArticleHighlights() {
    const article = document.getElementById('articleContent');
    if (!article) return;
    // Split only text nodes. Existing links, inline code and their event handlers stay in place.
    const parents = new Set();
    article.querySelectorAll('span[data-highlight-segment]').forEach(span => {
        parents.add(span.parentNode);
        span.replaceWith(...span.childNodes);
    });
    parents.forEach(parent => parent.normalize());

    const filter = document.getElementById('highlightMineOnly');
    const index = indexHighlightText(article);
    const located = [];
    const detached = [];
    articleHighlights.forEach(comment => {
        if (filter?.checked && !isOwnHighlight(comment)) return;
        const match = locateHighlight(comment.highlight, index);
        if (match) located.push({...match, comment});
        else detached.push(comment);
    });
    if (toSaveSelection && !articleHighlights.some(item => item.highlight === toSaveSelection)) {
        const draft = locateHighlight(toSaveSelection, index);
        if (draft) located.push(draft);
    }
    for (const {node, start, end} of index.nodes) {
        const overlaps = located.filter(mark => mark.start < end && mark.end > start);
        if (!overlaps.length) continue;
        const cuts = new Set([start, end]);
        overlaps.forEach(mark => {
            cuts.add(Math.max(mark.start, start));
            cuts.add(Math.min(mark.end, end));
        });
        const points = Array.from(cuts).sort((a, b) => a - b);
        // Split from right to left, so each offset still addresses the original text node.
        for (let i = points.length - 2; i >= 0; i--) {
            const pos = points[i];
            const piece = pos === start ? node : node.splitText(pos - start);
            const marks = overlaps.filter(mark => mark.start <= pos && pos < mark.end);
            if (marks.length) wrapHighlightSegment(piece, marks);
        }
    }
    const controls = document.getElementById('highlightControls');
    if (controls) {
        controls.hidden = !articleHighlights.length;
        controls.querySelector('[data-count="all"]').textContent = articleHighlights.length;
        controls.querySelector('[data-count="mine"]').textContent = articleHighlights.filter(isOwnHighlight).length;
    }
    renderDetachedHighlights(detached);
}

function wrapHighlightSegment(node, marks) {
    const comments = marks.map(mark => mark.comment).filter(Boolean);
    const span = document.createElement('span');
    span.dataset.highlightSegment = '';
    span.className = 'selected-text-highlight';
    span.classList.toggle('highlight-own', comments.some(isOwnHighlight));
    span.classList.toggle('highlight-draft', marks.length > comments.length);
    if (comments.length) {
        span.tabIndex = 0;
        span.setAttribute('role', 'button');
        span.onkeydown = event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                span.click();
            }
        };
        span.title = comments.length > 1 ? comments.length + ' 条划线讨论' : highlightThreadLabel(comments[0]);
        span.onclick = event => {
            if (!window.getSelection().isCollapsed) return;
            event.preventDefault();
            event.stopPropagation();
            const first = comments.find(isOwnHighlight) || comments[0];
            showQuoteCommentWithComments(first.commentId, comments);
        };
    }
    node.replaceWith(span);
    span.appendChild(node);
}

function renderDetachedHighlights(comments) {
    const panel = document.getElementById('detachedHighlights');
    if (!panel) return;
    panel.hidden = !comments.length || !!document.querySelector('.needlock');
    if (panel.hidden) {
        panel.querySelector('ul').replaceChildren();
        return;
    }
    panel.querySelector('summary').textContent = '未能定位到原文的划线（' + comments.length + '）';
    const list = panel.querySelector('ul');
    list.replaceChildren();
    comments.forEach(comment => {
        const item = document.createElement('li');
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = comment.highlight.selectedText;
        button.title = highlightThreadLabel(comment);
        button.onclick = () => showQuoteCommentWithComments(comment.commentId);
        item.appendChild(button);
        if (isOwnHighlight(comment)) {
            const relocate = document.createElement('button');
            relocate.type = 'button';
            relocate.textContent = '重新定位';
            relocate.onclick = () => startHighlightRelocation(comment);
            item.appendChild(relocate);
        }
        list.appendChild(item);
    });
}

function startHighlightRelocation(comment) {
    clearHighlightDraft();
    relocatingComment = comment;
    const prompt = document.getElementById('highlightRelocation');
    prompt.hidden = false;
    prompt.querySelector('button').onclick = cancelHighlightRelocation;
    prompt.scrollIntoView({block: 'center'});
}

function cancelHighlightRelocation() {
    relocatingComment = null;
    document.getElementById('highlightRelocation').hidden = true;
    hideCommentIcon();
}

function saveRelocatedHighlight(anchor) {
    const comment = relocatingComment;
    const url = '/comment/api/relocateHighlight?articleId=' + encodeURIComponent(articleId)
        + '&commentId=' + encodeURIComponent(comment.commentId);
    anchor.revision = comment.highlight.revision;
    post(url, anchor, updated => {
        // A slower response must not undo a subsequent relocation in this tab.
        if ((comment.highlight.revision || 0) > updated.revision) return;
        comment.highlight = updated;
        if (relocatingComment === comment) cancelHighlightRelocation();
        renderArticleHighlights();
        toastr.success('已更新划线位置');
    });
}

function addThreadSwitcher(container, comments, commentId) {
    if (comments.length < 2) return;
    const select = document.createElement('select');
    select.className = 'form-control';
    select.setAttribute('aria-label', '切换划线讨论');
    comments.forEach(comment => {
        const option = document.createElement('option');
        option.value = comment.commentId;
        option.textContent = highlightThreadLabel(comment);
        select.appendChild(option);
    });
    select.value = String(commentId);
    select.onchange = () => showQuoteCommentWithComments(select.value, comments);
    container.querySelector('.quote-content')?.prepend(select);
}

// 点击划线内容,加载评论数据
function loadCommentData(commentId, isModal, comments, version) {
    // 调用后端API获取评论数据
    $.get('/comment/api/listTopComment?commentId=' + commentId, function (data) {
        if (version !== highlightViewVersion) return;
        if (data && data.status && data.status.code === 0) {
            // 处理获取到的评论数据
            if (isModal) {
                const modal = document.getElementById('quoteCommentModal');
                modal.innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">${data.result}</div>`;
                decorateHighlightThread(modal);
                addThreadSwitcher(modal, comments, commentId);
                if (window.autoExpandSingleReplyWraps) {
                    window.autoExpandSingleReplyWraps(modal);
                }
            } else {
                const sidebar = getHighlightSidebar();
                sidebar.innerHTML = data.result;
                decorateHighlightThread(sidebar);
                addThreadSwitcher(sidebar, comments, commentId);
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

    if (!isSidebarContainer) {
        widget.querySelector('.highlight-comment-thread__close')?.setAttribute('data-dismiss', 'modal');
    }
    const count = 1 + Number(widget.querySelector('.expand-replies-wrap')?.dataset.commentCount || 0);
    const titleNode = widget.querySelector('.com-nav-bar-title');
    if (titleNode && count > 0 && isSidebarContainer) {
        titleNode.textContent = `划线评论（${count}）`;
    }
}


// 显示引用的评论信息
function showQuoteCommentWithComments(commentId, comments = []) {
    const version = ++highlightViewVersion;
    clearHighlightDraft();
    invalidateHighlightAiRequest('view-replaced');
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
            loadCommentData(commentId, true, comments, version);
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
            loadCommentData(commentId, false, comments, version);
        }
    }
}


function clearHighlightDraft() {
    if (!toSaveSelection) return;
    toSaveSelection = null;
    renderArticleHighlights();
}

// 隐藏评论图标
function hideCommentIcon() {
    const commentIcon = document.getElementById('comment-icon');
    if (commentIcon) {
        commentIcon.removeEventListener('click', handleCommentIconClick);
        commentIcon.remove();
    }
}

function showQuoteCommentForm(text) {
    ++highlightViewVersion;
    invalidateHighlightAiRequest('view-replaced');
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
                  <div class="quote-text" id="quotedText"></div>
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
                    <div class="highlight-ai-panel" id="highlightAiPanel">
                      <div class="highlight-ai-status" id="highlightAiStatus"></div>
                      <div class="highlight-ai-reply" id="highlightAiReply"></div>
                    </div>
                  </div>
                </div>
              </div>`

        // 重新绑定监听事件
        initQuoteEvent();
        const quotedTextNode = document.getElementById('quotedText');
        if (quotedTextNode) {
            quotedTextNode.textContent = text;
        }
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
    ++highlightViewVersion;
    clearHighlightDraft();
    invalidateHighlightAiRequest('view-hidden');
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

function highlightAiBotEnum(botType) {
    return botType === 'hater' ? 'HATER_BOT' : 'QA_BOT';
}

function highlightAiBotName(botType) {
    return botType === 'hater' ? '杠精派' : '派聪明';
}

function detectHighlightAiBot(commentContent) {
    if (commentContent.indexOf('@杠精派') >= 0) {
        return 'hater';
    }
    if (commentContent.indexOf('@派聪明') >= 0) {
        return 'smart';
    }
    return null;
}

function stripHighlightAiMention(commentContent) {
    return commentContent.replace(/@(杠精派|派聪明)\s*/g, '').trim();
}

function isHighlightCommentLoggedIn() {
    const navbar = document.querySelector('nav.navbar[data-islogin]');
    if (navbar) {
        return navbar.getAttribute('data-islogin') === 'true';
    }

    // 兼容未渲染新版 navbar 属性的页面；无法判断时交给服务端鉴权。
    if (typeof navbarIsLogin !== 'undefined') {
        return !!navbarIsLogin;
    }
    return true;
}

function showHighlightLoginRequired(message) {
    const prompt = message || '请先登录后再提问';
    toastr.error(prompt);
    if (typeof window.jQuery === 'function' && document.getElementById('loginModal')) {
        window.jQuery('#loginModal').modal('show');
    }
}

function createHighlightAiRequestError(message, code, loginRequired) {
    const error = new Error(message || 'AI 回复请求失败');
    error.userMessage = message;
    error.code = code;
    error.loginRequired = !!loginRequired;
    return error;
}

function validateHighlightAiResponse(response) {
    const contentType = response && response.headers
        ? (response.headers.get('content-type') || '').toLowerCase()
        : '';

    if (response && response.ok && response.body && contentType.indexOf('text/event-stream') >= 0) {
        return Promise.resolve(response);
    }

    const readBody = response && typeof response.text === 'function'
        ? response.text()
        : Promise.resolve('');

    return readBody.then(function (body) {
        let payload = null;
        try {
            payload = body ? JSON.parse(body) : null;
        } catch (ignore) {
            // 非 JSON 响应会使用下面的协议错误提示。
        }

        const status = payload && payload.status ? payload.status : null;
        const code = status ? status.code : null;
        const loginRequired = code === FORBID_NOT_LOGIN_CODE
            || (response && (response.status === 401 || response.status === 403));
        let message = status && status.msg ? status.msg : '';
        if (loginRequired) {
            message = '请先登录后再提问';
        } else if (!message && contentType.indexOf('text/event-stream') < 0) {
            message = 'AI 回复服务返回格式异常';
        } else if (!message) {
            message = 'AI 回复请求失败';
        }

        throw createHighlightAiRequestError(message, code, loginRequired);
    });
}

function isActiveHighlightAiRequest(request) {
    return !!request && highlightAiActiveRequest === request;
}

function invalidateHighlightAiRequest(reason) {
    const request = highlightAiActiveRequest;
    if (!request) {
        return;
    }

    // 先同步作废，再终止网络请求；旧请求的异步 catch/finally 不得改写新侧栏。
    highlightAiActiveRequest = null;
    request.abortReason = reason || 'invalidated';
    if (request.timeoutId) {
        clearTimeout(request.timeoutId);
        request.timeoutId = null;
    }
    if (request.controller && !request.controller.signal.aborted) {
        request.controller.abort();
    }
}

function settleHighlightAiRequest(request, message) {
    if (!isActiveHighlightAiRequest(request) || request.uiSettled) {
        return;
    }
    request.uiSettled = true;
    finishHighlightAiPanel(message);
}

function setHighlightCommentSubmitting(submitting) {
    const commentInput = document.getElementById('quoteCommentInput');
    const submitBtn = document.getElementById('submitQuoteComment');

    if (commentInput) {
        commentInput.disabled = submitting;
    }
    if (submitBtn) {
        submitBtn.disabled = submitting || !commentInput || commentInput.value.trim() === '';
    }
}

function resetHighlightAiPanel(botType) {
    const panel = document.getElementById('highlightAiPanel');
    const status = document.getElementById('highlightAiStatus');
    const reply = document.getElementById('highlightAiReply');

    if (panel) {
        panel.style.display = 'block';
        panel.classList.add('highlight-ai-panel--streaming');
    }
    if (status) {
        status.textContent = highlightAiBotName(botType) + ' 正在回复...';
    }
    if (reply) {
        delete reply.dataset.rawMarkdown;
        delete reply.dataset.rendered;
        reply.classList.remove('comment-content-markdown');
        reply.classList.remove('markdown-rendered');
        reply.textContent = '';
    }
    setHighlightCommentSubmitting(true);
}

function renderHighlightAiReply(markdownText) {
    const reply = document.getElementById('highlightAiReply');
    if (!reply) {
        return;
    }

    const rawMarkdown = markdownText || '';
    reply.dataset.rawMarkdown = rawMarkdown;
    reply.classList.add('comment-content-markdown');

    if (window.renderCommentMarkdownText) {
        window.renderCommentMarkdownText(reply, rawMarkdown);
    } else {
        reply.textContent = rawMarkdown;
    }
}

function finishHighlightAiPanel(message) {
    const panel = document.getElementById('highlightAiPanel');
    const status = document.getElementById('highlightAiStatus');

    if (panel) {
        panel.classList.remove('highlight-ai-panel--streaming');
    }
    if (status && message) {
        status.textContent = message;
    }
    setHighlightCommentSubmitting(false);
}

function bindGeneratedHighlightComment(commentId, anchor, content) {
    if (!commentId || !anchor || articleHighlights.some(item => String(item.commentId) === String(commentId))) return;
    articleHighlights.push({commentId, userId: highlightViewerId, highlight: anchor, commentContent: content});
    renderArticleHighlights();
}

function handleHighlightAiEvent(request, event) {
    if (!isActiveHighlightAiRequest(request)
        || request.terminal
        || !event
        || event.requestId !== request.requestId) {
        return;
    }

    const reply = document.getElementById('highlightAiReply');
    const status = document.getElementById('highlightAiStatus');

    if (event.type === 'delta') {
        if (reply) {
            renderHighlightAiReply(event.content || ((reply.dataset.rawMarkdown || '') + (event.delta || '')));
        }
        return;
    }

    if (event.type === 'comment') {
        bindGeneratedHighlightComment(event.commentId, request.anchor, request.content);
        if (status) {
            status.textContent = event.bot + ' 正在回复...';
        }
        return;
    }

    if (event.type === 'done') {
        request.terminal = true;
        bindGeneratedHighlightComment(event.commentId, request.anchor, request.content);
        toggleHighlightSidebarMode(true);
        normalizeHighlightSidebarState();
        settleHighlightAiRequest(request, '回复已生成');
        toastr.success('AI 回复已生成');
        return;
    }

    if (event.type === 'error') {
        request.terminal = true;
        if (event.commentId) {
            bindGeneratedHighlightComment(event.commentId, request.anchor, request.content);
        }
        if (event.html) {
            const sidebar = getHighlightSidebar();
            if (sidebar) {
                sidebar.innerHTML = event.html;
                decorateHighlightThread(sidebar);
                normalizeHighlightSidebarState();
                if (window.autoExpandSingleReplyWraps) {
                    window.autoExpandSingleReplyWraps(sidebar);
                }
            }
        }
        const message = event.message || 'AI 回复生成失败';
        settleHighlightAiRequest(request, message);
        toastr.error(message);
    }
}

function parseHighlightAiSseChunk(request, chunk) {
    const data = chunk.split('\n')
        .filter(line => line.indexOf('data:') === 0)
        .map(line => line.substring(5).trim())
        .join('\n');

    if (!data) {
        return;
    }

    let event;
    try {
        event = JSON.parse(data);
    } catch (e) {
        console.debug('解析划线 AI SSE 事件失败:', data, e);
        throw createHighlightAiRequestError('AI 回复数据解析失败');
    }
    handleHighlightAiEvent(request, event);
}

function readHighlightAiStream(request, reader, decoder, buffer) {
    function process(result) {
        if (!isActiveHighlightAiRequest(request)) {
            return;
        }

        if (result.done) {
            buffer += decoder.decode().replace(/\r\n/g, '\n');
            if (buffer.trim()) {
                parseHighlightAiSseChunk(request, buffer.trim());
            }
            if (!request.terminal) {
                throw createHighlightAiRequestError('AI 回复连接已中断，请稍后再试');
            }
            return;
        }

        buffer += decoder.decode(result.value, { stream: true }).replace(/\r\n/g, '\n');
        let splitIndex = buffer.indexOf('\n\n');
        while (splitIndex >= 0) {
            const chunk = buffer.substring(0, splitIndex);
            buffer = buffer.substring(splitIndex + 2);
            parseHighlightAiSseChunk(request, chunk);
            splitIndex = buffer.indexOf('\n\n');
        }

        if (request.terminal) {
            if (typeof reader.cancel === 'function') {
                return reader.cancel().catch(function () {
                    // 已收到终态，取消剩余响应失败不再影响界面结果。
                });
            }
            return;
        }

        return reader.read().then(process);
    }

    return reader.read().then(process);
}

function startHighlightAiReply(botType, commentContent) {
    if (!toSaveSelection) {
        toastr.error('请先选择一段文章内容');
        return;
    }

    if (!isHighlightCommentLoggedIn()) {
        showHighlightLoginRequired();
        return;
    }

    const previousRequest = highlightAiActiveRequest;
    const question = stripHighlightAiMention(commentContent);
    const request = {
        requestId: String(Date.now()) + String(Math.floor(Math.random() * 1000)),
        controller: new AbortController(),
        anchor: toSaveSelection,
        content: commentContent,
        terminal: false,
        uiSettled: false,
        abortReason: null,
        timeoutId: null
    };
    highlightAiActiveRequest = request;
    if (previousRequest && previousRequest.controller) {
        previousRequest.abortReason = 'superseded';
        previousRequest.controller.abort();
    }

    resetHighlightAiPanel(botType);
    request.timeoutId = setTimeout(function () {
        if (isActiveHighlightAiRequest(request) && !request.terminal) {
            request.abortReason = 'timeout';
            request.controller.abort();
        }
    }, HIGHLIGHT_AI_REQUEST_TIMEOUT_MS);

    fetch('/comment/api/highlightAiStream', {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json',
            ...(window.articleEditionHeaders ? window.articleEditionHeaders() : {})
        },
        body: JSON.stringify({
            ...(window.articleEditionCommentContext ? window.articleEditionCommentContext() : {}),
            articleId: articleId,
            highlight: request.anchor,
            bot: highlightAiBotEnum(botType),
            commentContent: commentContent,
            question: question,
            requestId: request.requestId
        }),
        signal: request.controller.signal
    }).then(validateHighlightAiResponse).then(function (response) {
        return readHighlightAiStream(request, response.body.getReader(), new TextDecoder('utf-8'), '');
    }).catch(function (e) {
        if (!isActiveHighlightAiRequest(request)) {
            return;
        }

        if (e.name === 'AbortError') {
            if (request.abortReason === 'timeout') {
                settleHighlightAiRequest(request, 'AI 回复超时，请稍后再试');
                toastr.error('AI 回复超时，请稍后再试');
            } else if (request.abortReason === 'user') {
                settleHighlightAiRequest(request, '已停止生成');
            } else {
                settleHighlightAiRequest(request, 'AI 回复生成失败');
                toastr.error('AI 回复生成失败，请稍后再试');
            }
            return;
        }

        const message = e.userMessage || 'AI 回复生成失败，请稍后再试';
        settleHighlightAiRequest(request, message);
        if (e.loginRequired) {
            showHighlightLoginRequired(message);
        } else {
            toastr.error(message);
        }
    }).finally(function () {
        if (request.timeoutId) {
            clearTimeout(request.timeoutId);
        }
        if (!isActiveHighlightAiRequest(request)) {
            return;
        }
        if (!request.uiSettled) {
            settleHighlightAiRequest(request, 'AI 回复生成失败');
        }
        highlightAiActiveRequest = null;
    });
}

function stopHighlightAiReply() {
    const request = highlightAiActiveRequest;
    if (request && request.controller && !request.terminal) {
        request.abortReason = 'user';
        request.controller.abort();
    }
}

// 初始化弹窗事件
function initQuoteModalEvent() {
    // 监听引用评论输入框
    const commentInput = document.getElementById('quoteCommentInputModal');
    const submitBtn = document.getElementById('submitQuoteCommentModal');

    if (commentInput && submitBtn && !submitBtn.dataset.quoteBound) {
        submitBtn.dataset.quoteBound = 'true';
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

            const version = highlightViewVersion;
            post("/comment/api/highlightComment", params, function (data) {
                bindGeneratedHighlightComment(data.commentId, params.highlight, commentContent);
                if (version !== highlightViewVersion) return;
                // 显示成功消息
                toastr.success("评论发表成功");
                const modal = document.getElementById('quoteCommentModal');
                modal.innerHTML = `<div class="modal-dialog modal-dialog-centered" role="document">${data.html}</div>`;
                decorateHighlightThread(modal);
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

            if (e.target.classList.contains('ai-bot-option') && e.target.closest('#sideAiBotSelector')) {
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

    const submitNode = document.getElementById('submitQuoteComment');
    if (!submitNode || submitNode.dataset.quoteBound) return;
    submitNode.dataset.quoteBound = 'true';
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

        const aiBotType = detectHighlightAiBot(commentContent);
        if (aiBotType) {
            startHighlightAiReply(aiBotType, commentContent);
            return;
        }

        // 提交评论
        const params = {
            articleId: articleId,
            commentContent: commentContent,
            highlight: toSaveSelection,
        };

        const version = highlightViewVersion;
        post("/comment/api/highlightComment", params, function (data) {
            bindGeneratedHighlightComment(data.commentId, params.highlight, commentContent);
            if (version !== highlightViewVersion) return;
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
    $(document).on('hidden.bs.modal', '#quoteCommentModal', function (event) {
        if (event.target !== this) return;
        ++highlightViewVersion;
        clearHighlightDraft();
    });
    initQuoteModalEvent();
} else {
    initQuoteEvent();
}

// Called before replacing a version's body; no listeners are rebound here.
function resetEditionHighlights() {
    invalidateHighlightAiRequest('edition-switch');
    hideQuoteCommentSidebar();
    hideCommentIcon();
    selectionRange = null;
    toSaveSelection = null;
    relocatingComment = null;
    articleHighlights = [];
    const relocation = document.getElementById('highlightRelocation');
    if (relocation) relocation.hidden = true;
    const detached = document.getElementById('detachedHighlights');
    if (detached) { detached.hidden = true; detached.querySelector('ul').replaceChildren(); }
    window.getSelection()?.removeAllRanges();
}
