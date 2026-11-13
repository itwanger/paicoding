#!/usr/bin/env python3
"""Prepare reviewed classification and stable URL mappings after export."""
import json,re,sys,argparse
import yaml
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from sync_paicoding_tutorials import images
from export_yuque_lakebook import read_archive
p=argparse.ArgumentParser();p.add_argument('lakebook',type=Path);args=p.parse_args()
meta,docs=read_archive(args.lakebook);src={'nodes':yaml.safe_load(meta['book']['tocYml'])}
root=Path(__file__).resolve().parents[1]/'docs/Java面试指南';inv=json.loads((root/'source-inventory.json').read_text());by={r['sourceSlug']:r for r in inv};nodes=src['nodes'];byid={n.get('uuid'):n for n in nodes};shorts=json.loads((Path(__file__).parent/'javaguide_short_titles.json').read_text());rows=[]
for n in nodes:
 if n.get('type')!='DOC':continue
 r=by[n['url']].copy();anc=[];parent=n.get('parent_uuid')
 while parent:
  a=byid[parent];anc.insert(0,a['title']);parent=a.get('parent_uuid')
 if not anc:anc=[n['title']]
 title=r['title'];short=re.sub(r'[（(](?:已?完结|未写完)[）)]','',title);short=re.sub('[🔥👍🏻💉]','',short).strip('？?！!。 ')
 short=shorts.get(n['url'],short)
 r.update({'category':anc,'shortTitle':short,'slug':'javaguide-'+n['url'],'sourceUrl':'https://www.yuque.com/itwanger/gykdzg/'+n['url'],'action':'create' if r['bodyChars'] else 'placeholder'})
 if n['url']=='grvrtx':r.update({'slug':'javaguide-resume-write','articleId':'2602216071786505','action':'preserve','shortTitle':'如何写好简历？'})
 rows.append(r)
original=json.loads((root/'original-site-snapshot.json').read_text())
for r in rows:
 if r['action']=='preserve':
  d=original['details'][r['articleId']];r['title']=d['title'];(root/r['path']).write_text('# '+d['title']+'\n\n'+d['content'])
for r in inv:
 if not r['inToc']:r.update({'action':'archive','sourceUrl':'https://www.yuque.com/itwanger/gykdzg/'+r['sourceSlug']});rows.append(r)
(Path(__file__).parent/'javaguide_manifest.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
urls={u for r in rows for u in images((root/r['path']).read_text())};print('ROWS',len(rows),'PUBLISH',sum(r['action'] in ['create','preserve'] for r in rows),'IMAGES',len(urls),'EXTERNAL',sum(not any(h in u for h in ['cdn.paicoding.com/','cdn.tobebetterjavaer.com/']) for u in urls))
