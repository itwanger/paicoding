#!/usr/bin/env python3
"""Resume the authorized Java interview guide import; preserve existing lessons.

Run after export and image migration. With --publish, creates drafts, verifies
exact content, publishes, organizes the column and re-reads every saved article.
Owns and always closes its browser tab; never closes user or other task tabs.
"""
import argparse,hashlib,html,json,re,urllib.request
from pathlib import Path
from urllib.parse import unquote,urlsplit
from sync_paicoding_tutorials import api,images,check_images
from tutorial_seo import description_for
ROOT=Path(__file__).resolve().parents[1]
DOCS=ROOT/'docs/Java面试指南'
MANIFEST=ROOT/'scripts/javaguide_manifest.json'
STATE=DOCS/'publish-state.json'
COLUMN=12

def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def checkpoint(state):STATE.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n')
def content(row):return re.sub(r'^# [^\n]+\n+','',(DOCS/row['path']).read_text(),count=1)
def remote_rows(target):
 r=api(target,'column/listColumnArticle',{'columnId':COLUMN,'pageNumber':1,'pageSize':500})
 assert len(r['list'])==int(r['total'])
 return r['list']
def groups(target):
 result={}
 def walk(items,parent=()):
  for g in items:
   key=parent+(g['title'],);result[key]=g
   walk(g.get('children') or [],key)
 walk(api(target,'column/listGroups?columnId=12'))
 return result

def rewrite_links(rows):
 bypath={(DOCS/r['path']).resolve():r for r in rows}
 bysource={r['sourceUrl']:r for r in rows}
 def dest(row):return 'https://paicoding.com/'+row['slug'] if row['action'] in ['create','preserve'] else row['sourceUrl']
 for r in rows:
  if r['action']=='preserve':continue
  p=DOCS/r['path'];s=p.read_text()
  def href(url):
   parsed=urlsplit(html.unescape(url));target=None
   if not parsed.scheme and parsed.path:target=bypath.get((p.parent/unquote(parsed.path)).resolve())
   elif parsed.hostname in ['www.yuque.com','yuque.com']:target=bysource.get('https://www.yuque.com'+parsed.path.rstrip('/'))
   if target:return dest(target)+('#'+parsed.fragment if parsed.fragment else '')
   return html.unescape(url)
  s=re.sub(r'\]\(([^\s)]+)',lambda m:']('+href(m[1]),s)
  s=re.sub(r'(<a\b[^>]*\bhref=")([^"]+)',lambda m:m[1]+html.escape(href(m[2]),quote=True),s)
  p.write_text(s)

def payload(row,body):
 return {'title':row['title'],'shortTitle':row['shortTitle'],'urlSlug':row['slug'],'categoryId':11,'tagIds':[],
 'summary':description_for(row['slug'],row['shortTitle']),'content':body,'cover':'','articleType':'COLUMN','source':2,'sourceUrl':row['sourceUrl'],
 'actionType':'save','status':0,'readType':3}

def exact_find(target,slug):
 result=api(target,'article/list',{'urlSlug':slug,'pageNumber':1,'pageSize':100})
 return [a for a in result['list'] if a.get('urlSlug')==slug]

def run(target,publish):
 rows=json.loads(MANIFEST.read_text());active=[r for r in rows if r['action'] in ['create','preserve']]
 for r in active:description_for(r['slug'],r['shortTitle'])
 rewrite_links(rows)
 assert len({r['slug'] for r in active})==len(active)
 for r in active:check_images(content(r))
 original=json.loads((DOCS/'original-site-snapshot.json').read_text())
 state=json.loads(STATE.read_text()) if STATE.exists() else {'columnId':COLUMN,'articles':{}}
 assert state['columnId']==COLUMN
 existing=remote_rows(target);by_slug={a['urlSlug']:a for a in existing}
 original_ids=set(original['details'])
 for a in existing:
  assert a['articleId'] in original_ids or state['articles'].get(a['urlSlug'],{}).get('articleId')==a['articleId'], 'Unexpected live lesson'
 for aid,d in original['details'].items():
  now=api(target,'article/detail?articleId='+aid)
  for key in ['content','title','urlSlug','readType','status']:assert now[key]==d[key],('Existing article drift',aid,key)
 for r in active:
  if r['action']=='preserve':continue
  found=exact_find(target,r['slug'])
  if found:
   assert len(found)==1 and r['slug'] in state['articles'],'Unexpected slug collision: '+r['slug']
   assert state['articles'][r['slug']].get('articleId',found[0]['articleId'])==found[0]['articleId']
 checkpoint(state)
 print('PREFLIGHT',len(active),'lessons',flush=True)
 if not publish:return
 categories=list(dict.fromkeys(tuple(r['category']) for r in active))
 allkeys=[]
 for key in categories:
  for i in range(1,len(key)+1):
   if key[:i] not in allkeys:allkeys.append(key[:i])
 gs=groups(target)
 for key in allkeys:
  if key not in gs:
   parent=gs[key[:-1]]['groupId'] if len(key)>1 else 0
   sibling_order=[k for k in allkeys if k[:-1]==key[:-1]].index(key)+1
   api(target,'column/saveColumnGroup',{'columnId':COLUMN,'parentGroupId':parent,'title':key[-1],'section':sibling_order})
   gs=groups(target)
 for index,r in enumerate(active,1):
  slug=r['slug'];old=by_slug.get(slug);body=content(r)
  previous=state['articles'].get(slug,{})
  if previous.get('phase')=='published' and old and sha(body)==previous.get('sha256'):
   assert old['articleId']==previous['articleId'] and old['groupId']==gs[tuple(r['category'])]['groupId'] and old['shortTitle']==r['shortTitle']
   continue
  if r['action']=='preserve':
   assert old and old['articleId']==r['articleId'];aid=r['articleId']
  else:
   rec=state['articles'].setdefault(slug,{'phase':'planned','sourceSlug':r['sourceSlug']})
   found=exact_find(target,slug);p=payload(r,body)
   if not found:
    rec['phase']='creating';checkpoint(state)
    api(target,'article/save',p)
    found=exact_find(target,slug)
   assert len(found)==1,slug
   aid=found[0]['articleId'];rec['articleId']=aid;checkpoint(state)
   d=api(target,'article/detail?articleId='+aid)
   if d['content']!=body and d['status']==0:
    def without_image_urls(text):
     for url in images(text):text=text.replace(url,'<IMAGE_URL>')
     return text
    assert without_image_urls(d['content'])==without_image_urls(body), 'Unexpected non-image draft change: '+slug
    rec['previousDraftSha256']=sha(d['content']);checkpoint(state)
    p['articleId']=aid;api(target,'article/save',p);d=api(target,'article/detail?articleId='+aid)
   assert d['content']==body, 'Saved body mismatch: '+slug
   assert d['title']==r['title'] and d['shortTitle']==r['shortTitle']
   check_images(d['content'])
   if d['status']!=1:
    rec['phase']='verified-draft';checkpoint(state)
    p.update({'articleId':aid,'status':1,'actionType':'post'});api(target,'article/save',p)
  d=api(target,'article/detail?articleId='+aid);assert d['status']==1;check_images(d['content'])
  assert d['content']==(original['details'][aid]['content'] if aid in original_ids else body)
  rel={'columnId':COLUMN,'articleId':aid,'groupId':gs[tuple(r['category'])]['groupId'],'sort':old['sort'] if old else 999,'shortTitle':r['shortTitle'],'read':old['readType'] if old else 0,'previewPercent':old['previewPercent'] if old else 0}
  if old:rel['id']=old['id']
  api(target,'column/saveColumnArticle',rel)
  state['articles'][slug]={'articleId':aid,'sourceSlug':r['sourceSlug'],'phase':'published','category':r['category'],'images':len(images(d['content'])),'sha256':sha(d['content']),'preserved':aid in original_ids}
  checkpoint(state);print('PUBLISHED',index,'/',len(active),slug,flush=True)
 for parent in dict.fromkeys(k[:-1] for k in allkeys):
  siblings=[k for k in allkeys if k[:-1]==parent]
  for first,second in zip(siblings,siblings[1:]):
   api(target,'column/moveColumnArticleOrGroup',{'columnId':COLUMN,'moveGroupId':gs[second]['groupId'],'targetGroupId':gs[first]['groupId'],'movePosition':1})
 for key in categories:
  for r in reversed([r for r in active if tuple(r['category'])==key]):
   api(target,'column/moveColumnArticleOrGroup',{'columnId':COLUMN,'moveArticleId':state['articles'][r['slug']]['articleId'],'targetGroupId':gs[key]['groupId'],'movePosition':0})
 final=remote_rows(target);assert len(final)==len(active)
 assert {a['urlSlug'] for a in final}=={r['slug'] for r in active}
 for key in categories:
  assert [a['urlSlug'] for a in final if a['groupId']==gs[key]['groupId']]==[r['slug'] for r in active if tuple(r['category'])==key]
 for old in original['articles']['list']:
  now=next(a for a in final if a['articleId']==old['articleId'])
  for key in ['urlSlug','readType','previewPercent','title']:assert now[key]==old[key],key
 for i,r in enumerate(active,1):
  rec=state['articles'][r['slug']];d=api(target,'article/detail?articleId='+rec['articleId']);check_images(d['content'])
  assert d['status']==1 and d['shortTitle']==r['shortTitle'] and sha(d['content'])==rec['sha256'],r['slug']
  if i%25==0:print('VERIFIED',i,'/',len(active),flush=True)
 col=api(target,'column/detail?columnId=12')
 fields=['columnId','column','urlSlug','author','introduction','cover','state','section','nums','type','freeStartTime','freeEndTime','readmeContent']
 for key in ['urlSlug','type','freeStartTime','freeEndTime','introduction','cover']:assert col[key]==original['column'][key]
 p={k:col.get(k) for k in fields};p['nums']=len(final);api(target,'column/saveColumn',p)
 state.update({'final_articles':final,'final_groups':api(target,'column/listGroups?columnId=12'),'verified':True});checkpoint(state)
 print('COMPLETE',len(final),flush=True)

def main():
 p=argparse.ArgumentParser();p.add_argument('--publish',action='store_true');a=p.parse_args();target=None
 try:
  req=urllib.request.Request('http://localhost:3456/new',data=b'https://paicoding.com/column/javaguide',method='POST')
  target=json.load(urllib.request.urlopen(req))['targetId'];print('OWNED_TAB',target,flush=True)
  run(target,a.publish)
 finally:
  if target:
   closed=json.load(urllib.request.urlopen('http://localhost:3456/close?target='+target));assert closed.get('success'),closed;print('CLOSED_TAB',target,flush=True)
if __name__=='__main__':main()
