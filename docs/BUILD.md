# 构建说明 · BUILD

> **一句话**：版本钉死、构建走仓根 `build.cmd`，release 是默认产物，两变体可互覆安装，release 资产校验以 GitHub Release 摘要为准。

## 1. 工具链与版本

| 项 | 版本 | 位置 |
| --- | --- | --- |
| JDK | 17 | 本机安装，`compileOptions` 与 Kotlin `jvmTarget` 都锁 17 |
| Gradle | 8.11.1 | `gradle/wrapper/gradle-wrapper.properties`，`build.cmd` 优先用已下载的 8.11.1 |
| AGP | 8.10.1 | `build.gradle.kts` 的 `com.android.application` |
| Kotlin | 2.4.0 | `build.gradle.kts` 的 Kotlin Android / Compose 插件 |
| compileSdk | 37 | `app/build.gradle.kts` |
| targetSdk | 36 | `app/build.gradle.kts` 的 `defaultConfig` |
| minSdk | 27（Android 8.1） | `app/build.gradle.kts` 的 `defaultConfig` |
| Miuix | 0.9.3 | `app/build.gradle.kts`；0.9.4+ 要求 AGP ≥ 9.1，别升 |

SDK 解析顺序：环境变量 `ANDROID_SDK_ROOT` / `ANDROID_HOME`（须存在）→ 仓内 `.work/android-sdk` → `local.properties` 的 `sdk.dir`。`build.cmd` 不写死任何绝对路径，保持纯 ASCII（cmd.exe 按 OEM 代码页读 `.cmd`）。

## 2. build.cmd 用法

在 Windows 仓根执行：

```cmd
build.cmd          :: release（默认），开 R8，实机与日常测试都装这个
build.cmd debug    :: debug，与 release 同口径（关 debuggable + 开 R8）
gradlew :app:assembleDebug -Ptpl.debuggable=true -Ptpl.minifyDebug=false   :: 真·可调试包
```

`build.cmd` 把 `GRADLE_USER_HOME` / `TMP` / `TEMP` / `ANDROID_USER_HOME` 重定向到工程内（`.gradle-home/`、`.tmp/`、`.android-home/`），不需要管理员权限；这些目录都是本机产物，不进库。变体之后的参数原样转发给 Gradle。

debug 变体默认 `isDebuggable=false` + R8，即与 release 同性能口径；要断点 / `run-as` / 可读堆栈才用上面的 `-P` 参数退回可调试构建。release 默认 R8 + 资源收缩，签名用 debug keystore（`signingConfigs.debug`），仅用于模板预览。

## 3. 产物路径

- release：`app/build/outputs/apk/release/app-release.apk`
- debug：`app/build/outputs/apk/debug/app-debug.apk`

两变体包名相同、签名相同，**可以互相覆盖安装**；覆盖前不需要卸载，LSPosed 模块勾选状态会保留。想对比两版 UI 时先备份当前 APK。

## 4. release 资产 sha256 的两个坑

1. **注释也进 dex**：release 开 R8，源码注释不会进 dex，但构建链路里任何「注释版」产物（如从中间态复制出的文件）与正式 release 的 sha256 不同。校验时只取 `app/build/outputs/apk/release/app-release.apk` 这一份最终产物，别拿中间态或 debug 包代替。
2. **构建未退时读到中间态**：若前一次构建尚未结束就复制 APK，可能复制到写入中的文件，sha256 会漂移。等 `build.cmd` 打印 `[ok] APK:` 且退出码 0 后再计算摘要。

发布 Release 时，资产摘要**以 GitHub Release 页面显示的 digest 为准**：本地 `sha256sum` / `Get-FileHash` 与远端不一致时，以 gh release 的 digest 为最终判据，并确认上传的是同一份最终产物。

## 5. 拉取后验证

```cmd
python tools\check-docs.py
gradlew :app:assembleRelease --no-daemon --console=plain
```

文档门禁退出码 0 才算合格；构建以 `BUILD SUCCESSFUL` 与 APK 存在为通过。

---
