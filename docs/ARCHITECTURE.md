# 架构说明 · ARCHITECTURE

> **一句话**：四层按「配置 / 模块自身 / 界面 / 业务」切开，`ui` 只依赖公共契约不引用 `feature` 业务类，偏好经 `ModulePreferences` 双后端统一读写。

## 1. 目录结构

```
app/src/main/java/io/github/MVennn/LsposedUITemplate/
├── <模块入口类>.java       # Xposed 入口，注册 Hook（新项目替换）
├── core/                   # 配置键与日志，不依赖 UI 与业务
│   ├── ConfigKeys.kt
│   └── GLog.kt
├── app/                    # 模块自身进程：偏好、框架桥接、本地化
│   ├── TemplateApp.kt
│   ├── ModulePreferences.kt
│   ├── FrameworkBridge.kt
│   └── LocaleHelper.kt
├── ui/                     # Compose 设置界面，只认公共契约
│   ├── SettingsActivity.kt / Routing.kt / Screens.kt
│   ├── GsSwitch.kt / Widgets.kt / AppSearchField.kt
│   ├── AppDialogSpec.kt / AppDialogHost.kt / AppDialogWidgets.kt
│   ├── AppIcons.kt / Dock.kt / Glass.kt / SystemBars.kt
│   ├── AnimCurves.kt / LiquidPhysics.kt
│   ├── PrefState.kt
│   └── AboutHero.kt / AboutTab.kt / MarkdownText.kt / BackupUiState.kt
└── feature/                # 业务实现，新项目在此加自己的功能（模板已剥离）
```

## 2. 各层职责

| 层 | 职责 | 依赖方向 |
| --- | --- | --- |
| `core/` | 配置键常量、日志封装；被 UI 与 Hook 侧共同引用 | 不依赖 `ui` / `feature` |
| `app/` | 模块自身 Application、偏好双后端、框架桥接与本地化 | 依赖 `core`；可被 `ui` 使用 |
| `ui/` | 设置入口、页面骨架、可复用组件与弹层 | 依赖 `core` / `app` 的契约；**不引用 `feature` 业务类** |
| `feature/` | 具体 Hook 业务实现；模板中已剥离 | 依赖 `core` / Xposed API；不反向依赖 `ui` |

## 3. 为什么 ui 不引用 feature

`ui` 只展示偏好状态、调用配置键，不知道 Hook 具体怎么实现。这样新项目换功能时只替换 `feature/` 与 `DemoScreen`，整套界面组件无需改动；反过来若 `ui` 直接引用业务类，界面就绑死在某个功能上，且业务类里常带宿主上下文，会让设置界面在模块自身进程里不可运行。`core/ConfigKeys.kt` 与 `app/ModulePreferences.kt` 是两层之间唯一的契约。

## 4. ModulePreferences 双后端

| 后端 | 使用环境 | 实现 |
| --- | --- | --- |
| remote | LSPosed 加载模块、进程内可拿到框架偏好时 | 走 `io.github.libxposed:service` 提供的偏好接口，模块 App 与 Hook 侧共享同一组键 |
| local | 模块 App 独立运行（未加载 / 调试预览） | 本地 `SharedPreferences`，保证界面仍可打开与编辑 |

两后端对外暴露同一组 `ConfigKeys`，`ui/PrefState.kt` 只面向这个抽象；新项目加键时只需在 `core/ConfigKeys.kt` 定义一次，不要在组件里分别写两套读写。

## 5. 现代 Xposed API 的位置

`app/build.gradle.kts`：

- `compileOnly("io.github.libxposed:api:102.0.0")`
- `compileOnly("io.github.libxposed:annotation:1.0.0")`
- `implementation("io.github.libxposed:service:102.0.0")`

API 与 annotation 是 **compileOnly**：编译期可见，运行期由 LSPosed 框架注入，不能打进 APK；service 是运行时依赖。仓库源在 `settings.gradle.kts` 的 `maven("https://api.xposed.info/repository/repository")`。`xposedminversion` 用 `101`，低于此的框架会静默不加载。

## 6. 与规范的对应

组件清单、页面骨架、行型与自检项见 [DESIGN-SYSTEM.md](DESIGN-SYSTEM.md)；套用步骤见 [TEMPLATE-GUIDE.md](TEMPLATE-GUIDE.md)；版本与构建见 [BUILD.md](BUILD.md)。

---
