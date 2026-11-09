const highlightBlocks = 'p,li,ul,h1,h2,h3,h4,h5,h6';
const contextLengths = [8, 32];

function highlightBlock(node) {
    const el = node.nodeType === Node.TEXT_NODE ? node.parentElement : node;
    return el.closest && el.closest(highlightBlocks);
}

function canShowCommentIcon(range) {
    const article = document.getElementById('articleContent');
    return article && article.contains(range.startContainer) && article.contains(range.endContainer)
        && highlightBlock(range.startContainer) && !range.collapsed;
}

// Index DOM text in UTF-16 units, the same units used by Range offsets and String.slice.
function indexHighlightText(article) {
    const walker = document.createTreeWalker(article, NodeFilter.SHOW_TEXT);
    const nodes = [];
    let text = '';
    while (walker.nextNode()) {
        const node = walker.currentNode;
        const start = text.length;
        text += node.data;
        if (node.length) nodes.push({node, start, end: text.length});
    }
    return {text, nodes, fingerprint: highlightFingerprint(text)};
}

// Context signatures distinguish repeated quotes without embedding surrounding article text in comments.
function highlightFingerprint(text) {
    let hash = 2166136261;
    for (let i = 0; i < text.length; i++) {
        hash = Math.imul(hash ^ text.charCodeAt(i), 16777619);
    }
    return text.length + '-' + (hash >>> 0).toString(16);
}

function highlightContext(text, start, end) {
    return contextLengths.flatMap(size => [
        highlightFingerprint(text.slice(Math.max(0, start - size), start)),
        highlightFingerprint(text.slice(end, end + size))
    ]);
}

function rangeToElementJSON(range) {
    if (!range || !canShowCommentIcon(range)) return null;
    const article = document.getElementById('articleContent');
    const block = highlightBlock(range.startContainer);
    const before = range.cloneRange();
    before.selectNodeContents(article);
    before.setEnd(range.startContainer, range.startOffset);
    const textStart = before.toString().length;
    before.selectNodeContents(block);
    before.setEnd(range.startContainer, range.startOffset);
    const startOffset = before.toString().length;
    const selectedText = range.toString();
    const text = article.textContent;
    const tag = block.tagName.toLowerCase();
    return {
        elementTag: tag,
        elementIndex: Array.from(article.querySelectorAll(tag)).indexOf(block),
        startOffset, endOffset: startOffset + selectedText.length, selectedText,
        textStart, textFingerprint: highlightFingerprint(text),
        context: highlightContext(text, textStart, textStart + selectedText.length),
        repeated: text.indexOf(selectedText) !== text.lastIndexOf(selectedText)
    };
}

// Old anchors have no revision or context. Their tag index cannot distinguish equal quotes after edits.
function locateHighlight(anchor, index) {
    const quote = anchor.anchoredText || anchor.selectedText;
    if (typeof quote !== 'string' || !quote.trim()) return null;
    const {text, fingerprint} = index;
    if (anchor.textFingerprint === fingerprint && Number.isInteger(anchor.textStart)
        && anchor.textStart >= 0 && text.startsWith(quote, anchor.textStart)) {
        return {start: anchor.textStart, end: anchor.textStart + quote.length};
    }
    const first = text.indexOf(quote);
    if (first < 0) return null;
    if (!anchor.repeated && text.indexOf(quote, first + 1) < 0) {
        return {start: first, end: first + quote.length};
    }
    if (!Array.isArray(anchor.context)) return null;

    let best = null;
    let bestScore = 0;
    for (let start = first; start >= 0; start = text.indexOf(quote, start + 1)) {
        const end = start + quote.length;
        const context = highlightContext(text, start, end);
        let score = 0;
        context.forEach((value, i) => {
            // Ignore empty/very short boundaries (e.g. a single space).
            const matchedLength = parseInt(value, 10);
            if (value === anchor.context[i] && matchedLength >= 4) {
                score += matchedLength;
            }
        });
        if (score > bestScore) {
            best = {start, end};
            bestScore = score;
        } else if (score === bestScore) {
            best = null;
        }
    }
    return best;
}

// Copy limit for column pages (enabled via data attributes on <body>)
function setupCopyLimit() {
    const body = document.body;
    if (!body || !body.dataset || !body.dataset.copyLimit) return;

    const limit = Number(body.dataset.copyLimit);
    if (!Number.isFinite(limit) || limit <= 0) return;

    const scopeSelector = body.dataset.copyLimitScope || '#articleContent';
    const exemptSelector = body.dataset.copyLimitExempt || 'pre,code';
    const container = document.querySelector(scopeSelector);
    if (!container) return;

    document.addEventListener('copy', function (event) {
        const selection = window.getSelection();
        if (!selection || selection.isCollapsed) return;

        const anchorNode = selection.anchorNode;
        const focusNode = selection.focusNode;
        if (!anchorNode || !focusNode) return;

        if (!container.contains(anchorNode) || !container.contains(focusNode)) return;

        const anchorEl = anchorNode.nodeType === Node.ELEMENT_NODE ? anchorNode : anchorNode.parentElement;
        const focusEl = focusNode.nodeType === Node.ELEMENT_NODE ? focusNode : focusNode.parentElement;
        if (!anchorEl || !focusEl) return;

        const anchorExempt = anchorEl.closest(exemptSelector);
        const focusExempt = focusEl.closest(exemptSelector);
        if (anchorExempt && focusExempt && anchorExempt === focusExempt) return;

        const text = selection.toString();
        if (!text || text.length <= limit) return;

        const truncated = text.slice(0, limit);
        if (event && event.clipboardData) {
            event.clipboardData.setData('text/plain', truncated);
            event.preventDefault();
        } else if (navigator.clipboard && navigator.clipboard.writeText) {
            event.preventDefault();
            navigator.clipboard.writeText(truncated).catch(function () {});
        }

        if (typeof toastr !== 'undefined' && typeof toastr.info === 'function') {
            toastr.info('仅允许复制前 ' + limit + ' 字');
        }
    }, true);
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setupCopyLimit);
} else {
    setupCopyLimit();
}
