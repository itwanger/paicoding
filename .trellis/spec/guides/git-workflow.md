# Git Workflow（分支 / 提交 / PR 节奏）

> 本仓库的长期 git 规则。**所有任务的 check 环节必须核对本文件**。
> 确立于 2026-08-05（chore/trellis-bootstrap 收尾时），单人仓库、自审 PR 模式。

---

## 分支策略

- **`main` 只通过 PR 合入，不直接 commit。**
- 上游同步：`git fetch upstream && git merge upstream/main`（upstream = `itwanger/paicoding`），merge 也走分支+PR。
- 每个 Trellis 任务对应一个分支，命名 `<type>/<任务简名>`：
  - `chore/trellis-bootstrap`、`feat/ai-summary-pipeline`、`feat/rag-search`、`chore/local-deploy` ...
  - type 与 Conventional Commits 的 type 对齐（feat / fix / docs / chore / refactor / perf / test）。
- fork 基线：tag `upstream-base`（`4b55260a`）。评估「我的增量」一律 `git diff upstream-base`。

## 任务生命周期与 git 的绑定

```
任务开始（task.py start）
  └─ 从 main 切任务分支 git checkout -b <type>/<任务简名>
开发中
  └─ 小步提交：一个逻辑变更一个 commit（禁止大杂烩 commit）
任务归档前（/trellis:finish-work）
  └─ 推送分支 git push origin <branch>
  └─ agent 产出 PR 标题与描述文案
开发者在 GitHub 开 PR → 自审 → 合并
  └─ PR 合并后任务才算完成（task.json 记录 pr_url）
```

## Commit 规范

- **Conventional Commits**：`<type>(<scope>): <subject>`
  - subject：英文，一行，祈使句。
  - body：中文，简述「是什么、为什么」（开发者逐个 commit review 学习用）。
- scope 常用值：`trellis`、`spec`、`task`、`git`、业务域名（`article`、`chatai`、`search`...）。
- 提交前 agent 必须把 `git status` 全量文件清单 + 每个文件归入哪个 commit 列给开发者确认，**确认后才执行**。

## Check 环节核对项

- [ ] 当前不在 `main` 上直接提交
- [ ] 分支名符合 `<type>/<任务简名>`
- [ ] 每个 commit 是单一逻辑变更，符合 Conventional Commits，body 有中文说明
- [ ] 分支已推送，PR 文案已交付
- [ ] 任务标记完成前 PR 已合并
