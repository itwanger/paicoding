#!/usr/bin/env node
'use strict';

const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const ROOT = path.resolve(__dirname, '..');
const SCRIPT_PATH = path.join(
  ROOT,
  'paicoding-ui/src/main/resources/static/js/biz/highlightcomment.js'
);
const SCRIPT_SOURCE = fs.readFileSync(SCRIPT_PATH, 'utf8');
const TEMPLATE_PATHS = [
  path.join(ROOT, 'paicoding-ui/src/main/resources/templates/views/article-detail/index.html'),
  path.join(ROOT, 'paicoding-ui/src/main/resources/templates/views/column-detail/index.html')
];
const encoder = new TextEncoder();

function abortError() {
  const error = new Error('aborted');
  error.name = 'AbortError';
  return error;
}

function classList() {
  const values = new Set();
  return {
    add(...names) {
      names.forEach(name => values.add(name));
    },
    remove(...names) {
      names.forEach(name => values.delete(name));
    },
    toggle(name, force) {
      if (force === true) {
        values.add(name);
      } else if (force === false) {
        values.delete(name);
      } else if (values.has(name)) {
        values.delete(name);
      } else {
        values.add(name);
      }
    },
    contains(name) {
      return values.has(name);
    }
  };
}

function element(overrides) {
  return Object.assign({
    addEventListener() {},
    classList: classList(),
    dataset: {},
    disabled: false,
    style: {},
    textContent: '',
    value: ''
  }, overrides || {});
}

function response(options) {
  const settings = Object.assign({
    status: 200,
    contentType: 'text/event-stream;charset=UTF-8',
    text: '',
    chunks: []
  }, options || {});
  let index = 0;

  return {
    status: settings.status,
    ok: settings.status >= 200 && settings.status < 300,
    headers: {
      get(name) {
        return name.toLowerCase() === 'content-type' ? settings.contentType : null;
      }
    },
    body: settings.body || {
      getReader() {
        return {
          read() {
            if (index < settings.chunks.length) {
              return Promise.resolve({
                done: false,
                value: encoder.encode(settings.chunks[index++])
              });
            }
            return Promise.resolve({ done: true });
          }
        };
      }
    },
    text() {
      return Promise.resolve(settings.text);
    }
  };
}

function pendingResponse(signal) {
  return response({
    body: {
      getReader() {
        return {
          read() {
            if (signal.aborted) {
              return Promise.reject(abortError());
            }
            return new Promise((resolve, reject) => {
              signal.addEventListener('abort', () => reject(abortError()), { once: true });
            });
          }
        };
      }
    }
  });
}

function sse(event) {
  return `data: ${JSON.stringify(event)}\n\n`;
}

function createHarness(options) {
  const settings = Object.assign({ loggedIn: true }, options || {});
  const calls = [];
  const modals = [];
  const timers = new Map();
  const toasts = [];
  let timerId = 0;

  const elements = {
    quoteCommentInput: element(),
    submitQuoteComment: element(),
    highlightAiPanel: element({ style: { display: 'none' } }),
    highlightAiStatus: element(),
    highlightAiReply: element(),
    loginModal: element()
  };
  const sidebar = element({
    parentElement: null,
    querySelector() { return null; }
  });
  Object.defineProperty(sidebar, 'innerHTML', {
    set() {
      // showQuoteCommentForm 会替换整块 DOM；测试也必须换成新节点才能捕捉旧流串台。
      elements.quoteCommentInput = element();
      elements.submitQuoteComment = element();
      elements.highlightAiPanel = element({ style: { display: 'none' } });
      elements.highlightAiStatus = element();
      elements.highlightAiReply = element();
    }
  });
  elements.quoteCommentSidebar = sidebar;
  const navbar = {
    getAttribute(name) {
      return name === 'data-islogin' ? String(settings.loggedIn) : null;
    }
  };
  const document = {
    addEventListener() {},
    getElementById(id) {
      return elements[id] || null;
    },
    querySelector(selector) {
      return selector === 'nav.navbar[data-islogin]' ? navbar : null;
    }
  };
  const jQuery = selector => ({
    modal(action) {
      modals.push({ selector, action });
    }
  });
  const window = {
    dispatchEvent() {},
    innerWidth: 1200,
    jQuery
  };
  const context = vm.createContext({
    AbortController,
    console: {
      debug() {},
      error() {},
      info() {},
      log() {},
      warn() {}
    },
    document,
    Event: function Event(type) { this.type = type; },
    fetch(url, fetchOptions) {
      calls.push({ url, options: fetchOptions });
      if (!settings.fetch) {
        throw new Error('unexpected fetch');
      }
      return Promise.resolve(settings.fetch(url, fetchOptions, calls.length));
    },
    Promise,
    setTimeout(callback) {
      timerId += 1;
      timers.set(timerId, callback);
      return timerId;
    },
    clearTimeout(id) {
      timers.delete(id);
    },
    TextDecoder,
    toastr: {
      error(message) { toasts.push({ type: 'error', message }); },
      info(message) { toasts.push({ type: 'info', message }); },
      success(message) { toasts.push({ type: 'success', message }); }
    },
    window,
    $: jQuery,
    articleId: '2533625399918592'
  });
  window.window = window;
  vm.runInContext(SCRIPT_SOURCE, context, { filename: SCRIPT_PATH });
  vm.runInContext("toSaveSelection = { selectedText: '测试划线内容' }", context);

  return {
    calls,
    context,
    elements,
    modals,
    timers,
    toasts,
    fireTimers() {
      const callbacks = Array.from(timers.values());
      timers.clear();
      callbacks.forEach(callback => callback());
    },
    start(content) {
      context.startHighlightAiReply('smart', content || '@派聪明 测试问题');
    }
  };
}

async function flushPromises() {
  for (let i = 0; i < 8; i += 1) {
    await Promise.resolve();
  }
  await new Promise(resolve => setImmediate(resolve));
}

function requestId(fetchOptions) {
  return JSON.parse(fetchOptions.body).requestId;
}

async function testAnonymousRequestOpensLoginWithoutFetch() {
  const harness = createHarness({ loggedIn: false });
  harness.start();

  assert.strictEqual(harness.calls.length, 0);
  assert.deepStrictEqual(harness.modals, [{ selector: '#loginModal', action: 'show' }]);
  assert.strictEqual(harness.toasts[0].message, '请先登录后再提问');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
}

async function testExpiredSessionJsonOpensLoginAndSettlesUi() {
  const harness = createHarness({
    fetch() {
      return response({
        contentType: 'application/json;charset=UTF-8',
        text: JSON.stringify({ status: { code: 100403003, msg: '未登录' }, result: null })
      });
    }
  });
  harness.start();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, '请先登录后再提问');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
  assert.deepStrictEqual(harness.modals, [{ selector: '#loginModal', action: 'show' }]);
}

async function testNonSseResponseFailsAndSettlesUi() {
  const harness = createHarness({
    fetch() {
      return response({ contentType: 'text/html', text: '<h1>Bad Gateway</h1>' });
    }
  });
  harness.start();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, 'AI 回复服务返回格式异常');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
}

async function testEofWithoutTerminalEventFails() {
  const harness = createHarness({
    fetch(url, fetchOptions) {
      const id = requestId(fetchOptions);
      return response({ chunks: [sse({ requestId: id, type: 'comment', bot: '派聪明', commentId: 1 })] });
    }
  });
  harness.start();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, 'AI 回复连接已中断，请稍后再试');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
}

async function testValidDoneEventCompletes() {
  const harness = createHarness({
    fetch(url, fetchOptions) {
      const id = requestId(fetchOptions);
      return response({ chunks: [sse({ requestId: id, type: 'done', bot: '派聪明', commentId: 1 })] });
    }
  });
  harness.start();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, '回复已生成');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
  assert.strictEqual(harness.toasts.some(toast => toast.type === 'success'), true);
}

async function testMalformedSseFails() {
  const harness = createHarness({
    fetch() {
      return response({ chunks: ['data: {not-json}\n\n'] });
    }
  });
  harness.start();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, 'AI 回复数据解析失败');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
}

async function testMismatchedRequestIdFailsAtEof() {
  const harness = createHarness({
    fetch() {
      return response({ chunks: [sse({ requestId: 'other-request', type: 'done' })] });
    }
  });
  harness.start();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, 'AI 回复连接已中断，请稍后再试');
}

async function testSupersededRequestCannotSettleNewRequest() {
  const harness = createHarness({
    fetch(url, fetchOptions, callNumber) {
      if (callNumber === 1) {
        return pendingResponse(fetchOptions.signal);
      }
      const id = requestId(fetchOptions);
      return response({ chunks: [sse({ requestId: id, type: 'done', bot: '派聪明', commentId: 2 })] });
    }
  });
  harness.start('@派聪明 第一个问题');
  await flushPromises();
  harness.start('@派聪明 第二个问题');
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, '回复已生成');
  assert.strictEqual(harness.toasts.some(toast => toast.message === '已停止生成'), false);
  assert.strictEqual(harness.toasts.some(toast => toast.message === 'AI 回复生成失败，请稍后再试'), false);
}

async function testReplacingFormInvalidatesPendingRequest() {
  let signal;
  const harness = createHarness({
    fetch(url, fetchOptions) {
      signal = fetchOptions.signal;
      return pendingResponse(signal);
    }
  });
  harness.start();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, '派聪明 正在回复...');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, true);
  harness.context.showQuoteCommentForm('第二段划线内容');

  assert.strictEqual(signal.aborted, true);
  assert.strictEqual(harness.timers.size, 0);
  assert.strictEqual(vm.runInContext('highlightAiActiveRequest === null', harness.context), true);
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, '');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
  assert.strictEqual(harness.toasts.some(toast => toast.message === 'AI 回复生成失败，请稍后再试'), false);
}

async function testExplicitStopSettlesCurrentRequest() {
  const harness = createHarness({
    fetch(url, fetchOptions) {
      return pendingResponse(fetchOptions.signal);
    }
  });
  harness.start();
  await flushPromises();
  harness.context.stopHighlightAiReply();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, '已停止生成');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
}

async function testClientTimeoutSettlesCurrentRequest() {
  const harness = createHarness({
    fetch(url, fetchOptions) {
      return pendingResponse(fetchOptions.signal);
    }
  });
  harness.start();
  await flushPromises();
  harness.fireTimers();
  await flushPromises();

  assert.strictEqual(harness.elements.highlightAiStatus.textContent, 'AI 回复超时，请稍后再试');
  assert.strictEqual(harness.elements.quoteCommentInput.disabled, false);
}

async function testTemplateAssetVersionsMatch() {
  const versions = TEMPLATE_PATHS.map(templatePath => {
    const template = fs.readFileSync(templatePath, 'utf8');
    const matches = Array.from(template.matchAll(/highlightcomment\.js\?v=(\d+)/g), match => match[1]);
    assert.strictEqual(matches.length, 2);
    assert.strictEqual(new Set(matches).size, 1);
    return matches[0];
  });

  assert.strictEqual(new Set(versions).size, 1);
}

async function run() {
  const tests = [
    testAnonymousRequestOpensLoginWithoutFetch,
    testExpiredSessionJsonOpensLoginAndSettlesUi,
    testNonSseResponseFailsAndSettlesUi,
    testEofWithoutTerminalEventFails,
    testValidDoneEventCompletes,
    testMalformedSseFails,
    testMismatchedRequestIdFailsAtEof,
    testSupersededRequestCannotSettleNewRequest,
    testReplacingFormInvalidatesPendingRequest,
    testExplicitStopSettlesCurrentRequest,
    testClientTimeoutSettlesCurrentRequest,
    testTemplateAssetVersionsMatch
  ];

  for (const test of tests) {
    await test();
    process.stdout.write(`PASS ${test.name}\n`);
  }
}

run().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
