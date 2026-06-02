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
    const mainCommentSelector = '#commentDiv #aiBotSelector';

    function updateCommentCount() {
        const $commentTextarea = $('#commentContent');
        const $commentCount = $('#commentCount');
        const $commentBtn = $('#commentBtn');
        const currentLength = $commentTextarea.val() ? $commentTextarea.val().length : 0;

        $commentCount.text(currentLength);

        if (currentLength > 0) {
            $commentBtn.removeClass('c-btn-disabled').prop('disabled', false);
        } else {
            $commentBtn.addClass('c-btn-disabled').prop('disabled', true);
        }
    }

    function placeDropdown($button, $dropdown) {
        if (!$dropdown.hasClass('show')) {
            return;
        }

        const buttonRect = $button[0].getBoundingClientRect();
        const dropdownRect = $dropdown[0].getBoundingClientRect();
        const viewportHeight = window.innerHeight;

        $dropdown.css({
            top: '100%',
            bottom: 'auto'
        });

        if (buttonRect.bottom + dropdownRect.height > viewportHeight) {
            $dropdown.css({
                top: 'auto',
                bottom: '100%'
            });
        }
    }

    $(document).off('click.aiCommentBot', '#commentDiv #aiBotBtn')
        .on('click.aiCommentBot', '#commentDiv #aiBotBtn', function (e) {
            e.stopPropagation();
            const $button = $(this);
            const $selector = $button.closest(mainCommentSelector);
            const $dropdown = $selector.find('#aiBotDropdown');

            $('.ai-bot-dropdown').not($dropdown).removeClass('show');
            $dropdown.toggleClass('show');
            placeDropdown($button, $dropdown);
        });

    $(document).off('click.aiCommentBotClose')
        .on('click.aiCommentBotClose', function (e) {
            if (!$(e.target).closest(mainCommentSelector).length) {
                $('#commentDiv #aiBotDropdown').removeClass('show');
            }
        });

    $(document).off('click.aiCommentBotOption', '#commentDiv #aiBotSelector .ai-bot-option')
        .on('click.aiCommentBotOption', '#commentDiv #aiBotSelector .ai-bot-option', function (e) {
            e.stopPropagation();
            const botType = $(this).data('bot');
            const botName = botType === 'hater' ? '杠精派' : '派聪明';
            const tag = `@${botName} `;
            const $commentTextarea = $('#commentContent');
            const currentText = $commentTextarea.val() || '';

            if (!currentText.includes(tag)) {
                $commentTextarea.val(tag + currentText);
            }

            $('#commentDiv #aiBotDropdown').removeClass('show');
            $commentTextarea.focus().trigger('input');
        });

    $(document).off('input.aiCommentBot propertychange.aiCommentBot', '#commentContent')
        .on('input.aiCommentBot propertychange.aiCommentBot', '#commentContent', function () {
            updateCommentCount();
        });

    $(document).off('focus.aiCommentBot', '#commentContent')
        .on('focus.aiCommentBot', '#commentContent', function () {
            $(this).closest('.comment-input-container').addClass('focus');
        });

    $(document).off('blur.aiCommentBot', '#commentContent')
        .on('blur.aiCommentBot', '#commentContent', function () {
            $(this).closest('.comment-input-container').removeClass('focus');
        });

    updateCommentCount();
});

window.aiCommentBot = (function () {
    function enumName(botType) {
        return botType === 'hater' ? 'HATER_BOT' : 'QA_BOT';
    }

    function botName(botType) {
        return botType === 'hater' ? '杠精派' : '派聪明';
    }

    function detect(commentContent) {
        if (!commentContent) {
            return null;
        }
        if (commentContent.indexOf('@杠精派') >= 0) {
            return 'hater';
        }
        if (commentContent.indexOf('@派聪明') >= 0) {
            return 'smart';
        }
        return null;
    }

    function stripMention(commentContent) {
        return (commentContent || '').replace(/@(杠精派|派聪明)\s*/g, '').trim();
    }

    function parseSseChunk(chunk, handleEvent) {
        const data = chunk.split('\n')
            .filter(line => line.indexOf('data:') === 0)
            .map(line => line.substring(5).trim())
            .join('\n');

        if (!data) {
            return;
        }

        try {
            handleEvent(JSON.parse(data));
        } catch (e) {
            console.debug('解析评论区 AI SSE 事件失败:', data, e);
        }
    }

    function readStream(reader, decoder, buffer, handleEvent) {
        return reader.read().then(function process(result) {
            if (result.done) {
                if (buffer.trim()) {
                    parseSseChunk(buffer.trim(), handleEvent);
                }
                return;
            }

            buffer += decoder.decode(result.value, { stream: true }).replace(/\r\n/g, '\n');
            let splitIndex = buffer.indexOf('\n\n');
            while (splitIndex >= 0) {
                const chunk = buffer.substring(0, splitIndex);
                buffer = buffer.substring(splitIndex + 2);
                parseSseChunk(chunk, handleEvent);
                splitIndex = buffer.indexOf('\n\n');
            }

            return reader.read().then(process);
        });
    }

    function startStream(params, callbacks) {
        const botType = detect(params.commentContent);
        if (!botType) {
            return false;
        }

        const requestId = String(Date.now()) + String(Math.floor(Math.random() * 1000));
        const options = callbacks || {};

        fetch('/comment/api/commentAiStream', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                articleId: params.articleId,
                bot: enumName(botType),
                commentContent: params.commentContent,
                parentCommentId: params.parentCommentId,
                topCommentId: params.topCommentId,
                question: stripMention(params.commentContent),
                requestId: requestId
            })
        }).then(function (response) {
            if (!response.ok || !response.body) {
                throw new Error('AI 评论请求失败');
            }

            function handleEvent(event) {
                if (!event || event.requestId !== requestId) {
                    return;
                }
                if (event.type === 'comment' && event.html && options.onComment) {
                    options.onComment(event);
                } else if (event.type === 'delta' && options.onDelta) {
                    options.onDelta(event);
                } else if (event.type === 'done') {
                    if (options.onDone) {
                        options.onDone(event);
                    }
                    toastr.success(botName(botType) + ' 回复已生成');
                } else if (event.type === 'error') {
                    if (event.html && options.onErrorHtml) {
                        options.onErrorHtml(event);
                    }
                    toastr.error(event.message || 'AI 回复生成失败');
                }
            }

            return readStream(response.body.getReader(), new TextDecoder('utf-8'), '', handleEvent);
        }).catch(function () {
            toastr.error('AI 回复生成失败，请稍后再试');
        }).finally(function () {
            if (options.onFinish) {
                options.onFinish();
            }
        });

        return true;
    }

    return {
        detect: detect,
        enumName: enumName,
        botName: botName,
        stripMention: stripMention,
        startStream: startStream
    };
})();
