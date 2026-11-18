// Run with Playwright and its Chromium installed, as for test-highlight-anchors.js.
const assert = require('node:assert/strict');
const path = require('node:path');
const { chromium } = require('playwright');

const assets = path.join(__dirname, '../paicoding-ui/src/main/resources/static');
const tasks = ['整理知识点', '复习', '练习', '自测'];
const rows = tasks.map((task, i) =>
  `<tr><td>${i + 1}</td><td>${task.repeat(16)}</td></tr>`).join('');
const article = `<main class="article-info-wrap"><div id="articleContent">
  <table><tr><th>天</th><th>任务</th></tr>${rows}<tr><td colspan="2">月末<a href="#review">复盘</a></td></tr></table>
  <table><tr><th>天</th><th>任务</th></tr><tr><td rowspan="2">周一</td><td>算法</td></tr><tr><td>练习</td></tr>${rows}</table></div></main>`;

function fixture(chapter) {
  const layout = chapter
    ? `<div class="article-wrap"><div class="article-content-wrap">
       <div class="article-content-inter-wrap" style="margin-right:300px">${article}</div></div></div>`
    : `<div class="home article-detail"><div class="col-body pg-2-article"><div class="com-3-layout">
       <div class="layout-main">${article}</div><aside class="layout-side">目录</aside></div></div></div>`;
  return `<!doctype html><meta charset="utf-8"><link rel="stylesheet" href="/css/three/bootstrap.min.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/views/${chapter ? 'column' : 'article'}-detail.css">
    <link rel="stylesheet" href="/css/components/article-tables.css">
    ${layout}<script src="/js/biz/table-model.js"></script><script src="/js/biz/article-tables.js"></script>`;
}

async function check(browser, width, chapter) {
  const page = await browser.newPage({ viewport: { width, height: 850 } });
  try {
    const requests = [];
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.route('http://reader.test/**', route => {
      const pathname = new URL(route.request().url()).pathname;
      if (pathname === '/') return route.fulfill({ contentType: 'text/html', body: fixture(chapter) });
      if (pathname === '/api/tables/capabilities') return route.fulfill({ json: { result: { ai: true, loggedIn: true } } });
      if (pathname === '/api/tables/agent') {
        requests.push(route.request().postDataJSON());
        const response = requests.length === 1
          ? { status: { code: 503, msg: '请重试' } }
          : { status: { code: 0 }, result: { answer: '已调整', view: { rowIds: [3, 1], columns: [0, 1] } } };
        return route.fulfill({ json: response });
      }
      return route.fulfill({ path: path.join(assets, pathname) });
    });
    await page.goto('http://reader.test/');
    const table = page.locator('.pai-article-table');
    const button = name => table.getByRole('button', { name, exact: true }).first();
    await table.locator('.tabulator-row').first().waitFor();
    assert.equal(await table.locator('.pai-row-num').count(), tasks.length);
    assert.equal(await table.locator('.pai-table-note a').getAttribute('href'), '#review');
    assert.equal(await page.locator('.pai-article-table-compact td[rowspan="2"]:visible').count(), 1);
    const originalWidth = (await table.boundingBox()).width;
    await button('展开').click();
    const expandedWidth = (await table.boundingBox()).width;
    assert.ok(expandedWidth > width * 0.90);
    if (width > 700) assert.ok(expandedWidth > originalWidth + 100);
    assert.ok(await table.evaluate(el => el.closest('#articleContent') && getComputedStyle(el).position !== 'fixed'
      && !document.querySelector('.pai-table-backdrop') && document.body.style.overflow !== 'hidden'));

    const headerWidth = field => table.locator(`.tabulator-col[tabulator-field="${field}"]`).evaluate(el => el.clientWidth);
    assert.ok(await headerWidth('c0') < await headerWidth('c1'));
    await button('表格智能体').click();
    const input = table.getByRole('textbox', { name: '输入指令' });
    const send = button('发送');
    assert.ok(await send.isDisabled());
    await input.fill('筛选后按学习顺序显示');
    const box = await input.boundingBox();
    const submit = await send.boundingBox();
    assert.ok(submit.x > box.x && submit.y > box.y && submit.x + submit.width < box.x + box.width
      && submit.y + submit.height < box.y + box.height);

    await send.click();
    await table.getByText('请重试', { exact: true }).waitFor();
    assert.equal(await input.inputValue(), requests[0].question);
    await input.press(chapter ? 'Meta+Enter' : 'Control+Enter');
    await table.getByText('已调整', { exact: true }).waitFor();
    assert.equal(requests.length, 2);
    assert.equal(requests[1].action, 'ask');
    assert.equal(requests[1].rows.length, tasks.length);
    assert.equal(await input.inputValue(), '');
    assert.deepEqual(await table.locator('.pai-row-num').allTextContents(), ['3', '1']);
    if (chapter) await page.keyboard.press('Escape');
    else await button('收起').click();
    await button('展开').waitFor();
    assert.equal((await table.boundingBox()).width, originalWidth);
    if (width > 700) {
      const header = table.locator('.tabulator-col[tabulator-field="c0"]');
      await header.scrollIntoViewIfNeeded();
      const initial = await headerWidth('c0');
      const edge = await header.boundingBox();
      await page.mouse.move(edge.x + edge.width - 1, edge.y + edge.height / 2);
      await page.mouse.down();
      await page.mouse.move(edge.x + edge.width + 49, edge.y + edge.height / 2, { steps: 5 });
      await page.mouse.up();
      const resized = await headerWidth('c0');
      assert.ok(resized > initial + 30, 'column can be widened manually');
      await page.reload();
      await table.locator('.tabulator-row').first().waitFor();
      await button('展开').click();
      assert.equal(await headerWidth('c0'), resized);
      await page.evaluate(() => window.resetArticleTables());
      assert.equal(await page.locator('body.pai-table-wide').count(), 0);
    }
    assert.deepEqual(errors, []);
    console.log('PASS', chapter ? 'chapter' : 'article', width);
  } finally {
    await page.close();
  }
}

// Keep the pointer over the final grid even when the browser does not chain wheel input.
async function checkLastTableWheel(browser, width) {
  const page = await browser.newPage({ viewport: { width, height: 850 } });
  try {
    const finalRows = Array.from({ length: 16 }, (_, i) => `<tr><td>${i}</td><td>岗位</td></tr>`).join('');
    const content = fixture(true).replace('</div></main>', `<div style="height:1300px"></div>
      <table><tr><th>公司</th><th>说明</th></tr>${finalRows}</table></div></main>`);
    await page.route('http://reader.test/**', route => {
      const pathname = new URL(route.request().url()).pathname;
      if (pathname === '/') return route.fulfill({ contentType: 'text/html', body: content });
      if (pathname === '/api/tables/capabilities') return route.fulfill({ json: { result: {} } });
      return route.fulfill({ path: path.join(assets, pathname) });
    });
    await page.goto('http://reader.test/');
    const grid = page.locator('.tabulator-tableholder').last();
    await grid.locator('.tabulator-row').first().waitFor();
    // Isolate the inner scroll area to exercise the explicit handoff on wheel boundaries.
    await grid.evaluate(async el => {
      el.style.overscrollBehaviorY = 'contain';
      el.scrollIntoView({ block: 'center' });
      await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
      el.scrollTop = el.scrollHeight;
    });
    const chapter = page.locator('.article-content-wrap');
    const reader = width >= 992 ? chapter : page.locator('html');
    const offset = () => reader.evaluate(el => el.scrollTop);
    const start = await offset();
    const box = await grid.boundingBox();
    await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
    assert.ok(await grid.evaluate(el => el.scrollTop > 45));
    await page.mouse.wheel(0, -45);
    await page.waitForFunction(el => el.scrollTop < el.scrollHeight - el.clientHeight - 10,
      await grid.elementHandle());
    assert.equal(await offset(), start, 'the grid scrolls while it has earlier rows');
    await grid.evaluate(el => { el.scrollTop = 0; });
    await page.mouse.wheel(0, -60);
    await page.waitForFunction(([reader, start]) => reader.scrollTop < start,
      [await reader.elementHandle(), start]);
    // Keep rolling at the same pointer position until the article title is reached.
    for (let i = 0; i < 20 && await offset() > 0; i++) await page.mouse.wheel(0, -200);
    await page.waitForFunction(el => el.scrollTop === 0, await reader.elementHandle());
    console.log('PASS last table wheel', width);
  } finally {
    await page.close();
  }
}

(async () => {
  const browser = await chromium.launch();
  try {
    await check(browser, 1440, true);
    await check(browser, 1440, false);
    await check(browser, 390, false);
    await checkLastTableWheel(browser, 1440);
    await checkLastTableWheel(browser, 390);
  } finally {
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
