# 模板套用指南 · TEMPLATE-GUIDE

> **一句话**：拿到模板后先改身份与元数据，概览 / 设置 / 声明页可直接复用，再删 `DemoScreen` 换成自己的功能页，全程只动「示例区」不动「UI 骨架」。

本指南面向开发者。模板的目标是让你在新 LSPosed 项目里复用一套与 GSAuxiliary / WxPicLabe 一致的 UI，接入点集中在少数几个文件。

## 1. 改名与换身份

| 项 | 文件 | 要改成什么 |
| --- | --- | --- |
| 包名 / 命名空间 | `app/build.gradle.kts` 的 `namespace` 与 `defaultConfig.applicationId` | 新项目包名，例如 `<REPLACE-APPLICATION-ID>` |
| Kotlin 源码目录 | `app/src/main/java/io/github/MVennn/LsposedUITemplate/` | 按新包名移动目录，并同步包声明 |
| 应用名 | `app/src/main/res/values/strings.xml` 的 `app_name` | 新项目显示名 |
| 项目名 | `settings.gradle.kts` 的 `rootProject.name` | 新项目名 |
| 模块入口类 | 新项目的 Java 入口类（模板示例为 `TemplateModule`，路径随源码包） | Hook 入口改名并同步所有引用 |
| 徽标 | `assets/logo.png` 与 README 徽章 URL | 新项目仓库名 |

包名改完后全局搜索旧包名 `io.github.MVennn.LsposedUITemplate` 确认无残留；文档里的路径示例也要同步。

## 2. 哪些文件不要动（UI 骨架）

下列文件承载规范落地，**不要改外观与结构**，只改包名 / 引用：

- `app/src/main/java/<PKG>/ui/SettingsActivity.kt`、`Routing.kt`、`Screens.kt` 的骨架部分（`AppTheme` → `MainScreen` → `HomeScaffold` → Tab 页）
- `app/src/main/java/<PKG>/ui/GsSwitch.kt`、`Widgets.kt`、`AppSearchField.kt`、`AboutHero.kt`、`AboutTab.kt`、`MarkdownText.kt`、`BackupUiState.kt`
- `app/src/main/java/<PKG>/ui/AppDialogSpec.kt`、`AppDialogHost.kt`、`AppDialogWidgets.kt`
- `app/src/main/java/<PKG>/ui/AppIcons.kt`、`Dock.kt`、`Glass.kt`、`SystemBars.kt`、`AnimCurves.kt`、`LiquidPhysics.kt`、`PrefState.kt`
- `app/src/main/res/values` 下与上述组件绑定的样式 / 颜色 / 尺寸 token

理由：这些文件对应 [DESIGN-SYSTEM.md](DESIGN-SYSTEM.md) §0–§13 的行型、弹层、图标、颜色、动画规则；改了外观就会与 GSAuxiliary / WxPicLabe 分叉，丢掉「同一套 UI」的保证。

## 3. 哪些文件要替换（示例区）

- `DemoScreen`（`ui/Screens.kt` 内）：演示开关行 / 列表行 / 新建弹层 / 清空确认，删掉换成新项目的功能页；照 DESIGN-SYSTEM §3 选行型、§5 选弹层。
- `core/ConfigKeys.kt`：演示用的偏好键改名或替换。
- `app/FrameworkBridge.kt`、`app/LocaleHelper.kt`：示例桥接与本地化，按新项目需要保留或替换。
- `app/src/main/assets/xposed/` 下的模块元数据：见第 5 节。

## 4. 加开关键：ConfigKeys 与 ModulePreferences

1. 在 `core/ConfigKeys.kt` 增加常量，键名全大写 + 连字符，例如 `const val KEY_MY_FEATURE = "my_feature"`。
2. 设置页用 `ui/PrefState.kt` 的偏好状态包装读写（具体函数名以源码为准），默认值给 `false`。
3. 偏好持久化走 `app/ModulePreferences.kt`：LSPosed 环境用 remote 后端（模块与宿主共享偏好），独立运行的模块 App 用 local 后端（本地 SharedPreferences）；两后端统一暴露同一组键，不要绕开它直接写文件。
4. Hook 侧在入口类里通过 `ModulePreferences` 读同一个键，确保设置页改动即时生效。
5. 加完键后按 DESIGN-SYSTEM §13 自检表逐项勾一遍，特别是 §5.3 键盘让位：使用 `rememberKeyboardOverlapPx`，**禁用 `WindowInsets.ime`**，让位量 = 根视图高 −（可见区 bottom − 根视图 y）。

## 5. 入口类与 Xposed 元数据

入口类：Java 写法，包名与新项目包一致，实现 `IXposedHookLoadPackage`（现代 API 102 的 `io.github.libxposed.api`），在 `handleLoadPackage` 里注册目标进程回调；模块 App 自身进程只跑 UI，不做 Hook。

`app/src/main/assets/xposed/`（模板里实际目录以源码为准）三份元数据：

| 文件 | 内容 | 怎么填 |
| --- | --- | --- |
| `module.prop` | `id=<REPLACE-MODULE-ID>`、`name=<REPLACE-MODULE-NAME>`、`version=<REPLACE-VERSION>`、`versionCode=<REPLACE-VERSION-CODE>`、`author=<REPLACE-AUTHOR>`、`description=<REPLACE-DESCRIPTION>` | id 用小写字母 / 数字 / 点 / 下划线，不写空格；versionCode 整数递增 |
| `java_init.list` | 一行一个入口类全限定名 | 写新项目入口类全名，例如 `<REPLACE-PACKAGE>.<REPLACE-MODULE-CLASS>` |
| `scope.list` | 一行一个目标包名 | 填新项目要 Hook 的应用包名；没有实际目标时留空并说明模板预览用途 |

`AndroidManifest.xml` 的 `xposedmodule` / `xposedminversion` / `xposeddescription` meta-data 与 `module.prop` 保持一致；`xposedminversion` 用 `101`（现代 API 102 的最低加载要求）。

## 6. 套用后自检

- [ ] 包名 / applicationId / 应用名 / 项目名四处都已改，无旧名残留
- [ ] `DemoScreen` 已替换，新页面行型从 DESIGN-SYSTEM §3 选
- [ ] 新增 ConfigKeys 已同时接入设置页与 Hook 侧
- [ ] Xposed 元数据三文件与 Manifest meta-data 一致，入口类全名正确
- [ ] 按 DESIGN-SYSTEM §13 的 20 项自检表逐项通过
- [ ] 真机安装后 UI 与 GSAuxiliary / WxPicLabe 观感一致（字体、间距、弹层、系统栏）

构建与产物校验见 [BUILD.md](BUILD.md)。

---
