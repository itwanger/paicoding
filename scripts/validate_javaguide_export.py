#!/usr/bin/env python3
"""Verify decoded table cell contents, links, and images against the source archive."""
import argparse,json,collections,re
from pathlib import Path
from lxml import html
from urllib.parse import unquote
from export_yuque_lakebook import read_archive
from javaguide_lakesheet import decode,cell_html

def canon(markup):
 node=html.fragment_fromstring(markup,create_parent='div')
 return (' '.join(node.text_content().split()),tuple(unquote(u) for u in node.xpath('.//a/@href')),tuple(node.xpath('.//img/@src')))
def main():
 p=argparse.ArgumentParser();p.add_argument('lakebook',type=Path);a=p.parse_args();_,docs=read_archive(a.lakebook)
 root=Path(__file__).resolve().parents[1]/'docs/Java面试指南';inv=json.loads((root/'source-inventory.json').read_text());report=[]
 for r in inv:
  if r['format']!='lakesheet':continue
  # Compare before image migration or undo only recorded byte-identical URL substitutions.
  content=(root/r['path']).read_text()
  records=json.loads((root/'image-migration.json').read_text()) if (root/'image-migration.json').exists() else {}
  for old,item in records.items():
   if item.get('cdnUrl'):content=content.replace(item['cdnUrl'],old)
  parsed=html.fragment_fromstring(content,create_parent='div');tables=parsed.xpath('.//table');sheets=decode(docs[r['sourceSlug']]['body']);nonempty=[]
  for sh in sheets:
   cells=[c for row in sh.get('data',{}).values() for c in row.values() if c.get('v') not in [None,'']]
   if cells:nonempty.append((sh,cells))
  assert len(tables)==len(nonempty),r['title']
  for table,(sh,cells) in zip(tables,nonempty):
   expected=collections.Counter(v for c in cells for v in [canon(cell_html(c))] if v != ('',(),()))
   actual=collections.Counter(canon(html.tostring(c,encoding='unicode')) for c in table.xpath('.//td') if c.text_content().strip() or c.xpath('.//img'))
   assert expected==actual,(r['title'],sh['name'],expected-actual,actual-expected)
   report.append({'sourceSlug':r['sourceSlug'],'sheet':sh['name'],'verifiedCells':len(cells)})
 (root/'sheet-validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n');print('VERIFIED_SHEETS',len(report),'CELLS',sum(r['verifiedCells'] for r in report))
if __name__=='__main__':main()
