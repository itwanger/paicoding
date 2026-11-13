#!/usr/bin/env python3
import json,subprocess,sys,re,argparse
from pathlib import Path
from lxml import html
sys.path.insert(0,str(Path(__file__).resolve().parent));from sync_paicoding_tutorials import images
from export_yuque_lakebook import read_archive
p=argparse.ArgumentParser();p.add_argument('lakebook',type=Path);a=p.parse_args();_,docs=read_archive(a.lakebook);src={'docs':docs};rows=json.loads(Path('scripts/javaguide_manifest.json').read_text());report=[]
def count(n,t):
 if isinstance(n,dict):return int(n.get('t')==t)+sum(count(v,t) for v in n.values())
 if isinstance(n,list):return sum(count(v,t) for v in n)
 return 0
for r in rows:
 if r['format']!='lake' or r['action']=='preserve':continue
 d=src['docs'][r['sourceSlug']];body=d['body'];s=(Path('docs/Java面试指南')/r['path']).read_text()
 if not body.strip():continue
 h=html.fragment_fromstring(body,create_parent='div')
 ast=json.loads(subprocess.check_output(['pandoc','-f','gfm','-t','json'],input=s.encode()))
 unavailable_path=Path('docs/Java面试指南/unavailable-images.json')
 unavailable=sum(item['document']==r['path'] for item in json.loads(unavailable_path.read_text())) if unavailable_path.exists() else 0
 rec={'knownUnavailableImages':unavailable,'slug':r['sourceSlug'],'title':r['title'],'sourceImages':len(h.xpath('.//img')),'exportImages':len(images(s)),'sourceCode':len(h.xpath('.//pre')),'exportCode':count(ast,'CodeBlock'),'sourceTables':len(h.xpath('.//table')),'exportTables':count(ast,'Table'),'rawHtml':count(ast,'RawBlock')}
 report.append(rec)
 if rec['sourceImages']>rec['exportImages']+unavailable or rec['sourceCode']!=rec['exportCode']:print('REVIEW',rec,flush=True)
Path('docs/Java面试指南/export-validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n');print('PARSED',len(report))
