/* One document URL, one article identity. Selection lives only in this page. */
(function () {
  'use strict';
  let sequence = 0;
  let pending;
  let scope = 'current';
  const positions = new Map();
  const drafts = new Map();
  function readData(root) {
    const node = root.querySelector('#articleEditionData');
    return node ? { context: JSON.parse(node.dataset.context), highlights: JSON.parse(node.dataset.highlights || '[]') } : null;
  }
  function headers(context, discussionScope) {
    const c = context || window.articleReadingContext;
    if (!c || !c.versioned) return {};
    return { 'X-Article-Id': c.articleId, 'X-Article-Edition': c.selectedEditionId,
      'X-Article-Detail': c.selectedDetailId, 'X-Comment-Scope': discussionScope || scope };
  }
  window.articleEditionHeaders = headers;
  window.articleEditionCommentContext = function () {
    const c = window.articleReadingContext;
    return c && c.versioned ? { editionId: c.selectedEditionId, articleDetailId: c.selectedDetailId } : {};
  };
  // Every existing jQuery comment path (including paging, replies and relocation) gets the same scope.
  if (window.jQuery) $.ajaxPrefilter(function (options, original, xhr) {
    if (!options.url || !/^\/comment\/api\//.test(options.url)) return;
    Object.entries(headers()).forEach(([key, value]) => xhr.setRequestHeader(key, value));
    const started = sequence;
    const success = options.success;
    if (typeof success === 'function') options.success = function () {
      if (started === sequence) return success.apply(this, arguments);
    };
  });
  function scroller() {
    return window.matchMedia('(min-width: 992px)').matches ? document.querySelector('.article-content-wrap') : null;
  }
  function position() { const el = scroller(); return el ? el.scrollTop : window.scrollY; }
  function scrollTo(y) { const el = scroller(); if (el) el.scrollTop = y; else window.scrollTo(0, y); }
  function closeEditionMenu(restoreFocus = false) {
    const menu = document.getElementById('articleEditionMenu');
    const trigger = document.getElementById('articleEditionTrigger');
    if (menu) menu.hidden = true;
    if (trigger) {
      trigger.setAttribute('aria-expanded', 'false');
      if (restoreFocus) trigger.focus({ preventScroll: true });
    }
  }
  function openEditionMenu() {
    const menu = document.getElementById('articleEditionMenu');
    if (!menu) return;
    menu.hidden = false;
    const bounds = document.getElementById('articleEditionControls').getBoundingClientRect();
    const scrollBounds = scroller()?.getBoundingClientRect();
    const below = Math.max(0, Math.min(window.innerHeight, scrollBounds?.bottom || window.innerHeight) - bounds.bottom - 14);
    const above = Math.max(0, bounds.top - Math.max(68, scrollBounds?.top || 0) - 6);
    const flip = below < Math.min(menu.scrollHeight, 240) && above > below;
    menu.dataset.placement = flip ? 'above' : 'below';
    menu.style.maxHeight = Math.min(360, flip ? above : below) + 'px';
    document.getElementById('articleEditionTrigger').setAttribute('aria-expanded', 'true');
    (menu.querySelector('[aria-checked="true"]') || menu.querySelector('button'))?.focus({ preventScroll: true });
  }
  function decorateComments() {
    const c = window.articleReadingContext;
    if (!c || !c.versioned) return;
    document.querySelectorAll('[data-comment-edition]').forEach(node => {
      const id = node.dataset.commentEdition;
      const edition = c.editions.find(e => String(e.id) === id);
      node.textContent = edition ? edition.editionCode + ' · ' + edition.editionName : '早期讨论';
      node.hidden = scope === 'current' && !!edition;
      if (edition && id !== c.selectedEditionId) {
        node.dataset.switchEdition = id;
        node.title = '阅读这条讨论对应的正文';
      } else delete node.dataset.switchEdition;
    });
    const tabs = document.querySelector('#editionDiscussionScope');
    if (tabs) tabs.querySelectorAll('[data-discussion-scope]').forEach(button => {
      const active = button.dataset.discussionScope === scope;
      button.classList.toggle('active', active); button.setAttribute('aria-pressed', String(active));
      const count = button.querySelector('span');
      if (count) count.textContent = button.dataset.discussionScope === 'current' ? c.currentCommentCount : c.historyCommentCount;
    });
  }
  function adoptDiscussionState(tabs) {
    const c = window.articleReadingContext;
    if (!tabs || !c?.versioned) return;
    scope = tabs.dataset.scope === 'history' ? 'history' : 'current';
    c.discussionScope = scope;
    c.currentCommentCount = Number(tabs.dataset.currentCount || 0);
    c.historyCommentCount = Number(tabs.dataset.historyCount || 0);
  }
  function mountComments(doc, commentsOnly) {
    const incoming = doc.querySelector('#commentDiv');
    if (!incoming) throw new Error('未获取到评论区');
    incoming.querySelectorAll('script').forEach(node => node.remove());
    if (commentsOnly) {
      const list = incoming.querySelector('#editionCommentResults');
      const tabs = incoming.querySelector('#editionDiscussionScope');
      if (!list || !tabs) throw new Error('未获取到评论列表');
      document.getElementById('editionCommentResults').replaceWith(list);
      Object.assign(document.getElementById('editionDiscussionScope').dataset, tabs.dataset);
    } else {
      document.getElementById('commentDiv').innerHTML = incoming.innerHTML;
      if (window.bindEditionCommentInput) window.bindEditionCommentInput();
    }
    adoptDiscussionState(document.getElementById('editionDiscussionScope'));
    if (window.autoExpandSingleReplyWraps) window.autoExpandSingleReplyWraps(document.getElementById('commentDiv'));
    if (window.renderCommentMarkdown) window.renderCommentMarkdown();
    decorateComments();
  }
  async function switchEdition(id, nextScope = 'current', commentsOnly = false) {
    const old = window.articleReadingContext;
    if (!old || !old.versioned) return false;
    const edition = old.editions.find(e => String(e.id) === String(id));
    if (!edition) { toastr.error('这个版本尚未公开或已撤回'); return false; }
    const seq = ++sequence;
    if (pending) pending.abort();
    pending = new AbortController();
    const selected = { ...old, selectedEditionId: String(id), selectedDetailId: edition.publishedDetailId };
    ['articleEditionControls', 'editionDiscussionScope'].forEach(id => document.getElementById(id)?.removeAttribute('aria-busy'));
    const busyTarget = commentsOnly ? 'editionDiscussionScope' : 'articleEditionControls';
    document.getElementById(busyTarget)?.setAttribute('aria-busy', 'true');
    try {
      // Reuse the SAME SSR controller, including all visibility, payment and preview rules.
      // We parse only inert fragments; no fetched script is executed and no history API is used.
      const response = await fetch(window.location.pathname + window.location.search, {
        credentials: 'same-origin', cache: 'no-store', signal: pending.signal,
        headers: { ...headers(selected, nextScope), 'X-Article-Switch': '1' }
      });
      if (!response.ok) throw new Error('版本暂时无法加载');
      const doc = new DOMParser().parseFromString(await response.text(), 'text/html');
      const data = readData(doc);
      if (seq !== sequence) return false;
      if (!data || data.context.articleId !== old.articleId || String(data.context.selectedEditionId) !== String(id)
          || !doc.getElementById('articleContent') || !doc.getElementById('commentDiv')) throw new Error('版本已变化，请刷新后重试');
      if (!commentsOnly) {
        positions.set(old.selectedEditionId + ":" + old.selectedDetailId, position());
        drafts.set(old.selectedEditionId, document.getElementById('commentContent')?.value || '');
        if (typeof saveArticleScrollProgress === 'function') saveArticleScrollProgress();
        if (typeof hideQuoteCommentSidebar === 'function') hideQuoteCommentSidebar();
        if (typeof invalidateHighlightAiRequest === 'function') invalidateHighlightAiRequest('edition-switch');
        if (window.aiCommentBot?.cancel) window.aiCommentBot.cancel();
        if (typeof resetEditionHighlights === 'function') resetEditionHighlights();
        document.getElementById('articleContent').innerHTML = doc.getElementById('articleContent').innerHTML;
        document.getElementById('postsTitle').textContent = doc.getElementById('postsTitle').textContent;
        document.getElementById('articleEditionControls').replaceWith(doc.getElementById('articleEditionControls'));
        document.getElementById('articleEditionData').replaceWith(doc.getElementById('articleEditionData'));
        document.title = doc.title;
      }
      window.articleReadingContext = commentsOnly ? { ...data.context, selectedDetailId: old.selectedDetailId } : data.context;
      if (commentsOnly && window.aiCommentBot?.cancel) window.aiCommentBot.cancel();
      scope = nextScope;
      mountComments(doc, commentsOnly);
      if (!commentsOnly) {
        const input = document.getElementById('commentContent');
        if (input) { input.value = drafts.get(String(id)) || ''; input.dispatchEvent(new Event('input', { bubbles: true })); }
        document.querySelectorAll('#articleContent pre code').forEach(node => { if (window.hljs) window.hljs.highlightElement(node); });
        if (typeof initUnderlineToSelection === 'function') initUnderlineToSelection(data.highlights, typeof highlightViewerId === 'undefined' ? null : highlightViewerId);
        if (typeof generateTocMenu === 'function') generateTocMenu();
        else if (typeof genTocMenu === 'function') genTocMenu('#articleContent', '#contentMenu');
        if (typeof initArticleImages === 'function') initArticleImages();
        requestAnimationFrame(() => {
          if (typeof resetEditionScrollProgress === 'function') resetEditionScrollProgress();
          scrollTo(positions.get(String(id) + ":" + data.context.selectedDetailId) || 0);
        });
      }
      return true;
    } catch (error) {
      if (error.name !== 'AbortError' && seq === sequence) toastr.error(error.message || '切换失败，当前正文已保留');
      return false;
    } finally {
      if (seq === sequence) {
        document.getElementById(busyTarget)?.removeAttribute('aria-busy');
      }
    }
  }
  window.switchArticleEdition = switchEdition;
  document.addEventListener('click', event => {
    const trigger = event.target.closest('#articleEditionTrigger');
    if (trigger) {
      if (trigger.getAttribute('aria-expanded') === 'true') closeEditionMenu();
      else openEditionMenu();
      return;
    }
    const choice = event.target.closest('[data-edition-choice]');
    if (choice) {
      const id = choice.dataset.editionChoice;
      closeEditionMenu(true);
      if (id !== window.articleReadingContext.selectedEditionId || document.getElementById('articleEditionControls')?.hasAttribute('aria-busy')) {
        switchEdition(id).then(() => document.getElementById('articleEditionTrigger')?.focus({ preventScroll: true }));
      }
      return;
    }
    if (!event.target.closest('#articleEditionControls')) closeEditionMenu();
    const edition = event.target.closest('[data-switch-edition]');
    if (edition) { event.preventDefault(); switchEdition(edition.dataset.switchEdition).then(ok => { if (ok && edition.dataset.thread) void focusComment(edition.dataset.thread); }); }
    const tab = event.target.closest('[data-discussion-scope]');
    if (tab) { event.preventDefault(); if (tab.dataset.discussionScope !== scope || document.getElementById('editionDiscussionScope')?.hasAttribute('aria-busy')) void switchEdition(window.articleReadingContext.selectedEditionId, tab.dataset.discussionScope, true); }
  });
  document.addEventListener('keydown', event => {
    const menu = document.getElementById('articleEditionMenu');
    if (!menu) return;
    if (event.key === 'Escape' && !menu.hidden) { event.preventDefault(); closeEditionMenu(true); return; }
    if (!event.target.closest('#articleEditionControls')) return;
    if (event.target.id === 'articleEditionTrigger' && ['ArrowDown', 'ArrowUp'].includes(event.key)) {
      event.preventDefault(); openEditionMenu(); return;
    }
    if (menu.hidden || !['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) return;
    event.preventDefault();
    const options = Array.from(menu.querySelectorAll('[data-edition-choice]'));
    const current = options.indexOf(document.activeElement);
    const next = event.key === 'Home' ? 0 : event.key === 'End' ? options.length - 1
      : (current + (event.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length;
    options[next]?.focus();
  });
  document.addEventListener('focusin', event => {
    if (!event.target.closest('#articleEditionControls')) closeEditionMenu();
  });
  window.addEventListener('resize', () => closeEditionMenu());
  async function focusComment(id) {
    const initial = sequence;
    try {
      const response = await fetch('/comment/api/context?commentId=' + encodeURIComponent(id), { credentials: 'same-origin', headers: headers() });
      const envelope = await response.json();
      if (initial !== sequence || envelope.status?.code !== 0) return;
      const target = envelope.result;
      const c = window.articleReadingContext;
      if (String(target.articleId) !== c.articleId) return;
      if (target.editionId && target.editionId !== c.selectedEditionId) {
        if (!await switchEdition(target.editionId)) return;
      } else if (!target.editionId && scope !== 'history') {
        if (!await switchEdition(c.selectedEditionId, 'history', true)) return;
      }
      const container = document.querySelector('#commentList .all-comment-items');
      if (!container) return;
      document.getElementById('comment-' + target.rootCommentId)?.closest('.comment-list-wrap')?.remove();
      const fragment = new DOMParser().parseFromString(target.html, 'text/html');
      fragment.querySelectorAll('script').forEach(node => node.remove());
      container.prepend(...Array.from(fragment.body.children));
      const root = document.getElementById('comment-' + target.rootCommentId);
      const block = root?.closest('.comment-list-wrap');
      if (block) block.querySelectorAll('.sub-comments-container').forEach(node => { node.style.display = 'block'; });
      decorateComments();
      if (window.renderCommentMarkdown) window.renderCommentMarkdown();
      const node = block?.querySelector('[data-comment-id="' + id + '"]') || root;
      node?.scrollIntoView({ block: 'center', behavior: 'smooth' });
      if (node?.animate) node.animate([{ background: '#fff0d4' }, { background: 'transparent' }], { duration: 1800 });
    } catch (error) { toastr.info('这条讨论暂时无法定位'); }
  }
  document.addEventListener('DOMContentLoaded', () => {
    const data = readData(document);
    if (!data) return;
    window.articleReadingContext = data.context;
    adoptDiscussionState(document.getElementById('editionDiscussionScope'));
    decorateComments();
    const anchor = window.location.hash.match(/^#comment-(\d+)$/);
    if (data.context.versioned && anchor) void focusComment(anchor[1]);
    const comments = document.getElementById('commentDiv');
    if (comments) new MutationObserver(() => {
      const labels = comments.querySelectorAll('[data-comment-edition]:not([data-edition-decorated])');
      if (!labels.length) return;
      labels.forEach(node => { node.dataset.editionDecorated = 'true'; });
      adoptDiscussionState(document.getElementById('editionDiscussionScope'));
      decorateComments();
    }).observe(comments, { childList: true, subtree: true });
  });
})();
