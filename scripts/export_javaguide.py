#!/usr/bin/env python3
"""Export the visible Yuque Java guide, retaining uncatalogued drafts locally."""
import argparse,json,re
from pathlib import Path
import yaml
from javaguide_lakesheet import to_markdown
from export_yuque_lakebook import read_archive,build_paths,clean_html,html_to_markdown,board_to_markdown,write_indexes,safe_name

def main():
 p=argparse.ArgumentParser();p.add_argument('lakebook',type=Path);p.add_argument('output',type=Path);a=p.parse_args()
 meta,docs=read_archive(a.lakebook);nodes=[n for n in yaml.safe_load(meta['book']['tocYml']) if n.get('type') in {'DOC','TITLE'}];paths=build_paths(nodes,a.output)
 assert not set(paths)-set(docs)
 extra=set(docs)-set(paths)
 for slug in extra:paths[slug]=a.output/'未列入目录（仅留档）'/(safe_name(docs[slug]['title'])+'-'+slug+'.md')
 report=[]
 for slug,path in paths.items():
  d=docs[slug];path.parent.mkdir(parents=True,exist_ok=True)
  body=d.get('body') or ''
  if d.get('format')=='lake':
   body=re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f]', '', body)
   body=re.sub(r'&#(?:0*([0-8]|1[124-9]|2[0-9]|3[01])|x0*([0-8bcef]|1[0-9a-f]));', '', body, flags=re.I)
  sheet_stats=[]
  if d.get('format')=='lakesheet':md,sheet_stats=to_markdown(body)
  else:md=board_to_markdown(body) if d.get('format')=='lakeboard' else html_to_markdown(clean_html(body,path,paths,{})) if body.strip() else ''
  path.write_text('# '+d['title']+'\n\n'+md+'\n')
  report.append({'sourceSlug':slug,'title':d['title'],'path':str(path.relative_to(a.output)),'format':d.get('format'),'status':d.get('status'),'inToc':slug not in extra,'bodyChars':len(md),'sheets':sheet_stats})
 write_indexes(a.output,'Java面试指南','https://www.yuque.com/itwanger/gykdzg',nodes,paths,{},len(docs))
 (a.output/'source-inventory.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
 print('EXPORTED',len(report),'CATALOGUED',len(report)-len(extra),'LOCAL_ONLY',len(extra))
 for r in report:
  if r['bodyChars']<350 or r['format']!='lake':print(json.dumps(r,ensure_ascii=False))
if __name__=='__main__':main()
