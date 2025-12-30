/**
 * 评论区 Markdown 渲染支持
 * 用于将评论内容中的 Markdown 格式转换为 HTML
 */

(function() {
    'use strict';

    // 检查 marked.js 是否加载
    function ensureMarkedLoaded(callback) {
        if (typeof marked !== 'undefined') {
            callback();
        } else {
            // 动态加载 marked.js
            const script = document.createElement('script');
            script.src = '/js/marked.min.js';
            script.onload = callback;
            script.onerror = function() {
                console.error('Failed to load marked.js');
                // 降级使用基础渲染
                callback();
            };
            document.head.appendChild(script);
        }
    }

    // 基础 Markdown 渲染 (降级方案)
    function basicMarkdownRender(text) {
        if (!text) return '';

        // HTML 转义已经在后端完成,这里直接处理 Markdown 语法
        let html = text;

        // 先处理 **`code`** 模式，转换为 `code`
        html = html.replace(/\*\*`([^`]+)`\*\*/g, '`$1`');

        // 清理 **<code>...</code>** 两侧的 ** 符号，保留 <code> 标签
        html = html.replace(/\*\*<code>/g, '<code>').replace(/<\/code>\*\*/g, '</code>');

        // 代码块 (多行)
        html = html.replace(/```(\w*)\n([\s\S]*?)```/g, function(match, lang, code) {
            return '<pre><code class="language-' + lang + '">' + code + '</code></pre>';
        });

        // 行内代码
        html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

        // 图片
        html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" style="max-width: 100%;" />');

        // 链接
        html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');

        // 加粗
        html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>');

        // 斜体
        html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
        html = html.replace(/_([^_]+)_/g, '<em>$1</em>');

        // 删除线
        html = html.replace(/~~([^~]+)~~/g, '<del>$1</del>');

        // 换行
        html = html.replace(/\n/g, '<br/>');

        return html;
    }

    // 使用 marked.js 渲染 Markdown
    function markedRender(text) {
        if (!text) return '';

        try {
            if (typeof marked !== 'undefined') {
                // 配置 marked
                marked.setOptions({
                    breaks: true,   // 启用单个换行符转换为 <br>
                    gfm: true,     // 启用 GitHub Flavored Markdown
                    sanitize: false, // 不进行 HTML 清理,因为后端已经转义
                    smartLists: true,
                    smartypants: true
                });

                // 预处理：保护 <code> 标签，使用占位符
                const codeMap = {};
                let processedText = text.replace(/<code>([\s\S]*?)<\/code>/g, function(match, content) {
                    const placeholder = '__CODE_PLACEHOLDER_' + Object.keys(codeMap).length + '__';
                    codeMap[placeholder] = '<code>' + content + '</code>';
                    return placeholder;
                });

                let html = marked.parse(processedText);

                // 恢复 <code> 标签
                Object.keys(codeMap).forEach(function(placeholder) {
                    html = html.replace(new RegExp(placeholder, 'g'), codeMap[placeholder]);
                });

                // 对于评论，移除顶层 <p> 标签包裹，避免样式问题
                // 如果整个内容只是一个 <p> 标签包裹，则移除外层的 <p> 和 </p>
                html = html.replace(/^<p>([\s\S]+?)<\/p>$/, '$1');

                // 清理多余的空段落和空白行
                html = html.replace(/<p>\s*<\/p>/g, ''); // 删除空段落
                html = html.replace(/(<\/p>)\s*(<p>)/g, '</p><p>'); // 合并连续段落间的空白
                html = html.replace(/\n{3,}/g, '\n\n'); // 限制最多2个连续换行

                return html;
            }
        } catch (e) {
            console.error('Marked.js rendering error:', e);
        }

        // 降级到基础渲染
        return basicMarkdownRender(text);
    }

    // 渲染单个评论内容
    function renderCommentContent(element) {
        if (!element || element.dataset.rendered === 'true') {
            return; // 已经渲染过,跳过
        }

        // 获取原始内容，如果是 HTML 实体，需要解码
        let rawContent = element.textContent || element.innerText;

        // 解码 HTML 实体（如 &lt; 转换为 <）
        const textarea = document.createElement('textarea');
        textarea.innerHTML = rawContent;
        rawContent = textarea.value;

        // 清理 **`code`** 模式，转换为 `code`
        rawContent = rawContent.replace(/\*\*`([^`]+)`\*\*/g, '`$1`');

        // 清理 **<code>...</code>** 两侧的 ** 符号
        rawContent = rawContent.replace(/\*\*<code>/g, '<code>').replace(/<\/code>\*\*/g, '</code>');

        const commentId = element.dataset.commentId;

        // 检测是否包含 Markdown 语法
        const hasMarkdown = /[`*_~\[\]!]|```|\n/.test(rawContent);

        if (hasMarkdown) {
            // 渲染 Markdown
            const renderedHtml = markedRender(rawContent);
            element.innerHTML = renderedHtml;

            // 添加样式类
            element.classList.add('markdown-rendered');

            // 标记已渲染
            element.dataset.rendered = 'true';

            console.log('Rendered markdown for comment:', commentId);
        } else {
            // 纯文本,保持原样但标记已处理
            element.dataset.rendered = 'true';
        }
    }

    // 渲染所有评论内容
    function renderAllComments() {
        const commentElements = document.querySelectorAll('.comment-content-markdown');

        commentElements.forEach(function(element) {
            renderCommentContent(element);
        });

        console.log('Rendered', commentElements.length, 'comments');
    }

    // 初始化函数
    function initCommentMarkdown() {
        ensureMarkedLoaded(function() {
            renderAllComments();
        });
    }

    // 导出到全局,供其他脚本调用
    window.renderCommentMarkdown = renderAllComments;
    window.renderSingleComment = renderCommentContent;

    // DOM 加载完成后自动初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCommentMarkdown);
    } else {
        // DOM 已经加载完成,直接执行
        initCommentMarkdown();
    }

    // 监听动态内容变化 (用于 Ajax 加载的评论)
    const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.addedNodes.length) {
                mutation.addedNodes.forEach(function(node) {
                    if (node.nodeType === 1) { // Element node
                        // 检查新添加的节点中是否有评论内容
                        const newComments = node.querySelectorAll ?
                            node.querySelectorAll('.comment-content-markdown') : [];

                        newComments.forEach(function(element) {
                            renderCommentContent(element);
                        });

                        // 检查节点本身是否是评论内容
                        if (node.classList && node.classList.contains('comment-content-markdown')) {
                            renderCommentContent(node);
                        }
                    }
                });
            }
        });
    });

    // 观察评论区域的变化
    const observeCommentArea = function() {
        const commentDiv = document.getElementById('commentDiv');
        const sidebarDiv = document.getElementById('quoteCommentSidebar');

        if (commentDiv) {
            observer.observe(commentDiv, {
                childList: true,
                subtree: true
            });
        }

        if (sidebarDiv) {
            observer.observe(sidebarDiv, {
                childList: true,
                subtree: true
            });
        }
    };

    // 延迟启动观察器,确保页面加载完成
    setTimeout(observeCommentArea, 1000);

})();
