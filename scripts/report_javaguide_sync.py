#!/usr/bin/env python3
"""Write local indices and an evidence-backed final import report."""
import json,collections
from pathlib import Path
from urllib.parse import quote
ROOT=Path(__file__).resolve().parents[1];DOCS=ROOT/'docs/Java面试指南'
rows=json.loads((ROOT/'scripts/javaguide_manifest.json').read_text());active=[r for r in rows if r['action'] in ['create','preserve']]
labels={'create':'新增','preserve':'保留线上完整正文','placeholder':'空白文档，仅留档','archive':'未列入语雀目录，仅留档'}
summary=['# Java 面试指南目录',''];mapping=['# 来源、长短标题与文章地址','','| 分类 | 原文标题 | 菜单短标题 | 技术派地址 | 处理方式 |','| --- | --- | --- | --- | --- |']
last=None
for r in rows:
 cat=' / '.join(r.get('category',[])) or '未列入目录（仅留档）'
 if cat!=last:summary+=['','## '+cat,''];last=cat
 label=r.get('shortTitle',r['title']);summary.append('- ['+label+']('+quote(r['path'],safe='/')+')'+('（仅留档）' if r['action'] in ['placeholder','archive'] else ''))
 target='[阅读](https://paicoding.com/'+r['slug']+')' if r['action'] in ['create','preserve'] else '暂不发布'
 mapping.append('| '+cat+' | ['+r['title'].replace('|','\\|')+']('+r['sourceUrl']+') | '+label+' | '+target+' | '+labels[r['action']]+' |')
(DOCS/'SUMMARY.md').write_text('\n'.join(summary)+'\n');(DOCS/'URL-MAPPING.md').write_text('\n'.join(mapping)+'\n')
(DOCS/'README.md').write_text('''# Java 面试指南

来源：[语雀 Java 面试指南](https://www.yuque.com/itwanger/gykdzg) · 网站：[技术派专栏](https://paicoding.com/column/javaguide)

导出日期：2026-11-12。语雀目录有 277 篇，导出包另有 2 篇未列入目录的草稿，均已本地留档。可发布 270 节，7 篇空白“未写完”文档仅留档。原有简历文章保留网站完整正文、长标题、地址和阅读权限。

- [分类目录](SUMMARY.md)
- [来源、长短标题与地址对应表](URL-MAPPING.md)
- [来源缺失说明](SOURCE-NOTES.md)
- [发布验收报告](PUBLISH-REPORT.md)

14 篇电子表格转换为 53 个 HTML 表格，保留单元格内容、超链接、图片、合并区域及合理列宽，并支持横向滚动。空白格式单元格不作为正文导出。历史招聘表、薪资信息的年份和原文观点原样保留，不代表实时招聘信息。

图片映射与文件哈希保存在 `image-migration.json`；原有网站内容备份在 `original-site-snapshot.json`；发布断点与线上正文哈希见 `publish-state.json`。

重新同步时，先导出、生成清单，再转存图片、验证转换，最后运行发布脚本。已有发布正文如与本地不同，脚本会停下供核对，不会自动覆盖。

```bash
python3 scripts/export_javaguide.py /absolute/path/Java面试指南.lakebook docs/Java面试指南
python3 scripts/prepare_javaguide_manifest.py /absolute/path/Java面试指南.lakebook
python3 scripts/migrate_javaguide_images.py
python3 scripts/normalize_javaguide_links.py
python3 scripts/validate_javaguide_articles.py /absolute/path/Java面试指南.lakebook
python3 scripts/validate_javaguide_export.py /absolute/path/Java面试指南.lakebook
python3 scripts/sync_javaguide.py --publish
python3 scripts/report_javaguide_sync.py
```

发布脚本使用已有 Chrome 登录态及本地 CDP Proxy，只操作 Java 面试指南专栏，并在结束或异常时关闭自身创建的标签。
''')
notes=['# 来源缺失与保留策略','','- “如果平滑度过35岁危机”中的 1 张旧表情图源站 image.dbbqb.com 中断连接；命令行与浏览器均无法访问。正文保留原始链接并标注暂不可用，详见 unavailable-images.json。','- 7 篇“未写完”文档正文为空，只在本地保留，不发布空课。','- 导出包额外包含“互联网公司薪资一览表”（空白）和旧版“如何写好简历”草稿，均未列入语雀目录，仅留档。','- 目录中的“如何写好简历”只是技术派跳转页，保留技术派原有完整正文。','- “24 届秋招信息汇总表”的“内推码”工作表中，有 5 张图片处于源端 `needUpload` 状态，只有 Windows 临时文件路径；导出包不含图片文件。对应单元格标记为“【原表图片未上传】”，未伪造替代图。','','| 工作表 | 行（从 1 计） | 列（从 1 计） |','| --- | --- | --- |','| 内推码 | 45 | 4 |','| 内推码 | 54 | 4 |','| 内推码 | 55 | 4 |','| 内推码 | 59 | 4 |','| 内推码 | 77 | 2 |']
(DOCS/'SOURCE-NOTES.md').write_text('\n'.join(notes)+'\n')
state_path=DOCS/'publish-state.json'
if state_path.exists():
 state=json.loads(state_path.read_text())
 if state.get('verified'):
  final=state['final_articles'];imgs=json.loads((DOCS/'image-migration.json').read_text());sheet=json.loads((DOCS/'sheet-validation.json').read_text())
  report=['# Java 面试指南同步验收','','日期：2026-11-12','','- 来源：https://www.yuque.com/itwanger/gykdzg','- 专栏：https://paicoding.com/column/javaguide',f'- 线上共 {len(final)} 节：保留 1 节，新增 {len(final)-1} 节。','- 10 个主分类、1 个支付对账子分类；左侧菜单短标题与正文长标题分别维护。','- 原有文章正文、长标题、URL 和阅读权限保持一致。',f'- {sum(bool(v.get("cdnUrl")) for v in imgs.values())} 个外部图片地址转存成功，并通过 CDN 回读 SHA-256 校验。',f'- 线上图片引用共 {sum(v["images"] for v in state["articles"].values())} 处，均使用技术派或二哥 CDN。',f'- 53 个工作表、{sum(r["verifiedCells"] for r in sheet):,} 个非空单元格通过内容、链接和图片比对。','- 所有线上正文重新读取并校验 SHA-256、发布状态、短标题、分类顺序。','- 270 个文章地址均通过未登录访问检查，页面标题与清单一致。','- 前台抽查了专栏目录、冲刺计划、16 工作表的大表格、Agent Skills 配图及原有简历页；宽表格支持横向滚动。','- 本次临时浏览器标签已关闭，未创建独立自动化浏览器进程。','- 7 篇空白文档与 2 篇未列入目录草稿仅留档；原表 5 张未上传图片及 1 张暂不可用的外链图见 [来源缺失说明](SOURCE-NOTES.md)。','','| 分类 | 节数 |','| --- | --- |']
  report += ['| '+cat+' | '+str(count)+' |' for cat,count in collections.Counter(' / '.join(r['category']) for r in active).items()]
  report+=['','| 菜单标题 | 地址 | 图片引用数 |','| --- | --- | --- |']
  report += [f'| {r["shortTitle"]} | https://paicoding.com/{r["slug"]} | {state["articles"][r["slug"]]["images"]} |' for r in active]
  (DOCS/'PUBLISH-REPORT.md').write_text('\n'.join(report)+'\n');print('REPORT',len(final))
