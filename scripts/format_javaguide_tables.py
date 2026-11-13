#!/usr/bin/env python3
"""Apply readable source column widths and horizontal scrolling to imported sheets."""
import argparse,json,urllib.request
from pathlib import Path
from export_yuque_lakebook import read_archive
from javaguide_lakesheet import to_markdown
from sync_javaguide import ROOT,DOCS,STATE,MANIFEST,api,content,sha,payload,checkpoint,rewrite_links

def main():
 p=argparse.ArgumentParser();p.add_argument('lakebook',type=Path);a=p.parse_args()
 _,docs=read_archive(a.lakebook);rows=json.loads(MANIFEST.read_text());tables=[r for r in rows if r['format']=='lakesheet']
 state=json.loads(STATE.read_text());assert state.get('verified'),'Finish base import before formatting'
 migration=json.loads((DOCS/'image-migration.json').read_text());target=None
 try:
  target=json.load(urllib.request.urlopen(urllib.request.Request('http://localhost:3456/new',data=b'https://paicoding.com/column/javaguide')))['targetId']
  for r in tables:
   rec=state['articles'][r['slug']];d=api(target,'article/detail?articleId='+rec['articleId']);assert sha(d['content'])==rec['sha256'] and d['status']==1
  state['verified']=False;checkpoint(state)
  for r in tables:
   md,_=to_markdown(docs[r['sourceSlug']]['body'])
   for old,item in migration.items():
    if item.get('cdnUrl'):md=md.replace(old,item['cdnUrl'])
   (DOCS/r['path']).write_text('# '+r['title']+'\n\n'+md+'\n')
  rewrite_links(rows)
  for r in tables:
   rec=state['articles'][r['slug']];p=payload(r,content(r));p.update({'articleId':rec['articleId'],'actionType':'post','status':1});api(target,'article/save',p)
   d=api(target,'article/detail?articleId='+rec['articleId']);assert d['content']==content(r) and d['status']==1
   rec['beforeLayoutSha256']=rec['sha256'];rec['sha256']=sha(d['content']);rec['tableLayoutVerified']=True;checkpoint(state);print('FORMATTED',r['title'],flush=True)
  state['verified']=True;state['sheetLayoutVerified']=True;checkpoint(state)
 finally:
  if target:
   result=json.load(urllib.request.urlopen('http://localhost:3456/close?target='+target));assert result.get('success');print('CLOSED_TAB',target,flush=True)
if __name__=='__main__':main()
