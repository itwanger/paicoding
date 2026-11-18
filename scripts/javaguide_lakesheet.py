"""Lossless displayed-cell export for Yuque's compressed lakesheet snapshots."""
import json,zlib,html,re
from javaguide_sprint import first_month_note

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
 return linked_text(str(v), cell.get('r', []))


def linked_text(value, runs):
    def text(s):
        return escape(s).replace('\n', '<br>')

    links = sorted((r for r in runs if r.get('l')), key=lambda r: r['t'])
    if not links:
        return text(value)
    # Yuque stores inclusive UTF-16 positions. Some pasted URLs have stale offsets.
    offsets = {0: 0}
    units = 0
    for index, char in enumerate(value, 1):
        units += 2 if ord(char) > 0xffff else 1
        offsets[units] = index
    out = []
    cursor = 0
    for run in links:
        url = run['l']
        start = offsets.get(run['t'])
        end = offsets.get(run['e'] + 1)
        if start is None or end is None or value[start:end] != url:
            # Recover the literal URL only if it overlaps this run. Named links
            # keep their original label, even when the same URL appears elsewhere.
            matches = [m for m in re.finditer(re.escape(url), value)
                       if m.start() >= cursor and start is not None and m.end() > start
                       and (end is None or m.start() < end)]
            if len(matches) == 1:
                start, end = matches[0].span()
        if start is None or end is None or not cursor <= start < end:
            raise ValueError(f'Invalid link range {run["t"]}..{run["e"]}')
        out.extend((text(value[cursor:start]), '<a href="' + escape(url) + '">'
                    + text(value[start:end]) + '</a>'))
        cursor = end
    return ''.join(out) + text(value[cursor:])


def to_markdown(body, source_slug=None):
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
  sprint = source_slug == 'vosmggr38sbz11uc'
  occupied = {r for r,c,v in filled}
  feedback = []
  for r in range(maxrow+1):
   if r not in occupied and not any((r,c) in covered or (r,c) in merges for c in range(maxcol+1)):
    continue
   row = data.get(str(r), {})
   first = row.get('0', {})
   if sprint and sheet['name'] == '笔试的公司' and re.match(r'^\d+、', str(first.get('v',''))) and not any(
       c != '0' and cell.get('v') not in [None, ''] for c,cell in row.items()):
    feedback.append(cell_html(first))
    continue
   out.append('<tr>')
   for c in range(maxcol+1):
    if (r,c) in covered:continue
    attrs=''
    if (r,c) in merges:
     nr,nc=merges[(r,c)];attrs=f' rowspan="{nr}" colspan="{nc}"'
    cell = row.get(str(c), {})
    note = first_month_note(cell) if sprint and sheet['name']=='第一个月' and (r,c) in merges and merges[(r,c)][1]==maxcol+1 else None
    value = note or cell_html(cell)
    out.append('<td'+attrs+' style="word-break:normal;overflow-wrap:anywhere;">'+value+'</td>')
   out.append('</tr>')
  if feedback:
   items = ''.join('<p>'+entry+'</p>' for entry in feedback)
   out.append(f'<tr><td colspan="{maxcol+1}"><p><strong>球友笔面试反馈</strong></p>'+items+'</td></tr>')
  out+=['</table>','</div>','']
  stats.append({'sheet':sheet['name'],'rows':maxrow+1,'columns':maxcol+1,'nonEmptyCells':len(filled),'missingImages':sum(isinstance(v.get('v'),dict) and v['v'].get('class')=='image' and not v['v'].get('src') for r,c,v in filled)})
 return '\n'.join(out),stats
