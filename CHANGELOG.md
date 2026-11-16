# 更新日志

本文件采用 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 格式。

## [Unreleased]

## [1.0.0] - 2026-11-16

### Added

- 从 GSAuxiliary / WxPicLabe 抽取 LSPosed 模块 UI 模板：保留 Compose + Miuix 0.9.3 全套 UI 组件与设计规范（docs/DESIGN-SYSTEM.md §0–§13 逐字同文）。
- 剥离「微信发图带文件名」业务 hook 与全部 ImgLabel 业务页面；包名 / 应用名 / SP 名 / scope 全部占位符化。
- 提供 DemoScreen 示例页：开关行 + 列表逐条删除 + 新建输入弹层 + 清空确认弹层；保留键盘让位（rememberKeyboardOverlapPx）。
