#!/usr/bin/env python3
"""Repair verified source-relative links and byte-identical CDN aliases."""
import json,sys,hashlib,subprocess,concurrent.futures,re
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent));from sync_paicoding_tutorials import images
root=Path(__file__).resolve().parents[1]/'docs/Java面试指南';rows=json.loads((Path(__file__).parent/'javaguide_manifest.json').read_text());prefix='https://cdn.tobebetterjavaer.com/paicoding/';urls={u for r in rows if r['action']=='create' for u in images((root/r['path']).read_text()) if u.startswith(prefix)}
def verify(u):
 new=u.replace(prefix,'https://cdn.paicoding.com/paicoding/');datas=[subprocess.check_output(['curl','--http1.1','-fL','--max-time','30','-sS',v]) for v in [u,new]];assert datas[0]==datas[1],u
 return {'sourceUrl':u,'cdnUrl':new,'sha256':hashlib.sha256(datas[0]).hexdigest()}
with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:records=list(pool.map(verify,urls))
(root/'cdn-alias-validation.json').write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n')
repairs=[]
for r in rows:
 if r['action']!='create':continue
 p=root/r['path'];s=p.read_text();old=s
 if (root/'unavailable-images.json').exists():
  failed=json.loads((root/'unavailable-images.json').read_text())
  migration=json.loads((root/'image-migration.json').read_text())
  for item in failed:
   u=item['sourceUrl']
   if item['document']==r['path'] and not migration.get(u,{}).get('cdnUrl'):
    s=s.replace('![]('+u+')','[原文配图暂无法加载（保留原始链接）]('+u+')')
 for item in records:s=s.replace(item['sourceUrl'],item['cdnUrl'])
 s=re.sub(r'\[(@(?:Component|resource|Transactional))\]\(/users/\d+\)',r'`\1`',s)
 s=s.replace('](/creation/subject/','](https://www.nowcoder.com/creation/subject/')
 s=s.replace('](/users/','](https://www.nowcoder.com/users/')
 for rel in ['/2018/06/09/tips-of-anti-spider-in-fe/','/2018/07/26/basic-of-rsa-big-prime-number/']:s=s.replace(']('+rel,'](https://coolcao.com'+rel)
 if s!=old:repairs.append(r['sourceSlug']);p.write_text(s)
(root/'link-repair-record.json').write_text(json.dumps({'articles':repairs,'nowcoder':'Relative topic links restored using original article source at www.nowcoder.com; Java annotation mentions converted to inline code','coolcao':['https://coolcao.com/2018/06/09/tips-of-anti-spider-in-fe/','https://coolcao.com/2018/07/26/basic-of-rsa-big-prime-number/']},ensure_ascii=False,indent=2)+'\n');print('CDN_ALIASES',len(records),'REPAIRED_ARTICLES',repairs)
