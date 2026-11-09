// Run with Playwright and its Chromium installed (or NODE_PATH pointing to global packages).
const assert = require('node:assert/strict');
const path = require('node:path');
const {chromium} = require('playwright');
const assets = path.join(__dirname, '../paicoding-ui/src/main/resources/static');
const original = '一个超级简化版的 Claude Code，大概 400 行代码就能跑起来。';

async function runReader(browser, mobile) {
    const context = await browser.newContext({viewport: {width: mobile ? 420 : 1200, height: 800}});
    try {
        const page = await context.newPage();
        const errors = [];
        page.on('pageerror', error => errors.push(error.message));
        const threadHtml = '<div class="widget"><h3 class="com-nav-bar-title"></h3><div class="quote-content"><div class="quote-text"></div></div></div>';
        const threadRequests = [];
        const saves = [];
        await page.route('http://reader.test/**', async route => {
            const url = new URL(route.request().url());
            if (url.pathname === '/') {
                return route.fulfill({contentType: 'text/html; charset=utf-8', body: `<div id="highlightControls"><input id="highlightMineOnly" type="checkbox"></div>
                    <div id="highlightRelocation" hidden><button>取消</button></div>
                    <article id="articleContent"><p>先看一个具体的任务。在 PaiCLI 里输入“把 Hello.java 里</p>
                    <p>一个超级简化版的 <em>Claude Code</em>，大概 400 行代码就能跑起来。</p>
                    <p>第一组文字中提到了 Plan-and-Execute，它用来规划。</p><p>第二组文字也会提到 Plan-and-Execute，它用来执行。</p></article>
                    <details id="detachedHighlights"><summary></summary><ul></ul></details>
                    <div><aside id="quoteCommentSidebar"></aside></div><div class="modal" id="quoteCommentModal"></div>`});
            }
            let result;
            if (url.pathname.endsWith('/listTopComment')) {
                threadRequests.push(url.searchParams.get('commentId'));
                result = threadHtml;
            } else if (url.pathname.endsWith('/relocateHighlight')) {
                saves.push(route.request().postDataJSON());
                result = {...saves.at(-1), selectedText: '历史引文', anchoredText: saves.at(-1).selectedText, revision: 1};
            } else if (url.pathname.endsWith('/highlightComment')) {
                saves.push(route.request().postDataJSON());
                result = {commentId: 31, html: threadHtml};
            } else {
                return route.abort();
            }
            return route.fulfill({json: {status: {code: 0}, result}});
        });
        await page.goto('http://reader.test/');
        for (const file of ['js/jquery.min.js', 'js/bootstrap.min.js', 'js/biz/forum.js',
            'js/biz/selection.js', 'js/biz/highlightcomment.js']) {
            await page.addScriptTag({path: path.join(assets, file)});
        }
        await page.addStyleTag({path: path.join(assets, 'css/global.css')});
        await page.evaluate(() => {
            window.toastr = {success() {}, error(message) { throw new Error(message); }};
            window.articleId = 8;
        });

        const results = await page.evaluate(original => {
            const article = document.getElementById('articleContent');
            const last = article.lastElementChild;
            const word = 'Plan-and-Execute';
            const range = document.createRange();
            const start = last.firstChild.data.indexOf(word);
            range.setStart(last.firstChild, start);
            range.setEnd(last.firstChild, start + word.length);
            const anchor = rangeToElementJSON(range);
            const legacy = {elementTag: 'p', elementIndex: 0, startOffset: 0, endOffset: original.length, selectedText: original};
            const rows = [
                {commentId: 11, userId: 1, highlight: legacy}, {commentId: 12, userId: 2, highlight: legacy},
                {commentId: 13, userId: 2, highlight: {selectedText: 'Claude Code，大概'}},
                {commentId: 14, userId: 1, highlight: anchor},
                {commentId: 15, userId: 2, highlight: {selectedText: word}},
                {commentId: 16, userId: 1, highlight: {selectedText: '历史引文'}},
            ];
            article.prepend(document.createElement('p'));
            article.firstChild.textContent = '新版开头';
            const text = article.textContent;
            initUnderlineToSelection(rows, 1);
            renderArticleHighlights();
            return {preserved: article.textContent === text, nested: !!article.querySelector('span span'),
                original: article.querySelectorAll('p')[2].innerText,
                location: last.querySelector('.highlight-own')?.textContent,
                detached: Array.from(document.querySelectorAll('#detachedHighlights li > button:first-child'), e => e.textContent),
                wrong: !!article.querySelectorAll('p')[1].querySelector('span')};
        }, original);
        assert.deepEqual(results, {preserved: true, nested: false, original, location: 'Plan-and-Execute',
            detached: ['Plan-and-Execute', '历史引文'], wrong: false});

        await page.locator('em .selected-text-highlight').click();
        const panel = page.locator(mobile ? '#quoteCommentModal' : '#quoteCommentSidebar');
        await panel.locator('select').waitFor();
        assert.equal(await panel.locator('option').count(), 3);
        await panel.locator('select').selectOption('12');
        await page.waitForFunction(() => document.querySelector('.widget select')?.value === '12');
        assert.deepEqual(threadRequests, ['11', '12']);
        if (mobile) await page.evaluate(() => $('#quoteCommentModal').modal('hide'));

        await page.locator('#highlightMineOnly').check();
        assert.equal(await page.locator('#detachedHighlights li').count(), 1);
        assert.equal(await page.locator('em span').count(), 1);
        assert.equal(await page.locator('.selected-text-highlight:not(.highlight-own)').count(), 0);
        await page.locator('#detachedHighlights summary').click();
        await page.getByRole('button', {name: '重新定位'}).click();
        async function selectFirstWords() {
            await page.evaluate(() => {
                const node = document.querySelectorAll('#articleContent p')[1].firstChild;
                const range = document.createRange();
                range.setStart(node, 0);
                range.setEnd(node, 9);
                window.getSelection().removeAllRanges();
                window.getSelection().addRange(range);
            });
        }
        await selectFirstWords();
        await page.locator('#comment-icon').click();
        await page.waitForFunction(() => document.querySelector('#detachedHighlights').hidden);
        assert.equal(saves[0].selectedText, '先看一个具体的任务');
        assert.equal(await page.locator('#articleContent p').nth(1).locator('span').innerText(), saves[0].selectedText);
        // Select already marked inline text and submit a separate discussion without a reload.
        await page.evaluate(() => {
            const range = document.createRange();
            range.selectNodeContents(document.querySelector('em .selected-text-highlight'));
            window.getSelection().removeAllRanges();
            window.getSelection().addRange(range);
        });
        await page.locator('#comment-icon').click();
        await panel.locator('textarea').fill('我也想讨论这段代码');
        await panel.locator(mobile ? '#submitQuoteCommentModal' : '#submitQuoteComment').click();
        await panel.locator('textarea').waitFor({state: 'detached'});
        assert.equal(saves[1].highlight.selectedText, 'Claude Code');
        assert.equal(await page.locator('.highlight-draft').count(), 0);
        assert.deepEqual(errors, []);
    } finally {
        await context.close();
    }
}

(async () => {
    const browser = await chromium.launch();
    try {
        for (const mobile of [false, true]) {
            await runReader(browser, mobile);
            console.log('PASS', mobile ? 'mobile reader' : 'desktop reader');
        }
    } finally {
        await browser.close();
    }
})().catch(error => { console.error(error); process.exitCode = 1; });
