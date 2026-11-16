# 贡献指南

> **一句话**：欢迎给 LSPosed UI 模板补组件、修规范、改示例页；动手前先读 docs/DESIGN-SYSTEM.md，改规范必须三仓同步。

## 提 Issue

用 `.github/ISSUE_TEMPLATE/` 下的模板：bug / feature / config。写清复现步骤与期望行为。

## 提 Pull Request

1. 只改一个主题；UI 改动必须过 `docs/DESIGN-SYSTEM.md §13 提交前自检 20 项`。
2. 改了 UI 组件或规范条款：确认这条规则在 GSAuxiliary / WxPicLabe 也成立；跨仓共性规则要同步三仓的同名文档。
3. 提交前跑门禁：`python tools\check-docs.py` 与 `python tools\check-secrets.py`，都要退出 0。
4. 提交前跑构建：`build.cmd`（release，R8）与 `build.cmd debug`，两个变体都要过。

## 不要做的事

- 不要把业务 hook 写进模板；模板只保留 UI 骨架与示例页。
- 不要把真值（包名、密钥、节点、订阅）写进仓；占位符用 `<REPLACE-...>`。
- 不要在 docs 里写本机绝对路径。
