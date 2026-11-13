#!/usr/bin/env python3
"""Check canonical article URLs without an authenticated browser."""
import json,re,html,urllib.request,concurrent.futures
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];DOCS=ROOT/'docs/Java面试指南'
rows=[r for r in json.loads((ROOT/'scripts/javaguide_manifest.json').read_text()) if r['action'] in ['create','preserve']]
def check(r):
 url='https://paicoding.com/'+r['slug']
 try:
  with urllib.request.urlopen(url,timeout=30) as response:
   page=response.read(32768).decode('utf-8',errors='replace');title=re.search(r'<title>(.*?)</title>',page,re.S);title=html.unescape(title[1]) if title else ''
   return {'url':url,'status':response.status,'finalUrl':response.url,'title':title,'ok':response.status==200 and r['title'] in title}
 except Exception as e:return {'url':url,'ok':False,'error':str(e)}
with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:results=list(pool.map(check,rows))
(DOCS/'public-url-validation.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n');print('URLS',len(results),'OK',sum(r['ok'] for r in results))
for r in results:
 if not r['ok']:print(r)
assert all(r['ok'] for r in results)
