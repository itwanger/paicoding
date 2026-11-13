#!/usr/bin/env python3
"""Copy existing article images byte-for-byte to Paicoding OSS with checkpoints."""
import concurrent.futures
import hashlib
import json
import subprocess
import tempfile
import urllib.parse
from pathlib import Path
import re
DOCS = Path(__file__).resolve().parents[1] / 'docs/Java面试指南'
def rows():
    return json.loads((Path(__file__).parent / 'javaguide_manifest.json').read_text())
def images(content):
    # Code samples (including the JWT logout/CSRF example) are not image resources.
    content = re.sub(r'(?ms)^(`{3,}|~{3,})[^\n]*\n.*?^\1[ \t]*(?:\n|$)', '', content)
    content = re.sub(r'`[^`\n]+`', '', content)
    return re.findall(r'!\[[^\]]*\]\((https?://[^\s)]+)', content) + re.findall(r'<img\b[^>]*\bsrc=["\'](https?://[^"\']+)', content, re.I)

RECORD = DOCS / 'image-migration.json'
UPLOAD = '/Users/itwanger/.codex/skills/article-image-cdn/scripts/upload-image-to-paicoding-oss.js'
ENV = '/Users/itwanger/Documents/GitHub/toBeBetterJavaer/.env'

def main():
    records = json.loads(RECORD.read_text()) if RECORD.exists() else {}
    paths = [DOCS / r['path'] for r in rows()]
    urls = list(dict.fromkeys(u for p in paths for u in images(p.read_text()) if not any(h in u for h in ['cdn.paicoding.com/'])))
    pending = [u for u in urls if not records.get(u,{}).get('cdnUrl')]
    print('UNIQUE_IMAGES',len(urls),'PENDING',len(pending),flush=True)
    def upload(url):
        key = hashlib.sha256(url.encode()).hexdigest()[:24]
        resolved_url = 'https://' + url[len('http://'):] if url.startswith('http://cdn.tobebetterjavaer.com/') else url
        parsed = urllib.parse.urlparse(url)
        if parsed.hostname == 'www.yuque.com' and parsed.path == '/api/filetransfer/images':
            resolved_url = urllib.parse.parse_qs(parsed.query)['url'][0]
            if urllib.parse.urlparse(resolved_url).hostname != 'files.mdnice.com':
                raise ValueError('Unexpected nested image host')
        ext = Path(urllib.parse.urlparse(url).path).suffix.lower()
        if ext not in ['.png','.jpg','.jpeg','.gif','.webp','.svg','.avif']:
            ext = '.png'
        with tempfile.TemporaryDirectory(prefix='javaguide-image-') as temp:
            image_path = Path(temp) / (key + ext)
            subprocess.run(['curl','--http1.1','-fL','--retry','1','--max-time','60','-sS'] + (['-A','Mozilla/5.0','-e','https://www.zhihu.com/'] if 'zhimg.com' in resolved_url else []) + [resolved_url,'-o',str(image_path)],check=True,capture_output=True)
            data = image_path.read_bytes()
            valid = data.startswith((b'\x89PNG', b'GIF8', b'\xff\xd8', b'RIFF')) or b'<svg' in data[:1000] or b'ftypavif' in data[:40]
            if not valid:
                raise ValueError('Source URL did not return an image')
            result = subprocess.run(['node',UPLOAD,'--image',str(image_path),'--env',ENV,'--prefix','javaguide','--name',key,'--json'],check=True,capture_output=True,text=True,timeout=60)
            result = json.loads(result.stdout)
            cdn = result['cdnUrl']
            check = subprocess.run(['curl','-fL','--max-time','20','-sS',cdn],check=True,capture_output=True)
            assert hashlib.sha256(check.stdout).digest() == hashlib.sha256(image_path.read_bytes()).digest(), 'CDN byte mismatch'
            return {'cdnUrl':cdn,'sha256':hashlib.sha256(check.stdout).hexdigest(),'bytes':len(check.stdout),'resolvedSource':resolved_url}
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:
        futures = {pool.submit(upload,u):u for u in pending}
        for i,future in enumerate(concurrent.futures.as_completed(futures),1):
            url=futures[future]
            try:
                records[url]=future.result()
            except Exception as e:
                detail = getattr(e, 'stderr', '') or str(e)
                if isinstance(detail, bytes): detail = detail.decode(errors='replace')
                records[url]={'error':detail[:500]}
                print('FAILED',url,str(e)[:80],flush=True)
            RECORD.write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n')
            if i % 25 == 0:
                print('IMAGES',i,'/',len(pending),flush=True)
    for path in paths:
        content=path.read_text()
        for url in sorted(set(images(content)), key=len, reverse=True):
            if records.get(url,{}).get('cdnUrl'):
                content=content.replace(url,records[url]['cdnUrl'])
        path.write_text(content)
    print('COMPLETE',sum(bool(v.get('cdnUrl')) for v in records.values()),'FAILED',sum(not v.get('cdnUrl') for v in records.values()),flush=True)

if __name__=='__main__':
    main()
