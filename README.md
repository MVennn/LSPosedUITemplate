<div align="center">

<img src="https://raw.githubusercontent.com/MVennn/LSPosedUITemplate/main/assets/logo.png" width="128" alt="LSPosedUITemplate">

# LSPosedUITemplate

**LSPosed 模块 · UI 模板**（非官方，与任何第三方应用厂商无隶属、合作或授权关系）

Consistent UI. Zero business logic.

![CI](https://github.com/MVennn/LSPosedUITemplate/actions/workflows/ci.yml/badge.svg) ![源码仓库](https://img.shields.io/badge/源码仓库-GitHub-181717?logo=github&logoColor=white) ![问题反馈](https://img.shields.io/badge/问题反馈-Issues-181717?logo=github&logoColor=white) ![Android](https://img.shields.io/badge/Android-8.1%2B-3DDC84?logo=android&logoColor=white) ![LSPosed](https://img.shields.io/badge/LSPosed-API%20102-5C6BC0) ![License](https://img.shields.io/badge/License-Apache--2.0-blue)

</div>

---

## 这是什么

给开发者套 UI 用的 **LSPosed 模块模板**：从 WxPicLabe 剥离「微信发图带文件名」业务后，保留整套 UI 组件、页面骨架与设计规范，供新 LSPosed 项目直接套用，保证 UI 与 GSAuxiliary / WxPicLabe 一致。模板包含一个演示开关行 / 列表行 / 新建弹层 / 清空确认的 `DemoScreen`，拿到后换成自己的功能页即可。

保留的组件与骨架：

| 类别 | 内容 | 用途 |
| --- | --- | --- |
| 开关与行 | `GsSwitch` / `Widgets` | 开关行、入口行、列表行等规范行型 |
| 弹层 | `AppDialogSpec` / `AppDialogHost` / `AppDialogWidgets` | 新建弹层、确认弹层、输入框 |
| 视觉与动效 | `AppIcons` / `Dock` / `Glass` / `SystemBars` / `AnimCurves` / `LiquidPhysics` | 图标、底栏、玻璃材质、系统栏、动画与物理 |
| 偏好与架构 | `PrefState` / `ModulePreferences` / `ConfigKeys` | 偏好状态与 remote/local 双后端 |
| 页面骨架 | `SettingsActivity` → `AppTheme` → `MainScreen` → `HomeScaffold` → Tab 页 | 设置入口到各 Tab 的现成骨架 |

怎么套用：改包名 / applicationId / 应用名、删 `DemoScreen` 换功能页、加 ConfigKeys 开关键，步骤见 [docs/TEMPLATE-GUIDE.md](https://github.com/MVennn/LSPosedUITemplate/blob/main/docs/TEMPLATE-GUIDE.md)；UI 规范见 [docs/DESIGN-SYSTEM.md](https://github.com/MVennn/LSPosedUITemplate/blob/main/docs/DESIGN-SYSTEM.md)，架构见 [docs/ARCHITECTURE.md](https://github.com/MVennn/LSPosedUITemplate/blob/main/docs/ARCHITECTURE.md)。

模板不采集数据、不含宿主 Hook 业务，只提供可复用的设置界面骨架。

## 环境要求

- 安卓 8.1+（minSdk 27），targetSdk 36，compileSdk 37
- **较新的 LSPosed**（现代 Xposed API 102，`minApiVersion` 101）。过旧的 LSPosed、太极 / LSPatch / EdXposed 等实现会静默不加载
- 构建：JDK 17、Gradle 8.11.1、AGP 8.10.1；Windows 下仓根 `build.cmd`

## 安装

模板本身不是面向终端用户的功能模块，安装用于预览 UI：

1. [Releases](https://github.com/MVennn/LSPosedUITemplate/releases) 下载最新 APK 并安装
2. LSPosed 管理器 → 模块，勾选「LSPosedUITemplate」
3. 作用域按需勾选；模板没有实际 Hook 目标，勾选只用于加载模块入口
4. 打开模块 App，查看 `DemoScreen` 的开关行、列表行与弹层效果
5. 套用到新项目时按 [docs/TEMPLATE-GUIDE.md](https://github.com/MVennn/LSPosedUITemplate/blob/main/docs/TEMPLATE-GUIDE.md) 改造

### 从源码构建

在 Windows 仓根执行 `build.cmd`，**默认出 release 包**（开 R8）；debug 变体同样关 `debuggable` + 开 R8，要真正可调试的包再显式退回：

```cmd
build.cmd          :: release（默认），产物就是模板包
build.cmd debug    :: debug，与 release 同口径构建（R8 + 关 debuggable）
gradlew :app:assembleDebug -Ptpl.debuggable=true -Ptpl.minifyDebug=false   :: 真·可调试包
```

产物分别是 `app\build\outputs\apk\release\app-release.apk` 与 `app\build\outputs\apk\debug\app-debug.apk`；构建参数与产物校验见 [docs/BUILD.md](https://github.com/MVennn/LSPosedUITemplate/blob/main/docs/BUILD.md)。

## 反馈

模板的 UI 组件、规范落地或套用流程有问题，到 [Issues](https://github.com/MVennn/LSPosedUITemplate/issues) 反馈。请带上：

- 模板版本号、安卓版本、机型
- 复现步骤，以及预期为什么样、实际什么样
- 若是 UI 布局问题，附截图与页面所在文件

## 免责声明

本项目仅供学习与个人使用。

- 不收集、不上传、不存储任何个人信息，不含统计分析、广告或数据回传组件
- 只提供 UI 模板与示例页面，不含任何宿主应用的业务逻辑
- 使用第三方模块带来的账号或设备后果由使用者自行承担
- 完全免费开源，不直接或间接获取经济利益

完整声明见 [LICENSE](https://github.com/MVennn/LSPosedUITemplate/blob/main/LICENSE)。

## 开源鸣谢

本模板建立在这些开源项目之上：

| 项目 | 用途 |
| --- | --- |
| [**LSPosed**](https://github.com/LSPosed/LSPosed) · [**libxposed**](https://central.sonatype.com/artifact/io.github.libxposed/api) | 模块框架与现代 Xposed API（API 102） |
| [**Miuix**](https://github.com/miuix-kotlin-multiplatform/miuix) | 设置界面的 UI 组件库（小米 HyperOS 风格 Compose） |
| [**AndroidLiquidGlass**](https://github.com/Kyant0/AndroidLiquidGlass)（`io.github.kyant0:backdrop`） | 液态玻璃顶栏 / 悬浮底栏 / 弹窗的折射与模糊效果 |
| [**Jetpack Compose**](https://developer.android.com/jetpack/compose) · [**AndroidX**](https://developer.android.com/jetpack/androidx) | 全部界面实现的基础设施 |
| [**Kotlin**](https://github.com/JetBrains/kotlin) | 模块与设置界面的实现语言 |
| [**Material Icons**](https://github.com/google/material-design-icons) | 界面图标的矢量路径数据来源（按 path 自绘，不引入图标库） |
| [**HyperIsland**](https://github.com/1812z/HyperIsland) | 关于页的布局与动效来源（MIT，非代码依赖，实现为本项目自写） |

## 许可证

本项目以 [Apache License 2.0](https://github.com/MVennn/LSPosedUITemplate/blob/main/LICENSE) 发布。
第三方组件的版权与许可证声明见 [NOTICE](https://github.com/MVennn/LSPosedUITemplate/blob/main/NOTICE)（随 APK 分发）。
---
