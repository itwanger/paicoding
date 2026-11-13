#!/usr/bin/env python3
"""Backfill reviewed summaries through metadata-only article/update.

Default: read-only preflight. Requires deployed summary support for --apply.
Never falls back to article/save (which rewrites Markdown images).
Every write is preceded by a drift check and followed by full metadata/body
verification. --rollback restores only summaries still matching this plan.
"""
import argparse
import hashlib
import json
import re
import urllib.request
from pathlib import Path
from sync_paicoding_tutorials import api
from tutorial_seo import description_for

ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT / 'docs/教程SEO修复'
PLAN = DIRECTORY / 'changes.json'
STATE = DIRECTORY / 'backfill-state.json'
HEADLINES = json.loads((DIRECTORY / 'java8-headlines.json').read_text())

def proxy(path, code=None):
    req = urllib.request.Request('http://localhost:3456/' + path,
                                 data=None if code is None else code.encode())
    with urllib.request.urlopen(req, timeout=180) as response:
        return json.load(response)

def checkpoint(state):
    temporary = STATE.with_suffix('.tmp')
    temporary.write_text(json.dumps(state, ensure_ascii=False, indent=2) + '\n')
    temporary.replace(STATE)

def verify_detail(row, detail, allowed):
    slug = row['slug']
    for key, expected in row['metadata'].items():
        if detail.get(key) != expected:
            raise RuntimeError(f'{slug}: {key} changed since the plan was prepared; stop and review')
    if hashlib.sha256(detail['content'].encode()).hexdigest() != row['contentSha256']:
        raise RuntimeError(f'{slug}: article body changed; stop and review')
    if detail['summary'] not in allowed:
        raise RuntimeError(f'{slug}: summary changed outside this plan; stop and review')

def verify_columns(target, plan):
    for cid, column in plan['columns'].items():
        result = api(target, 'column/listColumnArticle',
                     {'columnId':int(cid), 'pageNumber':1, 'pageSize':500})
        expected = [r['columnRow'] for r in plan['articles'] if str(r['columnId']) == cid]
        if int(result['total']) != len(expected) or result['list'] != expected:
            raise RuntimeError(f'Column {cid}: membership, order, titles or reading permissions changed; review the plan')
        if api(target, 'column/listGroups?columnId=' + cid) != column['groups']:
            raise RuntimeError(f'Column {cid}: groups changed; review the plan')

def rendered_description(title, summary):
    # Match SeoInjectService's normalization and Java UTF-16 truncation.
    # Generated using the project's JDK 8: its Unicode categories differ from
    # Python's for newer emoji. This keeps full-string checks faithful to prod.
    headline = HEADLINES[title]
    text = re.sub(r'<[^>]+>', ' ', summary)
    text = re.sub(r'!\[[^\]]*]\([^)]*\)', ' ', text)
    text = re.sub(r'\[([^\]]+)]\([^)]*\)', r'\1', text)
    text = re.sub(r'[*_`#>~]+', '', text)
    text = re.sub(r'\s+', ' ', text, flags=re.ASCII).strip()
    if headline and headline not in text:
        text = headline + '：' + text if text else headline
    encoded = text.encode('utf-16-le')
    return encoded[:360].decode('utf-16-le', errors='surrogatepass') + '...' if len(encoded) > 360 else text

def public_description(target, row, expected):
    code = '''(async () => {
      const r = await fetch('/' + SLUG, {cache:'no-store'});
      const doc = new DOMParser().parseFromString(await r.text(), 'text/html');
      const get = selector => [...doc.querySelectorAll(selector)].map(x => x.content);
      const jsonLd = [...doc.querySelectorAll('script[type="application/ld+json"]')]
        .flatMap(x => {try {return [JSON.parse(x.textContent)];} catch(e) {return [];} });
      return {status:r.status, description:get('meta[name="description"]'),
        og:get('meta[property="og:description"],meta[name="og:description"]'),
        jsonLd, canonical:doc.querySelector('link[rel="canonical"]')?.href};
    })()'''.replace('SLUG', json.dumps(row['slug']))
    result = proxy('eval?target=' + target, code)['value']
    description = result['description']
    if result['status'] != 200 or description != [rendered_description(row['title'], expected)]:
        raise RuntimeError(f"{row['slug']}: rendered meta description does not match the planned summary")
    if result['og'] != description:
        raise RuntimeError(f"{row['slug']}: Open Graph description differs")
    def descriptions(node):
        if isinstance(node, dict):
            if node.get('@type') in ('TechArticle', 'Article'):
                yield node.get('description')
            for value in node.values():
                yield from descriptions(value)
        elif isinstance(node, list):
            for value in node:
                yield from descriptions(value)
    result['structuredDescriptions'] = list(descriptions(result['jsonLd']))
    if description[0] not in result['structuredDescriptions']:
        raise RuntimeError(f"{row['slug']}: structured description differs or is absent")
    if result['canonical'] != 'https://paicoding.com/' + row['slug']:
        raise RuntimeError(f"{row['slug']}: canonical differs")
    return {k:v for k,v in result.items() if k != 'jsonLd'}

def run(target, args):
    plan = json.loads(PLAN.read_text())
    rows = plan['articles']
    for row in rows:
        if description_for(row['slug'], row['title']) != row['after']:
            raise RuntimeError(f"{row['slug']}: description map and reviewed plan differ")
    verify_columns(target, plan)
    for i, row in enumerate(rows, 1):
        detail = api(target, 'article/detail?articleId=' + row['articleId'])
        verify_detail(row, detail, [row['before'], row['after']])
        if i % 50 == 0:
            print('PREFLIGHT', i, '/', len(rows), flush=True)
    print('PREFLIGHT_OK', len(rows), flush=True)
    if not (args.apply or args.rollback or args.verify_public):
        return
    state = json.loads(STATE.read_text()) if STATE.exists() else {'articles':{}}
    state['planSha256'] = hashlib.sha256(PLAN.read_bytes()).hexdigest()
    selected = rows[:args.limit] if args.limit else rows
    for i, row in enumerate(selected, 1):
        desired = row['before'] if args.rollback else row['after']
        detail = api(target, 'article/detail?articleId=' + row['articleId'])
        verify_detail(row, detail, [row['before'], row['after']])
        record = state['articles'].setdefault(row['slug'], {'articleId':row['articleId']})
        if detail['summary'] != desired:
            if not (args.apply or args.rollback):
                raise RuntimeError(row['slug'] + ': not yet applied')
            record['phase'] = 'rolling-back' if args.rollback else 'updating'
            checkpoint(state)
            # Send no content, title, URL, category, permissions, tags or column fields.
            api(target, 'article/update', {'articleId':row['articleId'],
                'status':detail['status'], 'summary':desired})
            saved = api(target, 'article/detail?articleId=' + row['articleId'])
            verify_detail(row, saved, [desired])
        record['phase'] = 'rolled-back' if args.rollback else 'verified-summary'
        record['contentSha256'] = row['contentSha256']
        checkpoint(state)
        if args.verify_public:
            record['public'] = public_description(target, row, desired)
            record['phase'] = 'verified-public'
            checkpoint(state)
        print('VERIFIED', i, '/', len(selected), row['slug'], flush=True)
    verify_columns(target, plan)
    print('COMPLETE', len(selected), 'rollback' if args.rollback else 'apply/verify', flush=True)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument('--apply', action='store_true')
    mode.add_argument('--rollback', action='store_true')
    parser.add_argument('--verify-public', action='store_true')
    parser.add_argument('--limit', type=int, default=0, help='Canary size; zero selects all')
    args = parser.parse_args()
    if args.limit < 0:
        parser.error('--limit must not be negative')
    target = None
    try:
        target = proxy('new', 'https://paicoding.com/column/javaguide')['targetId']
        print('OWNED_TAB', target, flush=True)
        run(target, args)
    finally:
        if target:
            result = proxy('close?target=' + target)
            if not result.get('success'):
                raise RuntimeError('Could not close owned tab ' + target)
            print('CLOSED_TAB', target, flush=True)

if __name__ == '__main__':
    main()
