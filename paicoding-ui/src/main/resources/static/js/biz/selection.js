const ALLOWED = ['P','LI','UL','IMG','H1','H2','H3','H4','H5','H6'];

function canShowCommentIcon(range) {
    let startNode = range.startContainer;
    let startEl = (startNode.nodeType === Node.TEXT_NODE) ? startNode.parentElement : startNode;
    if (startEl && ALLOWED.includes(startEl.tagName)) {
        return true;
    }
    return false;
}


// 序列化：以同类型标签的顺序计数 elementIndex
function rangeToElementJSON(range) {
    const container = document.getElementById('articleContent');
    if (!container) throw new Error('#articleContent not found');


    // 从 startContainer 向上找第一个符合的元素
    let startNode = range.startContainer;
    let startEl = (startNode.nodeType === Node.TEXT_NODE) ? startNode.parentElement : startNode;
    while (startEl && (!startEl.tagName || !ALLOWED.includes(startEl.tagName))) {
        startEl = startEl.parentElement;
    }
    if (!startEl) throw new Error('Selection start is not inside a supported element');

    const tag = startEl.tagName.toLowerCase(); // 存小写，方便序列化 / querySelectorAll
    // 只在同类型标签集合中计数（更稳定）
    const sameTagElements = Array.from(container.querySelectorAll(tag));
    const elementIndex = sameTagElements.indexOf(startEl);
    if (elementIndex === -1) throw new Error('Element not found inside #articleContent');

    // 计算在该元素内的偏移
    const preRange = range.cloneRange();
    preRange.selectNodeContents(startEl);
    preRange.setEnd(range.startContainer, range.startOffset);
    const startOffset = preRange.toString().length;

    const selectedText = range.toString();
    const endOffset = startOffset + selectedText.length;

    return {
        elementTag: tag,
        elementIndex,
        startOffset,
        endOffset,
        selectedText,
    };
}


// 反序列化：基于 elementTag + elementIndex + offsets 恢复 Range（包含容错）
function elementJSONToRange(json) {
    const container = document.getElementById('articleContent');
    if (!container) {
        console.warn('#articleContent not found');
        return null;
    }

    const selector = json.elementTag; // 比如 'p'
    const elements = Array.from(container.querySelectorAll(selector));
    const el = elements[json.elementIndex];
    if (!el) {
        console.warn(`Element index not found for <${selector}>`);
        return null;
    }

    // 收集该元素内所有文本节点（按文档顺序）
    const textNodes = [];
    (function collectTextNodes(node) {
        if (node.nodeType === Node.TEXT_NODE) {
            textNodes.push(node);
        } else {
            for (const child of node.childNodes) collectTextNodes(child);
        }
    })(el);

    const range = document.createRange();
    let current = 0;
    let startSet = false;
    let endSet = false;

    for (const node of textNodes) {
        const next = current + node.textContent.length;

        // 设置起点（包括等于 current 的情况）
        if (!startSet && json.startOffset >= current && json.startOffset <= next) {
            range.setStart(node, Math.max(0, json.startOffset - current));
            startSet = true;
        }

        // 设置终点（包括等于 next 的情况）
        if (!endSet && json.endOffset >= current && json.endOffset <= next) {
            range.setEnd(node, Math.max(0, json.endOffset - current));
            endSet = true;
            break;
        }

        current = next;
    }

    // 防御性补救：如果没有找到 start/end，回退到元素内容
    if (!startSet || !endSet) {
        console.warn('Offsets mismatch within element, falling back to selectNodeContents');
        try {
            range.selectNodeContents(el);
        } catch (e) {
            // 最后兜底：如果是 img（无文本节点）
            if (el.tagName === 'IMG') {
                range.selectNode(el);
            } else {
                return null;
            }
        }
        return range;
    }

    // 确保选区不会跨出 el（surroundContents 要求 common ancestor 在同一容器内）
    const commonAncestor = range.commonAncestorContainer;
    if (!el.contains(commonAncestor)) {
        console.warn('Range crosses element boundaries, normalizing to element contents');
        range.selectNodeContents(el);
    }

    // 特殊：若元素本身无文本（如 img），选中整个元素
    if (textNodes.length === 0 && el.tagName === 'IMG') {
        range.selectNode(el);
    }

    return range;
}
