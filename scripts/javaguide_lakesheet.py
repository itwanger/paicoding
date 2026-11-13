"""Lossless displayed-cell export for Yuque's compressed lakesheet snapshots."""
import json,zlib,html,re

def escape(value,quote=True):
 return html.escape(re.sub(r"[\x00-\x08\x0b\x0c\x0e-\x1f]", "", str(value)),quote=quote)

def decode(body):
 return json.loads(zlib.decompress(json.loads(body)['sheet'].encode('latin1')))

def cell_html(cell):
 v=cell.get('v','')
 if v is None:v=''
 if isinstance(v,dict):
  if v.get('class')=='link':return '<a href="'+escape(v['url'],quote=True)+'">'+escape(str(v.get('text',v['url'])))+'</a>'
  if v.get('class')=='image':
   if v.get('src'):return '<img src="'+escape(v['src'],quote=True)+'" alt="'+escape(v.get('name',''),quote=True)+'">'
   return '【原表图片未上传】'
  raise ValueError('Unknown cell type: '+str(v))
 text=escape(str(v)).replace('\n','<br>')
 links=list(dict.fromkeys(r['l'] for r in cell.get('r',[]) if r.get('l')))
 if links:text+=' '+' '.join('<a href="'+escape(u,quote=True)+'">参考链接'+str(i+1)+'</a>' for i,u in enumerate(links))
 return text

def to_markdown(body):
 sheets=decode(body);out=[];stats=[]
 for sheet in sheets:
  data=sheet.get('data',{});filled=[(int(r),int(c),v) for r,row in data.items() for c,v in row.items() if v.get('v') not in [None,'']]
  if not filled:continue
  maxrow=max(r for r,c,v in filled);maxcol=max(c for r,c,v in filled)
  widths=[max(90,min(400,round(sheet.get('columns',{}).get(str(c),{}).get('size',160)))) for c in range(maxcol+1)]
  table_width=sum(widths)
  out+=['## '+sheet['name'],'','<div style="max-width:100%;overflow-x:auto;">',f'<table style="width:{table_width}px;min-width:{table_width}px;table-layout:fixed;">','<colgroup>']
  out.extend(f'<col style="width:{w}px;">' for w in widths)
  out.append('</colgroup>')
  merges={};covered=set()
  for m in sheet.get('mergeCells',{}).values():
   r,c=m['row'],m['col'];nr=min(m['rowCount'],maxrow-r+1);nc=min(m['colCount'],maxcol-c+1)
   if nr<1 or nc<1:continue
   merges[(r,c)]=(nr,nc)
   covered.update((rr,cc) for rr in range(r,r+nr) for cc in range(c,c+nc) if (rr,cc)!=(r,c) and data.get(str(rr),{}).get(str(cc),{}).get('v') in [None,''])
  for r in range(maxrow+1):
   out.append('<tr>')
   for c in range(maxcol+1):
    if (r,c) in covered:continue
    attrs=''
    if (r,c) in merges:
     nr,nc=merges[(r,c)];attrs=f' rowspan="{nr}" colspan="{nc}"'
    out.append('<td'+attrs+' style="word-break:normal;overflow-wrap:anywhere;">'+cell_html(data.get(str(r),{}).get(str(c),{}))+'</td>')
   out.append('</tr>')
  out+=['</table>','</div>','']
  stats.append({'sheet':sheet['name'],'rows':maxrow+1,'columns':maxcol+1,'nonEmptyCells':len(filled),'missingImages':sum(isinstance(v.get('v'),dict) and v['v'].get('class')=='image' and not v['v'].get('src') for r,c,v in filled)})
 return '\n'.join(out),stats
