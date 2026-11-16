# AGENTS.md —— LSPosedUITemplate 仓级规则

> **一句话**：这是给新 LSPosed 项目套 UI 用的模板仓，只放 UI 骨架、设计规则与示例页，不放任何业务 hook；改本仓等于改以后所有套用它的项目的门面。

---

## 1. 本仓是什么

- 从 GSAuxiliary / WxPicLabe 抽取的 LSPosed 模块 UI 模板：Compose + Miuix 0.9.3 全套组件、`docs/DESIGN-SYSTEM.md` 规范、一个 `DemoScreen` 示例页。
- 业务（微信发图带文件名等）已全部剥离；包名 / 应用名 / SP 名 / scope 均为占位符。
- 目标：新项目拿来改包名和示例页即可，UI 与规范自动与两仓一致。

---

## 2. 硬规则

- **不写业务 hook 进模板**：`TemplateModule` 保持空壳，业务注入由套用方在自己的项目里加。
- **规范即正本**：UI 改动必须过 `docs/DESIGN-SYSTEM.md §13` 自检；§0–§13 与 GSAuxiliary / WxPicLabe 同文，改一处要同步三处。
- **路径纪律**：文档、脚本、配置只写仓库相对路径，不写本机绝对路径。
- **凭据**：真值外置，占位符用 `<REPLACE-...>`；`local.properties`、`.gradle-home/` 等本机产物已 gitignore。

---

## 3. 验证

- 文档门禁：仓根 `python tools\check-docs.py`，退出码 0。
- 凭据门禁：仓根 `python tools\check-secrets.py`，退出码 0。
- 构建：`build.cmd`（release，R8）与 `build.cmd debug`，都要 BUILD SUCCESSFUL。
- 改构建配置后必须实跑一次，贴 APK 体积。

---

## 4. 套用

- 从 README 的「这是什么」读起；改名 / 换页 / 加配置键 / 填 Xposed 元数据见 `docs/TEMPLATE-GUIDE.md`。
- `ui/` 下组件是骨架不要动，示例页整页替换；不要破坏 DESIGN-SYSTEM 的间距、图标、弹层与键盘让位规则。
