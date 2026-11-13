#!/usr/bin/env python3
"""Resume the authorized Yuque -> PaiCoding column 6 import; never delete lessons."""
import argparse
import hashlib
import json
import re
import urllib.request
import subprocess
import concurrent.futures
from collections import Counter
from pathlib import Path
from urllib.parse import unquote, urlsplit
from tutorial_seo import description_for

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / 'docs/技术派教程'
MANIFEST = ROOT / 'scripts/paicoding_tutorial_manifest.json'
STATE = DOCS / 'publish-state.json'
COLUMN = 6
TRUSTED_IMAGES = {'cdn.paicoding.com', 'cdn.tobebetterjavaer.com'}

def images(content):
    # Code samples (including the JWT logout/CSRF example) are not image resources.
    content = re.sub(r'(?ms)^(`{3,}|~{3,})[^\n]*\n.*?^\1[ \t]*(?:\n|$)', '', content)
    content = re.sub(r'`[^`\n]+`', '', content)
    return re.findall(r'!\[[^\]]*\]\((https?://[^\s)]+)', content) + re.findall(r'<img\b[^>]*\bsrc=["\'](https?://[^"\']+)', content, re.I)

def api(target, endpoint, body=None):
    options = {} if body is None else {'method':'POST', 'headers':{'Content-Type':'application/json'}, 'body':json.dumps(body,ensure_ascii=False)}
    code = '(async()=>await(await fetch(' + json.dumps('/api/admin/'+endpoint) + ',' + json.dumps(options) + ')).json())()'
    req = urllib.request.Request('http://localhost:3456/eval?target='+target,data=code.encode(),method='POST')
    with urllib.request.urlopen(req,timeout=180) as response:
        wrapper = json.load(response)
    if 'value' not in wrapper:
        raise RuntimeError(str(wrapper))
    result = wrapper['value']
    if result.get('status',{}).get('code') != 0:
        raise RuntimeError(f'{endpoint}: {result}')
    return result.get('result')

def remote_rows(target):
    result = api(target,'column/listColumnArticle',{'columnId':COLUMN,'pageNumber':1,'pageSize':300})
    assert len(result['list']) == int(result['total'])
    return result['list']

def rewrite_links(rows):
    paths = {(DOCS/r['path']).resolve(): 'https://paicoding.com/'+r['slug'] for r in rows if r['action']!='placeholder'}
    urls = {r['sourceUrl']: 'https://paicoding.com/'+r['slug'] for r in rows if r['action']!='placeholder'}
    for r in rows:
        p = DOCS/r['path'];content=p.read_text()
        def replace(m):
            href=m.group(1);parsed=urlsplit(href)
            if not parsed.scheme and parsed.path:
                key=(p.parent/unquote(parsed.path)).resolve()
                if key in paths:return ']('+paths[key]+('' if not parsed.fragment else '#'+parsed.fragment)
            if parsed.hostname in {'www.yuque.com','yuque.com'}:
                base='https://www.yuque.com'+parsed.path
                if base in urls:return ']('+urls[base]+('' if not parsed.fragment else '#'+parsed.fragment)
            return m.group(0)
        content=re.sub(r'\]\(([^\s)]+)',replace,content)
        p.write_text(content)

def reconcile_images(expected, actual):
    """Accept only image-URL normalization with byte-identical image bodies."""
    if expected == actual:return actual
    before,after=images(expected),images(actual)
    assert len(before)==len(after), 'Saved image count changed'
    changes={a:b for a,b in zip(before,after) if a!=b}
    candidate=expected
    for a,b in sorted(changes.items(),key=lambda item:len(item[0]),reverse=True):
        candidate=candidate.replace(a,b)
    assert candidate==actual, 'Saved article text changed beyond image URLs'
    record=DOCS/'server-image-normalization.json'
    cache=json.loads(record.read_text()) if record.exists() else {}
    def verify(pair):
        a,b=pair
        assert urlsplit(a).hostname in TRUSTED_IMAGES and urlsplit(b).hostname in TRUSTED_IMAGES
        if cache.get(a,{}).get('cdnUrl')==b:return a,cache[a]
        def digest(u):
            blob=subprocess.check_output(['curl','--http1.1','-fLsS','--retry','1','--max-time','30',u])
            return hashlib.sha256(blob).hexdigest()
        first,second=digest(a),digest(b)
        assert first==second, 'Server-normalized image bytes differ: '+a
        return a,{'cdnUrl':b,'sha256':first}
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        for a,result in pool.map(verify,changes.items()):cache[a]=result
    record.write_text(json.dumps(cache,ensure_ascii=False,indent=2)+'\n')
    return actual

def check_images(content):
    bad=[u for u in images(content) if urlsplit(u).hostname not in TRUSTED_IMAGES or 'saveError' in u]
    if bad:raise RuntimeError(f'{len(bad)} unmigrated images: {bad[:2]}')

def main():
    p=argparse.ArgumentParser();p.add_argument('--target',required=True);p.add_argument('--publish',action='store_true');args=p.parse_args()
    rows=json.loads(MANIFEST.read_text());active=[r for r in rows if r['action'] not in ['placeholder','alias']]
    for r in active:description_for(r['slug'],r['shortTitle'])
    rewrite_links(rows)
    for r in active:check_images((DOCS/r['path']).read_text())
    assert len({r['slug'] for r in active})==len(active)
    target=args.target
    state=json.loads(STATE.read_text()) if STATE.exists() else {'articles':{}}
    def checkpoint():STATE.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n')
    existing=remote_rows(target)
    state.setdefault('original_articles',existing)
    state.setdefault('original_groups',api(target,'column/listGroups?columnId=6'))
    state.setdefault('original_column',api(target,'column/detail?columnId=6'))
    if 'original_content_hashes' not in state:
        state['original_content_hashes']={a['articleId']:hashlib.sha256(api(target,'article/detail?articleId='+a['articleId'])['content'].encode()).hexdigest() for a in state['original_articles']}
    checkpoint()
    if not args.publish:
        print('READY',len(active),'lessons; use --publish to apply');return
    categories=list(dict.fromkeys(r['category'] for r in active))
    groups={g['title']:g for g in api(target,'column/listGroups?columnId=6')}
    for i,name in enumerate(categories,1):
        if name not in groups:
            api(target,'column/saveColumnGroup',{'columnId':COLUMN,'parentGroupId':0,'title':name,'section':i})
            groups={g['title']:g for g in api(target,'column/listGroups?columnId=6')}
    by_slug={a['urlSlug']:a for a in existing}
    original_ids={a['articleId'] for a in state['original_articles']}
    for r in active:
        slug=r['slug'];old=by_slug.get(slug)
        if r['action']=='preserve':
            assert old and old['articleId']==r['articleId'],slug
            article_id=old['articleId']
        else:
            found=api(target,'article/list',{'urlSlug':slug,'pageNumber':1,'pageSize':100})['list']
            found=[a for a in found if a.get('urlSlug')==slug]
            if found and slug not in state['articles']:
                raise RuntimeError('Unexpected existing slug; review before adopting: '+slug)
            content=re.sub(r'^# [^\n]+\n+','',(DOCS/r['path']).read_text(),count=1)
            payload={'title':r['title'],'shortTitle':r['shortTitle'],'urlSlug':slug,'categoryId':1,'tagIds':[150], 'summary':description_for(slug,r['shortTitle']),'content':content,'cover':'','articleType':'COLUMN','source':2,'sourceUrl':r['sourceUrl'],'actionType':'save','status':0,'readType':3}
            if not found:
                state['articles'][slug]={'phase':'creating','sourceSlug':r['sourceSlug']};checkpoint()
                api(target,'article/save',payload)
                found=api(target,'article/list',{'urlSlug':slug,'pageNumber':1,'pageSize':100})['list']
                found=[a for a in found if a.get('urlSlug')==slug]
            assert len(found)==1,slug
            article_id=found[0]['articleId']
            detail=api(target,'article/detail?articleId='+article_id)
            check_images(detail['content'])
            content=reconcile_images(content,detail['content'])
            payload['content']=content
            local_path=DOCS/r['path']
            heading=local_path.read_text().split('\n',1)[0]
            local_path.write_text(heading+'\n\n'+content)
            state['articles'][slug].update({'articleId':article_id,'phase':'verified-draft'});checkpoint()
            if detail['status']!=1:
                payload.update({'articleId':article_id,'actionType':'post','status':1})
                api(target,'article/save',payload)
        detail=api(target,'article/detail?articleId='+article_id)
        check_images(detail['content']);assert detail['status']==1
        if r['action']=='create':
            normalized=reconcile_images(content,detail['content'])
            local_path=DOCS/r['path'];heading=local_path.read_text().split('\n',1)[0]
            local_path.write_text(heading+'\n\n'+normalized)
        if article_id in original_ids:
            assert hashlib.sha256(detail['content'].encode()).hexdigest()==state['original_content_hashes'][article_id], 'Original article changed: '+slug
        group_id=groups[r['category']]['groupId']
        relation={'columnId':COLUMN,'articleId':article_id,'groupId':group_id,'sort':old['sort'] if old else 999,'shortTitle':r['shortTitle'],'read':old['readType'] if old else 0,'previewPercent':old['previewPercent'] if old else 0}
        if old:relation['id']=old['id']
        api(target,'column/saveColumnArticle',relation)
        state['articles'][slug]={'articleId':article_id,'sourceSlug':r['sourceSlug'],'phase':'published','category':r['category'],'images':len(images(detail['content'])),'sha256':hashlib.sha256(detail['content'].encode()).hexdigest(),'preserved':article_id in original_ids}
        checkpoint();print('PUBLISHED',slug,flush=True)
    for first,second in zip(categories,categories[1:]):
        api(target,'column/moveColumnArticleOrGroup',{'columnId':COLUMN,'moveGroupId':groups[second]['groupId'],'targetGroupId':groups[first]['groupId'],'movePosition':1})
    for name in categories:
        for r in reversed([r for r in active if r['category']==name]):
            api(target,'column/moveColumnArticleOrGroup',{'columnId':COLUMN,'moveArticleId':state['articles'][r['slug']]['articleId'],'targetGroupId':groups[name]['groupId'],'movePosition':0})
    final=remote_rows(target);assert len(final)==len(active)
    assert [a['urlSlug'] for a in final]==[r['slug'] for r in active], 'Lesson ordering mismatch'
    finalmap={a['articleId']:a for a in final}
    for old in state['original_articles']:
        now=finalmap[old['articleId']]
        for key in ['urlSlug','readType','previewPercent','title']:assert now[key]==old[key],(key,old['articleId'])
    for r in active:
        detail=api(target,'article/detail?articleId='+state['articles'][r['slug']]['articleId']);check_images(detail['content'])
        assert detail['shortTitle']==r['shortTitle'] and detail['status']==1
        if r['action']=='create':
            local_path=DOCS/r['path'];local=local_path.read_text();heading=local.split('\n',1)[0]
            body=re.sub(r'^# [^\n]+\n+','',local,count=1)
            body=reconcile_images(body,detail['content']);local_path.write_text(heading+'\n\n'+body)
            state['articles'][r['slug']]['sha256']=hashlib.sha256(body.encode()).hexdigest()
        else:
            assert hashlib.sha256(detail['content'].encode()).hexdigest()==state['original_content_hashes'][detail['articleId']]
    column=api(target,'column/detail?columnId=6')
    fields=['columnId','column','urlSlug','author','introduction','cover','state','section','nums','type','freeStartTime','freeEndTime']
    payload={k:column.get(k) for k in fields};payload['nums']=len(final)
    api(target,'column/saveColumn',payload)
    state['final_articles']=final;state['final_groups']=api(target,'column/listGroups?columnId=6');state['verified']=True;checkpoint()
    report=['# 技术派教程同步验收','','日期：2026-11-12','','- 来源：https://www.yuque.com/itwanger/az7yww','- 专栏：https://paicoding.com/column/paicoding',f'- 源文档 106 篇；线上 {len(final)} 节；原有 23 节保留，新增 {len(final)-23} 节。','- 3 篇“敬请期待”保留在本地，未发布；工程篇 MVC 跳转合并至基础篇正文。','- 原有 URL、长标题、正文及阅读权限保留；菜单短标题单独设置。原有两篇通用示例作为补充教程，与语雀项目实战文分别保留。','','| 分类 | 小节数 |','| --- | --- |']
    report += [f'| {name} | {sum(a["groupName"]==name for a in final)} |' for name in categories]
    report += ['','| 章节 | 短标题 | 地址 | 图片引用数 |','| --- | --- | --- | --- |']
    report += [f'| {r["title"]} | {r["shortTitle"]} | https://paicoding.com/{r["slug"]} | {state["articles"][r["slug"]]["images"]} |' for r in active]
    (DOCS/'PUBLISH-REPORT.md').write_text('\n'.join(report)+'\n')
    print('VERIFIED',len(final),flush=True)

if __name__=='__main__':main()
