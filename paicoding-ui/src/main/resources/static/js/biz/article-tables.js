(function () {
  'use strict';

  const node = (tag, text, cls) => {
    const el = document.createElement(tag);
    if (text != null) el.textContent = text;
    if (cls) el.className = cls;
    return el;
  };
  const button = (text, fn, cls) => {
    const b = node('button', text, cls);
    b.type = 'button';
    b.onclick = fn;
    return b;
  };

  const scriptEl = document.getElementById('paiArticleTablesScript') || document.currentScript;
  const ossBase = (scriptEl?.dataset?.oss || '').replace(/\/+$/, '');
  const assetUrl = path => ossBase + path;

  let tabulatorPromise = null;
  function ensureTabulator() {
    if (window.Tabulator) return Promise.resolve(window.Tabulator);
    if (tabulatorPromise) return tabulatorPromise;
    tabulatorPromise = new Promise((resolve, reject) => {
      if (!document.getElementById('paiTabulatorCss')) {
        const link = document.createElement('link');
        link.id = 'paiTabulatorCss';
        link.rel = 'stylesheet';
        link.href = assetUrl('/vendor/tabulator/6.3.1/tabulator.min.css');
        document.head.appendChild(link);
      }
      const script = document.createElement('script');
      script.src = assetUrl('/vendor/tabulator/6.3.1/tabulator.min.js');
      script.onload = () => window.Tabulator ? resolve(window.Tabulator) : reject(new Error('Tabulator unavailable'));
      script.onerror = () => reject(new Error('Failed to load Tabulator'));
      document.head.appendChild(script);
    });
    return tabulatorPromise;
  }

  let capabilitiesPromise = null;
  function getCapabilities() {
    if (!capabilitiesPromise) {
      capabilitiesPromise = fetch('/api/tables/capabilities', { credentials: 'same-origin' })
        .then(r => r.ok ? r.json() : null)
        .then(r => ({ ai: Boolean(r?.result?.ai), loggedIn: Boolean(r?.result?.loggedIn) }))
        .catch(() => ({ ai: false, loggedIn: false }));
    }
    return capabilitiesPromise;
  }

  function promptLoginIfAvailable() {
    if (window.jQuery && document.getElementById('loginModal')) {
      window.jQuery('#loginModal').modal('show');
      return true;
    }
    return false;
  }

  // Keep article formatting and links without copying executable markup, broken null URLs, or duplicate IDs.
  function rich(source) {
    const box = node('div');
    function append(from, to) {
      from.childNodes.forEach(child => {
        if (child.nodeType === 3) {
          to.append(document.createTextNode(child.textContent));
          return;
        }
        if (child.nodeType !== 1 || /^(SCRIPT|STYLE|IFRAME|OBJECT|INPUT|BUTTON)$/.test(child.tagName)) return;
        const tag = child.tagName.toLowerCase();
        if (tag === 'a') {
          const rawHref = child.getAttribute('href');
          if (!rawHref || !rawHref.trim()) { append(child, to); return; }
          if (rawHref.trim().startsWith('#')) {
            const a = node('a');
            a.href = rawHref.trim();
            append(child, a);
            to.append(a);
            return;
          }
          let url;
          try { url = new URL(rawHref, location.href); } catch (e) { append(child, to); return; }
          if (!['http:', 'https:', 'mailto:'].includes(url.protocol)) { append(child, to); return; }
          const a = node('a');
          a.href = url.href;
          a.target = '_blank';
          a.rel = 'noopener noreferrer';
          append(child, a);
          to.append(a);
        } else if (tag === 'img') {
          const rawSrc = child.getAttribute('src');
          if (!rawSrc || !rawSrc.trim()) return;
          let url;
          try { url = new URL(rawSrc, location.href); } catch (e) { return; }
          if (!['http:', 'https:'].includes(url.protocol)) return;
          const img = node('img');
          img.src = url.href;
          img.alt = child.alt || '';
          img.loading = 'lazy';
          to.append(img);
        } else if (/^(br|p|strong|b|em|i|code|del|ul|ol|li|span)$/.test(tag)) {
          const el = node(tag);
          append(child, el);
          to.append(el);
        } else {
          append(child, to);
        }
      });
    }
    append(source, box);
    return box;
  }

  // Pure DOM Markdown renderer for AI answers: supports fenced code blocks, inline code, bold, and lists without innerHTML.
  function renderSafeMarkdown(container, text) {
    container.replaceChildren();
    const raw = String(text || '').replace(/\r\n/g, '\n');
    const parts = raw.split(/```([a-zA-Z0-9_-]*)\n?([\s\S]*?)```/g);
    for (let idx = 0; idx < parts.length; idx += 3) {
      const prose = parts[idx];
      if (prose && prose.trim()) {
        prose.trim().split(/\n{2,}/).forEach(block => {
          const lines = block.split('\n').filter(l => l.trim().length > 0);
          if (!lines.length) return;
          const isOrdered = lines.length > 1 && lines.every(l => /^\d+[.、)]\s*/.test(l.trim()));
          const isUnordered = lines.length > 1 && lines.every(l => /^[-*•]\s+/.test(l.trim()));
          if (isOrdered || isUnordered) {
            const list = node(isOrdered ? 'ol' : 'ul', null, 'pai-md-list');
            lines.forEach(line => {
              const li = node('li');
              const cleaned = isOrdered ? line.trim().replace(/^\d+[.、)]\s*/, '') : line.trim().replace(/^[-*•]\s+/, '');
              appendInlineMarkdown(li, cleaned);
              list.append(li);
            });
            container.append(list);
          } else {
            const p = node('p', null, 'pai-md-p');
            lines.forEach((line, lineIdx) => {
              if (lineIdx > 0) p.append(node('br'));
              appendInlineMarkdown(p, line);
            });
            container.append(p);
          }
        });
      }
      if (idx + 2 < parts.length) {
        const lang = parts[idx + 1] || '';
        const codeText = (parts[idx + 2] || '').replace(/\n$/, '');
        const pre = node('pre', null, 'pai-md-pre');
        const code = node('code', codeText, lang ? 'language-' + lang : '');
        pre.append(code);
        if (window.hljs && typeof window.hljs.highlightElement === 'function') {
          try { window.hljs.highlightElement(code); } catch (e) { /* keep plain code */ }
        }
        container.append(pre);
      }
    }
  }

  function appendInlineMarkdown(target, text) {
    const tokenRe = /(`[^`]+`|\*\*[^*]+\*\*)/g;
    let last = 0;
    let match;
    while ((match = tokenRe.exec(text)) !== null) {
      if (match.index > last) {
        target.append(document.createTextNode(text.slice(last, match.index)));
      }
      const token = match[0];
      if (token.startsWith('`')) {
        target.append(node('code', token.slice(1, -1), 'pai-md-code'));
      } else if (token.startsWith('**')) {
        target.append(node('strong', token.slice(2, -2)));
      }
      last = tokenRe.lastIndex;
    }
    if (last < text.length) {
      target.append(document.createTextNode(text.slice(last)));
    }
  }

  function storageKey(index) {
    const articleId = document.getElementById('postsTitle')?.dataset?.id
      || window.articleReadingContext?.articleId
      || location.pathname;
    const edition = window.articleReadingContext?.selectedEditionId || 'default';
    return 'paicoding:table:v2:' + articleId + ':' + edition + ':' + index;
  }

  function loadPersistedState(key) {
    try {
      const raw = localStorage.getItem(key);
      if (!raw) return { done: [], hidden: [], group: '', widths: {}, undoneOnly: false };
      const parsed = JSON.parse(raw);
      return {
        done: Array.isArray(parsed.done) ? parsed.done : [],
        hidden: Array.isArray(parsed.hidden) ? parsed.hidden : [],
        group: typeof parsed.group === 'string' ? parsed.group : '',
        widths: parsed.widths && typeof parsed.widths === 'object' ? parsed.widths : {},
        undoneOnly: Boolean(parsed.undoneOnly)
      };
    } catch (e) {
      return { done: [], hidden: [], group: '', widths: {}, undoneOnly: false };
    }
  }

  function savePersistedState(key, state) {
    try {
      localStorage.setItem(key, JSON.stringify(state));
    } catch (e) { /* ignore quota / private mode */ }
  }

  let activeWideClose = null;
  const instances = [];

  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && activeWideClose) {
      event.preventDefault();
      activeWideClose();
    }
  });

  function extractCellText(cell) {
    if (!cell) return '';
    const txt = (cell.textContent || '').replace(/\s+/g, ' ').trim();
    if (txt) return txt;
    const imgAlt = Array.from(cell.querySelectorAll('img')).map(img => img.alt).filter(Boolean).join(' ');
    return imgAlt.trim();
  }

  function scrollReaderAtGridEdge(event) {
    if (event.defaultPrevented || !event.cancelable || event.ctrlKey || event.metaKey || event.shiftKey
        || Math.abs(event.deltaY) <= Math.abs(event.deltaX)) return;

    const holder = event.target.closest('.tabulator-tableholder');
    if (!holder) return;
    const scrollingUp = event.deltaY < 0;
    const canScroll = element => scrollingUp
      ? element.scrollTop > 0
      : element.scrollTop + element.clientHeight < element.scrollHeight - 1;
    if (canScroll(holder)) return;

    const chapter = holder.closest('.article-content-wrap');
    const reader = chapter && /^(auto|scroll)$/.test(getComputedStyle(chapter).overflowY)
      ? chapter : document.scrollingElement;
    if (!reader || !canScroll(reader)) return;

    const mode = event.deltaMode;
    let distance = event.deltaY;
    if (mode === WheelEvent.DOM_DELTA_LINE) distance *= parseFloat(getComputedStyle(holder).lineHeight) || 16;
    if (mode === WheelEvent.DOM_DELTA_PAGE) distance *= reader.clientHeight;
    event.preventDefault();
    reader.scrollTop += distance;
  }

  function wrapCompactTable(table) {
    const wrapper = node('div', null, 'pai-article-table-compact');
    table.before(wrapper);
    wrapper.append(table);
  }

  function enhanceInteractive(table, index, usedTitles, rows, notes) {
    const columns = Array.from(rows[0].cells).map((c, i) => extractCellText(c) || ('列 ' + (i + 1)));
    const article = table.closest('#articleContent');
    const headings = Array.from(article.querySelectorAll('h1,h2,h3,h4,h5,h6'))
      .filter(h => !h.closest('.pai-article-ui') && (h.compareDocumentPosition(table) & Node.DOCUMENT_POSITION_FOLLOWING));
    const nearestHeading = headings.length ? headings[headings.length - 1] : null;
    const baseTitle = nearestHeading ? nearestHeading.textContent.trim() : ('表格 ' + (index + 1));
    const seenCount = (usedTitles.get(baseTitle) || 0) + 1;
    usedTitles.set(baseTitle, seenCount);
    const title = seenCount > 1 ? (baseTitle + '（' + seenCount + '）') : baseTitle;
    const adjacentHeading = nearestHeading && table.previousElementSibling === nearestHeading && seenCount === 1;
    const exportName = ((document.getElementById('postsTitle')?.textContent.trim() || document.title.split(' - ')[0]) + ' · ' + title).slice(0, 200);

    const persistKey = storageKey(index);
    const persisted = loadPersistedState(persistKey);
    const doneSet = new Set(persisted.done);

    const wrapper = node('section', null, 'pai-article-table');
    wrapper.setAttribute('aria-label', title + '交互表格');
    if (adjacentHeading) wrapper.classList.add('is-adjacent-heading');

    const ui = node('div', null, 'pai-article-ui');
    table.before(wrapper);
    table.classList.add('pai-source-table');
    table.hidden = true;
    wrapper.append(table, ui);

    const data = rows.slice(1).map((r, i) => {
      const result = { _id: i + 1 };
      columns.forEach((c, j) => { result['c' + j] = extractCellText(r.cells[j]); });
      return result;
    });
    const cells = rows.slice(1).map(r => columns.map((c, i) => r.cells[i] ? rich(r.cells[i]) : node('div')));
    const M = window.PaiTableModel;
    const fields = columns.map(c => M.field(c, 'longtext'));
    const documentData = {
      name: exportName,
      fields,
      records: data.map(r => ({ id: M.uid('r'), values: Object.fromEntries(fields.map((f, i) => [f.id, r['c' + i]])) }))
    };

    const normalHeight = Math.min(480, Math.max(180, data.length * 64 + 54));
    let grid = null;
    let isWide = false;
    let activeStreamAbort = null;

    function syncProgressBadge() {
      const doneCount = data.filter(r => doneSet.has(r._id)).length;
      progressText.textContent = '已完成 ' + doneCount + ' / ' + data.length;
      progressFill.style.width = (data.length ? Math.round((doneCount / data.length) * 100) : 0) + '%';
      progressBadge.classList.toggle('is-complete', doneCount > 0 && doneCount === data.length);
    }

    function persistCurrent() {
      savePersistedState(persistKey, {
        done: Array.from(doneSet),
        hidden: persisted.hidden,
        group: group.value || '',
        widths: persisted.widths,
        undoneOnly: persisted.undoneOnly
      });
    }

    function setWide(on) {
      if (on === isWide) return;
      if (on && activeWideClose && activeWideClose !== closeWide) activeWideClose();
      isWide = on;
      activeWideClose = on ? closeWide : null;
      document.body.classList.toggle('pai-table-wide', on);
      wrapper.classList.toggle('is-expanded', on);
      expandBtn.textContent = on ? '收起' : '展开';
      expandBtn.setAttribute('aria-expanded', String(on));
      requestAnimationFrame(() => {
        if (grid) {
          grid.setHeight(on ? 'min(68vh, ' + (data.length * 64 + 54) + 'px)' : normalHeight);
          grid.redraw(true);
          if (on === isWide) wrapper.scrollIntoView({ block: 'start' });
        }
      });
    }
    const closeWide = () => setWide(false);

    // Top Header Bar
    const top = node('div', null, 'pai-article-table-top');
    const titleEl = node('strong', title, 'pai-table-title');
    const progressBadge = node('div', null, 'pai-table-progress');
    const progressText = node('span', '');
    const progressTrack = node('span', null, 'pai-table-progress-track');
    const progressFill = node('span', null, 'pai-table-progress-fill');
    progressTrack.append(progressFill);
    progressBadge.append(progressText, progressTrack);
    const expandBtn = button('展开', () => setWide(!isWide), 'pai-btn-ghost');
    expandBtn.setAttribute('aria-expanded', 'false');
    top.append(titleEl, progressBadge, expandBtn);
    ui.append(top);
    syncProgressBadge();

    // Tools Bar
    const tools = node('div', null, 'pai-article-table-tools');
    const search = node('input');
    search.type = 'search';
    search.placeholder = '搜索表格内容…';
    search.setAttribute('aria-label', '搜索' + title);

    const undoneToggle = button('只看未完成', () => {
      viewMutationSeq++;
      persisted.undoneOnly = !persisted.undoneOnly;
      undoneToggle.classList.toggle('is-active', persisted.undoneOnly);
      undoneToggle.setAttribute('aria-pressed', String(persisted.undoneOnly));
      persistCurrent();
      applyFilters();
    }, 'pai-btn-chip');
    undoneToggle.classList.toggle('is-active', persisted.undoneOnly);
    undoneToggle.setAttribute('aria-pressed', String(persisted.undoneOnly));

    const resetAgentViewBtn = button('恢复全部视图', () => {
      viewMutationSeq++;
      agentRowOrder = null;
      grid.clearSort();
      resetAgentViewBtn.hidden = true;
      persisted.hidden = [];
      columns.forEach((_, idx) => { const col = grid?.getColumn('c' + idx); if (col) col.show(); });
      Array.from(columnPanel.querySelectorAll('input[type="checkbox"]')).forEach(chk => { chk.checked = true; });
      search.value = '';
      persistCurrent();
      applyFilters();
    }, 'pai-btn-chip is-active');
    resetAgentViewBtn.hidden = true;

    const group = node('select');
    group.setAttribute('aria-label', '按列分组');
    group.append(new Option('按列分组：不分组', ''));
    columns.forEach((c, i) => group.append(new Option('分组：' + c, 'c' + i)));
    if (persisted.group && columns[Number(persisted.group.slice(1))] != null) {
      group.value = persisted.group;
    }

    const columnPanel = node('div', null, 'pai-table-columns');
    columnPanel.hidden = true;
    const columnToggle = button('显示列', () => {
      columnPanel.hidden = !columnPanel.hidden;
      columnToggle.setAttribute('aria-expanded', String(!columnPanel.hidden));
    });
    columnToggle.setAttribute('aria-expanded', 'false');

    const aiButton = button('表格智能体', () => openPanel(selected || data[0], 'ai'), 'pai-btn-ai');
    aiButton.hidden = true;
    let capabilitiesState = { ai: false, loggedIn: false };
    getCapabilities().then(cap => {
      capabilitiesState = cap;
      aiButton.hidden = !cap.ai || !data.length;
      if (!panel.hidden) paintPanel();
    });

    tools.append(search, undoneToggle, resetAgentViewBtn, group, columnToggle, aiButton);
    ui.append(tools, columnPanel);

    // Body: Grid + Side/Bottom Detail & AI Panel
    const body = node('div', null, 'pai-table-body');
    const area = node('div', null, 'pai-table-grid-area');
    const host = node('div', null, 'pai-article-grid');
    host.addEventListener('wheel', scrollReaderAtGridEdge, { passive: false });
    const panel = node('aside', null, 'pai-table-panel');
    panel.hidden = true;
    panel.setAttribute('aria-label', '行详情与 表格智能体');
    area.append(host);
    body.append(area, panel);
    ui.append(body);

    // Footer
    const footer = node('footer');
    const count = node('span', '共 ' + data.length + ' 条记录');
    const exportButton = button('导出 CSV', () => downloadCsvForSpec(null), 'pai-btn-export');
    footer.append(count, exportButton);
    ui.append(footer);
    for (const cell of notes) {
      const note = rich(cell);
      note.className = 'pai-table-note';
      ui.append(note);
    }

    let selected = null;
    let mode = 'detail';
    let currentPresetAction = 'explain';
    const actionCache = new Map(); // key: `${rowId}:${action}` -> { pending, error, text }
    const qaHistory = new Map();   // key: rowId -> Array<{ question, pending, error, text, steps, exportSpec }>
    const drafts = new Map();
    let agentRowOrder = null;
    let viewMutationSeq = 0;

    function visibleColumnIndices() {
      return grid.getColumns().filter(col => col.isVisible() && col.getField() !== '_id')
        .map(col => Number(col.getField().slice(1)));
    }

    function visibleRowIds() {
      return grid.getRows('active').map(row => row.getData()._id);
    }

    function downloadCsvForSpec(spec) {
      const colIndices = spec?.columns || visibleColumnIndices();
      const exportFields = colIndices.map(idx => documentData.fields[idx]).filter(Boolean);
      const rowIds = spec?.rowIds || visibleRowIds();
      const exportRecords = rowIds.map(id => documentData.records[id - 1]).filter(Boolean);
      const blob = new Blob([M.csv(documentData, exportRecords, exportFields)], { type: 'text/csv;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const a = node('a');
      a.href = url;
      a.download = M.safeFilename(exportName) + '.csv';
      a.click();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
    }

    function applyAgentView(viewSpec) {
      if (!grid || !viewSpec) return;
      if (Array.isArray(viewSpec.columns) && viewSpec.columns.length) {
        const visibleSet = new Set(viewSpec.columns);
        persisted.hidden = columns.map((_, idx) => idx).filter(idx => !visibleSet.has(idx));
        columns.forEach((_, idx) => {
          const col = grid.getColumn('c' + idx);
          if (col) {
            if (visibleSet.has(idx)) col.show();
            else col.hide();
          }
        });
        Array.from(columnPanel.querySelectorAll('input[type="checkbox"]')).forEach((chk, idx) => {
          chk.checked = visibleSet.has(idx);
        });
      }
      if (Array.isArray(viewSpec.rowIds)) {
        agentRowOrder = new Map(viewSpec.rowIds.map((id, index) => [id, index]));
        grid.setSort('_id', 'asc');
        applyFilters();
      }
      resetAgentViewBtn.hidden = false;
      persistCurrent();
    }

    function applyRowVisualState(rowComponent) {
      const rowData = rowComponent.getData();
      if (!rowData) return;
      const el = rowComponent.getElement();
      el.classList.toggle('pai-selected', Boolean(selected && rowData._id === selected._id && !panel.hidden));
      el.classList.toggle('is-done', doneSet.has(rowData._id));
      const chk = el.querySelector('.pai-row-check');
      if (chk) chk.checked = doneSet.has(rowData._id);
    }

    function refreshAllRowStates() {
      if (!grid) return;
      grid.getRows().forEach(applyRowVisualState);
    }

    function toggleDone(rowId) {
      if (doneSet.has(rowId)) doneSet.delete(rowId);
      else doneSet.add(rowId);
      syncProgressBadge();
      persistCurrent();
      refreshAllRowStates();
      if (persisted.undoneOnly) applyFilters();
      if (!panel.hidden && selected && selected._id === rowId) paintPanel();
    }

    function openPanel(record, nextMode) {
      if (!record) return;
      selected = record;
      mode = nextMode || mode || 'detail';
      panel.hidden = false;
      body.classList.add('has-panel');
      paintPanel();
      refreshAllRowStates();
      if (!isWide && window.innerWidth <= 900) {
        panel.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
      }
    }

    function closePanel() {
      panel.hidden = true;
      body.classList.remove('has-panel');
      refreshAllRowStates();
    }

    function renderQuizOrMarkdown(container, text) {
      const marker = text.indexOf('【参考答案】');
      if (marker >= 0) {
        const qBox = node('div');
        renderSafeMarkdown(qBox, text.slice(0, marker).trim());
        const details = node('details', null, 'pai-quiz-answers');
        details.append(node('summary', '查看参考答案'));
        const aBox = node('div');
        renderSafeMarkdown(aBox, text.slice(marker + '【参考答案】'.length).trim());
        details.append(aBox);
        container.replaceChildren(qBox, details);
      } else {
        renderSafeMarkdown(container, text);
      }
    }

    function paintPanel() {
      panel.replaceChildren();
      const activeRecord = selected || data[0];
      if (!activeRecord) return;
      selected = activeRecord;

      const header = node('header');
      const selectWrap = node('div', null, 'pai-panel-row-picker');
      const select = node('select');
      select.setAttribute('aria-label', '选择当前行');
      data.forEach(r => {
        const prefix = doneSet.has(r._id) ? '✓ ' : '';
        select.append(new Option(prefix + (r.c0 || ('第 ' + r._id + ' 行')), String(r._id)));
      });
      select.value = String(selected._id);
      select.onchange = () => openPanel(data.find(r => r._id === Number(select.value)), mode);

      const doneBtn = button(doneSet.has(selected._id) ? '✓ 已打卡' : '打卡完成', () => toggleDone(selected._id),
        'pai-btn-done' + (doneSet.has(selected._id) ? ' is-done' : ''));
      selectWrap.append(select, doneBtn);
      header.append(selectWrap, button('关闭', closePanel, 'pai-btn-close'));
      panel.append(header);

      const tabs = node('div', null, 'pai-table-panel-tabs');
      [['detail', '完整内容'], ['ai', '表格智能体']].forEach(([key, label]) => {
        if (key === 'ai' && aiButton.hidden) return;
        const b = button(label, () => { mode = key; paintPanel(); });
        b.setAttribute('aria-pressed', String(mode === key));
        tabs.append(b);
      });
      panel.append(tabs);

      if (mode === 'detail') {
        const list = node('dl', null, 'pai-table-record');
        columns.forEach((c, i) => {
          const richCell = cells[selected._id - 1][i];
          if (!selected['c' + i] && !richCell.querySelector('img')) return;
          list.append(node('dt', c));
          const dd = node('dd');
          dd.append(richCell.cloneNode(true));
          list.append(dd);
        });
        panel.append(list);
        return;
      }

      // AI Learning Assistant Tab
      const isAnyPending = Boolean(activeStreamAbort);
      const actions = node('div', null, 'pai-table-ai-actions');
      [['explain', '讲解知识点'], ['order', '学习顺序'], ['quiz', '出自测题']].forEach(([act, label]) => {
        const cached = actionCache.get(selected._id + ':' + act);
        const btnLabel = cached && !cached.error && !cached.pending ? (label + ' ✓') : label;
        const b = button(btnLabel, () => {
          currentPresetAction = act;
          if (cached && !cached.error) {
            paintPanel();
          } else {
            runAiAction(selected, act, '');
          }
        });
        b.disabled = isAnyPending && !(cached && cached.pending);
        b.setAttribute('aria-pressed', String(currentPresetAction === act));
        actions.append(b);
      });
      panel.append(actions);

      const presetKey = selected._id + ':' + currentPresetAction;
      const presetState = actionCache.get(presetKey);
      if (presetState) {
        const box = node('div', null, 'pai-table-ai-answer' + (presetState.error ? ' is-error' : ''));
        box.setAttribute('role', presetState.error ? 'alert' : 'region');
        if (presetState.pending && !presetState.text) {
          const loadingRow = node('div', null, 'pai-ai-loading');
          loadingRow.append(node('span', '正在生成讲解…'), button('停止', () => { if (activeStreamAbort) activeStreamAbort(); }, 'pai-btn-link'));
          box.append(loadingRow);
        } else {
          const contentBox = node('div', null, 'pai-md-body');
          if (presetState.error) contentBox.textContent = presetState.text;
          else if (currentPresetAction === 'quiz' && !presetState.pending) renderQuizOrMarkdown(contentBox, presetState.text);
          else renderSafeMarkdown(contentBox, presetState.text);
          box.append(contentBox);

          const metaBar = node('div', null, 'pai-ai-meta-bar');
          if (presetState.pending) {
            metaBar.append(node('span', '生成中…'), button('停止生成', () => { if (activeStreamAbort) activeStreamAbort(); }, 'pai-btn-link'));
          } else {
            metaBar.append(button('重新生成', () => runAiAction(selected, currentPresetAction, '', true), 'pai-btn-link'));
          }
          box.append(metaBar);
        }
        panel.append(box);
      }

      // Custom Question Form & Thread
      const qaSection = node('div', null, 'pai-ai-qa-section');
      const form = node('form', null, 'pai-table-question');
      const question = node('textarea');
      question.rows = 2;
      question.maxLength = 500;
      question.placeholder = '输入指令…';
      question.setAttribute('aria-label', '输入指令');
      question.setAttribute('aria-keyshortcuts', 'Control+Enter Meta+Enter');
      const rowId = selected._id;
      question.value = drafts.get(rowId) || '';
      const send = node('button', '发送');
      send.type = 'submit';
      send.disabled = isAnyPending || !question.value.trim();
      question.oninput = () => {
        drafts.set(rowId, question.value);
        send.disabled = isAnyPending || !question.value.trim();
      };
      question.onkeydown = ev => {
        if (!ev.isComposing && (ev.ctrlKey || ev.metaKey) && ev.key === 'Enter') {
          ev.preventDefault();
          form.requestSubmit ? form.requestSubmit() : form.dispatchEvent(new Event('submit', { cancelable: true }));
        }
      };
      form.append(question, send);
      form.onsubmit = event => {
        event.preventDefault();
        const q = question.value.trim();
        if (q) runAiAction(selected, 'ask', q);
      };
      qaSection.append(form);

      const history = qaHistory.get(rowId) || [];
      history.slice().reverse().forEach(item => {
        const itemBox = node('div', null, 'pai-table-ai-answer pai-qa-item' + (item.error ? ' is-error' : ''));
        itemBox.append(node('div', item.question, 'pai-table-asked'));
        if (item.steps && item.steps.length) {
          itemBox.append(node('div', '已执行操作：' + item.steps.join(' → '), 'pai-agent-steps'));
        }
        if (item.pending && !item.text) {
          const loadingRow = node('div', null, 'pai-ai-loading');
          loadingRow.append(node('span', '正在处理指令…'), button('停止', () => { if (activeStreamAbort) activeStreamAbort(); }, 'pai-btn-link'));
          itemBox.append(loadingRow);
        } else {
          const answerBody = node('div', null, 'pai-md-body');
          if (item.error) answerBody.textContent = item.text;
          else renderSafeMarkdown(answerBody, item.text);
          itemBox.append(answerBody);
          if (item.exportSpec) {
            const dlBar = node('div', null, 'pai-ai-meta-bar');
            dlBar.append(button('下载当前结果 CSV', () => downloadCsvForSpec(item.exportSpec), 'pai-btn-chip is-active'));
            itemBox.append(dlBar);
          }
        }
        qaSection.append(itemBox);
      });
      panel.append(qaSection);
    }

    async function runAiAction(record, act, questionText, forceRefresh) {
      if (activeStreamAbort) return;
      if (!capabilitiesState.loggedIn) {
        const opened = promptLoginIfAvailable();
        const loginMsg = opened ? '请先在弹窗中完成登录，再使用 表格智能体' : '请先登录后再使用 表格智能体';
        if (act === 'ask') {
          const list = qaHistory.get(record._id) || [];
          qaHistory.set(record._id, [...list.filter(x => !x.error), { question: questionText, pending: false, error: true, text: loginMsg }]);
        } else {
          currentPresetAction = act;
          actionCache.set(record._id + ':' + act, { pending: false, error: true, text: loginMsg });
        }
        paintPanel();
        return;
      }

      const key = record._id + ':' + act;
      if (act !== 'ask' && !forceRefresh && actionCache.get(key) && !actionCache.get(key).error) {
        paintPanel();
        return;
      }

      const stateObj = act === 'ask'
        ? { question: questionText, pending: true, error: false, text: '' }
        : { pending: true, error: false, text: '' };

      if (act === 'ask') {
        const prev = qaHistory.get(record._id) || [];
        qaHistory.set(record._id, [...prev, stateObj]);
      } else {
        currentPresetAction = act;
        actionCache.set(key, stateObj);
      }

      const controller = new AbortController();
      let userAborted = false;
      activeStreamAbort = () => {
        userAborted = true;
        controller.abort();
      };
      paintPanel();

      const startedViewSeq = viewMutationSeq;
      const payload = JSON.stringify({
        title: exportName,
        columns,
        values: columns.map((_, i) => record['c' + i]),
        rows: act === 'ask' ? data.map(r => columns.map((_, i) => r['c' + i])) : undefined,
        visibleRows: act === 'ask' ? visibleRowIds() : undefined,
        visibleColumns: act === 'ask' ? visibleColumnIndices() : undefined,
        action: act,
        question: questionText
      });
      const headers = { 'Content-Type': 'application/json', 'X-Pai-Table': '1' };

      try {
        if (act === 'ask') {
          const agentResp = await fetch('/api/tables/agent', {
            method: 'POST', credentials: 'same-origin', headers, body: payload, signal: controller.signal
          });
          const agentJson = await agentResp.json().catch(() => null);
          if (!agentResp.ok || agentJson?.status?.code !== 0) {
            if (agentResp.status === 401 || agentJson?.status?.code === 401) {
              capabilitiesState.loggedIn = false;
              promptLoginIfAvailable();
            }
            throw new Error(agentJson?.status?.msg || '请求失败，请重试');
          }
          const res = agentJson.result || {};
          stateObj.text = res.answer || '';
          stateObj.steps = Array.isArray(res.steps) ? res.steps : [];
          stateObj.exportSpec = res.export || null;
          if (res.view && startedViewSeq === viewMutationSeq) {
            applyAgentView(res.view);
          }
          if (drafts.get(record._id)?.trim() === questionText) drafts.delete(record._id);
          return;
        }
        const response = await fetch('/api/tables/learning-help/stream', {
          method: 'POST',
          credentials: 'same-origin',
          headers,
          body: payload,
          signal: controller.signal
        });

        const contentType = response.headers.get('content-type') || '';
        if (!response.ok || contentType.includes('application/json')) {
          // Handle synchronous JSON error (401/403/429/400) or non-SSE fallback
          if (response.status === 404) {
            const fallback = await fetch('/api/tables/learning-help', {
              method: 'POST', credentials: 'same-origin', headers, body: payload, signal: controller.signal
            });
            const fbJson = await fallback.json();
            if (!fallback.ok || fbJson.status?.code !== 0) {
              if (fallback.status === 401 || fbJson.status?.code === 401) {
                capabilitiesState.loggedIn = false;
                promptLoginIfAvailable();
              }
              throw new Error(fbJson.status?.msg || '请求失败，请重试');
            }
            stateObj.text = fbJson.result || '';
          } else {
            const errJson = await response.json().catch(() => null);
            if (response.status === 401 || errJson?.status?.code === 401) {
              capabilitiesState.loggedIn = false;
              promptLoginIfAvailable();
            }
            throw new Error(errJson?.status?.msg || '请求失败，请重试');
          }
        } else if (response.body) {
          const reader = response.body.getReader();
          const decoder = new TextDecoder('utf-8');
          let buffer = '';
          while (true) {
            const { value, done } = await reader.read();
            if (done) break;
            buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n');
            let sep;
            while ((sep = buffer.indexOf('\n\n')) >= 0) {
              const rawEvent = buffer.slice(0, sep);
              buffer = buffer.slice(sep + 2);
              const dataLines = rawEvent.split('\n')
                .filter(l => l.startsWith('data:'))
                .map(l => l.slice(5).trim())
                .join('\n');
              if (!dataLines) continue;
              const evt = JSON.parse(dataLines);
              if (evt.type === 'delta' && evt.delta) {
                stateObj.text += evt.delta;
                if (selected && selected._id === record._id && mode === 'ai') paintPanel();
              } else if (evt.type === 'error') {
                throw new Error(evt.message || 'AI 服务暂不可用，请重试');
              }
            }
          }
        }
      } catch (err) {
        if (userAborted) {
          if (!stateObj.text) stateObj.text = '已停止生成';
        } else {
          stateObj.error = true;
          stateObj.text = err.message || '请求失败，请重试';
        }
      } finally {
        stateObj.pending = false;
        activeStreamAbort = null;
        if (selected && selected._id === record._id && mode === 'ai') paintPanel();
      }
    }

    function applyFilters() {
      if (!grid) return;
      const q = search.value.trim().toLocaleLowerCase();
      grid.setFilter(r => {
        if (agentRowOrder && !agentRowOrder.has(r._id)) return false;
        if (persisted.undoneOnly && doneSet.has(r._id)) return false;
        if (!q) return true;
        return columns.some((_, i) => String(r['c' + i]).toLocaleLowerCase().includes(q));
      });
    }

    try {
      const defs = [{
        title: '打卡',
        field: '_id',
        width: 68,
        minWidth: 68,
        frozen: true,
        headerSort: false,
        sorter: (a, b) => agentRowOrder ? agentRowOrder.get(a) - agentRowOrder.get(b) : a - b,
        hozAlign: 'center',
        formatter: cell => {
          const rowData = cell.getRow().getData();
          const box = node('div', null, 'pai-row-index-cell');
          const chk = node('input', null, 'pai-row-check');
          chk.type = 'checkbox';
          chk.checked = doneSet.has(rowData._id);
          chk.title = '标记完成';
          chk.setAttribute('aria-label', '标记第 ' + rowData._id + ' 行已完成');
          chk.onclick = ev => { ev.stopPropagation(); toggleDone(rowData._id); };
          const numBtn = button(String(rowData._id), ev => { ev.stopPropagation(); openPanel(rowData, 'detail'); }, 'pai-row-num');
          numBtn.title = '查看第 ' + rowData._id + ' 行完整内容';
          numBtn.setAttribute('aria-label', '查看第 ' + rowData._id + ' 行');
          box.append(chk, numBtn);
          return box;
        }
      }];

      const measure = document.createElement('canvas').getContext('2d');
      measure.font = '13.5px ' + getComputedStyle(wrapper).fontFamily;
      const preferredWidths = columns.map((title, i) => {
        const contentWidth = data.reduce((widest, row) =>
          Math.max(widest, measure.measureText(row['c' + i].slice(0, 160)).width), 0);
        const headerWidth = Math.ceil(measure.measureText(title).width) + 52;
        const textWidth = contentWidth > 160 ? contentWidth / 2 : contentWidth;
        return Math.ceil(Math.min(300, Math.max(72, headerWidth, textWidth + 26)));
      });

      function fitVisibleColumns() {
        if (!grid) return;
        const visible = grid.getColumns().filter(col => col.isVisible() && col.getField() !== '_id');
        const auto = visible.filter(col => !persisted.widths[col.getField()]);
        const baseWidth = col => preferredWidths[Number(col.getField().slice(1))];
        const used = visible.reduce((sum, col) =>
          sum + (persisted.widths[col.getField()] || baseWidth(col)), grid.getColumn('_id').getWidth());
        const growable = auto.filter(col => baseWidth(col) >= 170);
        const extra = growable.length ? Math.max(0, Math.floor((host.clientWidth - used) / growable.length)) : 0;
        auto.forEach(col => {
          const width = baseWidth(col);
          const target = width >= 170 ? width + extra : width;
          if (col.getWidth() !== target) col.setWidth(target);
        });
      }

      columns.forEach((c, i) => {
        const fieldName = 'c' + i;
        defs.push({
          title: c,
          field: fieldName,
          visible: !persisted.hidden.includes(i),
          width: persisted.widths[fieldName] || preferredWidths[i],
          minWidth: 64,
          frozen: i === 0 && window.innerWidth > 700,
          headerTooltip: true,
          sorter: 'alphanum',
          formatter: cell => {
            const value = cells[cell.getRow().getData()._id - 1][i].cloneNode(true);
            value.className = 'pai-table-cell';
            return value;
          },
          cellClick: (e, cell) => {
            if (e.target.closest('a,input,button')) return;
            openPanel(cell.getRow().getData(), panel.hidden ? 'detail' : mode);
          }
        });
      });

      grid = new window.Tabulator(host, {
        data,
        columns: defs,
        layout: 'fitData',
        height: normalHeight,
        rowHeight: 64,
        placeholder: '没有匹配的记录',
        movableColumns: true,
        columnDefaults: { resizable: true },
        rowFormatter: row => applyRowVisualState(row),
        groupBy: group.value || false,
        groupHeader: (value, n) => node('span', String(value || '未填写') + ' · ' + n + ' 条')
      });

      const resizeObserver = new ResizeObserver(fitVisibleColumns);
      grid.on('tableBuilt', () => {
        resizeObserver.observe(area);
        fitVisibleColumns();
      });
      grid.on('columnVisibilityChanged', fitVisibleColumns);

      grid.on('dataFiltered', (filters, filtered) => {
        const isFiltered = filtered.length !== data.length;
        count.textContent = isFiltered ? ('筛选出 ' + filtered.length + ' / ' + data.length + ' 条记录') : ('共 ' + data.length + ' 条记录');
        exportButton.textContent = isFiltered ? ('导出筛选 CSV (' + filtered.length + ')') : '导出 CSV';
      });

      grid.on('dataSorted', () => { viewMutationSeq++; });
      grid.on('columnMoved', () => { viewMutationSeq++; });

      grid.on('columnResized', col => {
        const f = col.getField();
        if (f && f !== '_id') {
          persisted.widths[f] = col.getWidth();
          fitVisibleColumns();
          persistCurrent();
        }
      });

      if (persisted.undoneOnly) {
        grid.on('tableBuilt', applyFilters);
      }

      let searchTimer;
      search.oninput = () => {
        viewMutationSeq++;
        clearTimeout(searchTimer);
        searchTimer = setTimeout(applyFilters, 140);
      };

      group.onchange = () => {
        viewMutationSeq++;
        grid.setGroupBy(group.value || false);
        persistCurrent();
      };

      columns.forEach((c, i) => {
        const label = node('label');
        const check = node('input');
        check.type = 'checkbox';
        check.checked = !persisted.hidden.includes(i);
        check.onchange = () => {
          viewMutationSeq++;
          const visibleDataCols = grid.getColumns().filter(col => col.isVisible() && col.getField() !== '_id');
          if (!check.checked && visibleDataCols.length <= 1) {
            check.checked = true;
            return;
          }
          grid.toggleColumn('c' + i);
          if (check.checked) persisted.hidden = persisted.hidden.filter(idx => idx !== i);
          else if (!persisted.hidden.includes(i)) persisted.hidden.push(i);
          persistCurrent();
        };
        label.append(check, document.createTextNode(c));
        columnPanel.append(label);
      });

      instances.push({
        destroy() {
          resizeObserver.disconnect();
          if (activeStreamAbort) activeStreamAbort();
          if (isWide) setWide(false);
          if (grid) { grid.destroy(); grid = null; }
        }
      });
    } catch (error) {
      if (grid) grid.destroy();
      ui.replaceChildren();
      table.hidden = false;
      table.classList.remove('pai-source-table');
      const scroll = node('div', null, 'pai-article-table-scroll');
      scroll.append(table);
      ui.append(scroll);
    }
  }

  function resetArticleTables() {
    while (instances.length) {
      const inst = instances.pop();
      try { inst.destroy(); } catch (e) { /* ignore */ }
    }
    if (activeWideClose) activeWideClose();
  }

  function initArticleTables() {
    const content = document.getElementById('articleContent');
    if (!content || !window.PaiTableModel) return;
    const rawTables = Array.from(content.querySelectorAll('table'))
      .filter(t => !t.closest('.pai-article-table,.pai-article-table-compact'));
    if (!rawTables.length) return;

    const interactiveCandidates = [];
    rawTables.forEach((table, idx) => {
      const rows = Array.from(table.rows);
      if (rows.length < 2) return;
      const colCount = rows[0].cells.length;
      // A full-width note after the records belongs below the grid.
      const notes = [];
      while (colCount > 1 && rows.length > 1) {
        const last = rows[rows.length - 1];
        const cell = last.cells[0];
        if (last.cells.length !== 1 || cell.colSpan !== colCount || cell.rowSpan !== 1) break;
        notes.unshift(cell);
        rows.pop();
      }
      const irregular = rows.length < 2 || colCount === 0 || rows.some(r =>
        r.cells.length > colCount || Array.from(r.cells).some(c => c.colSpan !== 1 || c.rowSpan !== 1)
      );
      const dataRowCount = rows.length - 1;
      const textLen = rows.reduce((size, row) => size + row.textContent.trim().length, 0);
      const isSmallSimple = dataRowCount < 4 && colCount < 5 && textLen < 320;
      if (irregular || isSmallSimple) {
        wrapCompactTable(table);
      } else {
        interactiveCandidates.push({ table, idx, rows, notes });
      }
    });

    if (!interactiveCandidates.length) return;
    ensureTabulator().then(() => {
      const usedTitles = new Map();
      interactiveCandidates.forEach(({ table, idx, rows, notes }) => {
        if (document.body.contains(table)) {
          enhanceInteractive(table, idx, usedTitles, rows, notes);
        }
      });
    }).catch(() => {
      interactiveCandidates.forEach(({ table }) => {
        if (document.body.contains(table)) wrapCompactTable(table);
      });
    });
  }

  window.initArticleTables = initArticleTables;
  window.resetArticleTables = resetArticleTables;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initArticleTables);
  } else {
    initArticleTables();
  }
})();
