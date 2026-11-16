package io.github.MVennn.LsposedUITemplate.core

/**
 * 配置键与默认值（UI 模板）。
 *
 * **刻意不引用任何 Xposed / Android 框架类**：目标进程侧与模块 App 侧都引用它，
 * 保证模块 App 不会因为加载它而意外触发 Xposed 类的加载。
 *
 * 这是模板示例：保留了一套「功能开关键 + 模块设置键」的登记口径。
 * 套用时把 [FEATURE_ENABLED] / [DEMO_ITEMS] 换成你自己的功能键，
 * 其余界面设置键（语言 / 主题 / 底栏 / 日志）按需要删改。
 */
object ConfigKeys {

    /** remote preferences 的分组名。新项目务必换成自己的唯一名字。 */
    const val GROUP = "config"

    // ---------------------------------------------------------------- 示例功能键（套用时替换）

    /** 示例功能开关键：模板演示用，套用时换成你的功能开关 */
    const val FEATURE_ENABLED = "feature_enabled"

    /** 示例列表：一行一条文本，存成一个字符串，多条用换行分隔 */
    const val DEMO_ITEMS = "demo_items"

    // ---------------------------------------------------------------- 本机界面状态

    /**
     * 是否已阅读并同意免责声明。
     * 只用于模块自己的界面（首次启动的同意弹窗），走**本地**存储：
     * 走 remote preferences 的话，后端异步切换会让它在启动时先 false 后 true，
     * 弹窗「闪一下就没」。
     */
    const val DISCLAIMER_ACCEPTED = "disclaimer_accepted"

    /** 桌面图标开关的本机镜像（模板示例：只切 UI 状态，不真的切 alias） */
    const val DESKTOP_ICON_LOCAL = "desktop_icon_local"

    // ---------------------------------------------------------------- 模块设置

    /** 日志等级，取值见 [LogLevel] */
    const val LOG_LEVEL = "log_level"

    /** 液态玻璃总开关：true = 顶栏与悬浮底栏都是液态玻璃 */
    const val LIQUID_GLASS = "dock_liquid"

    /** 液态玻璃的光效：模糊 / 折射 / 投影 / 内阴影 */
    const val GLASS_EFFECT = "dock_effect"

    /** 界面语言，取值见 [LANGUAGE_VALUES] */
    const val LANGUAGE = "language"

    /** 深色模式，取值见 [THEME_VALUES] */
    const val THEME = "theme"

    /** 可预测返回（Android 13+ Predictive Back） */
    const val PREDICTIVE_BACK = "predictive_back"

    // ---------------------------------------------------------------- 语言取值

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_ZH = "zh-CN"
    const val LANGUAGE_EN = "en"

    /** 必须与 R.array.language_labels 的条目一一对应：跟随系统 / 简体中文 / English */
    val LANGUAGE_VALUES = arrayOf(LANGUAGE_SYSTEM, LANGUAGE_ZH, LANGUAGE_EN)

    // ---------------------------------------------------------------- 深色模式取值

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    /** 与 R.array.theme_labels 一一对应：跟随系统 / 亮色 / 暗色 */
    val THEME_VALUES = arrayOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK)

    // ---------------------------------------------------------------- 默认值

    /** 所有功能开关一律默认关闭：装完不动，行为与原生一致 */
    const val DEFAULT_FEATURE_ENABLED = false

    const val DEFAULT_DEMO_ITEMS = ""

    const val DEFAULT_LOG_LEVEL = LogLevel.INFO
    const val DEFAULT_LIQUID_GLASS = true
    const val DEFAULT_GLASS_EFFECT = true
    const val DEFAULT_LANGUAGE = LANGUAGE_SYSTEM
    const val DEFAULT_THEME = THEME_SYSTEM
    const val DEFAULT_PREDICTIVE_BACK = true

    /** 本地界面状态缺键回落值：没键 = 还没同意 = 弹门槛弹窗 */
    const val DEFAULT_DISCLAIMER_ACCEPTED = false
    const val DEFAULT_DESKTOP_ICON = true

    // ---------------------------------------------------------------- 数据管理

    /**
     * 会被备份 / 恢复 / 重置覆盖的全部配置键。
     * [DISCLAIMER_ACCEPTED] 不在此列（本机界面状态，见其注释）。
     */
    val ALL_KEYS = arrayOf(
        FEATURE_ENABLED, DEMO_ITEMS,
        LIQUID_GLASS, GLASS_EFFECT, LANGUAGE, THEME, PREDICTIVE_BACK,
        LOG_LEVEL,
    )

    /** 功能开关键：重置时显式写回 false */
    val FEATURE_KEYS = arrayOf(FEATURE_ENABLED)

    /** 目标进程真正会读的键（预留登记口径，新增时只增不减） */
    val SNAPSHOT_KEYS: Set<String> = setOf(FEATURE_ENABLED, DEMO_ITEMS, LOG_LEVEL)

    /** 全部配置键的出厂默认值 */
    val DEFAULTS: Map<String, Any> = mapOf(
        FEATURE_ENABLED to DEFAULT_FEATURE_ENABLED,
        DEMO_ITEMS to DEFAULT_DEMO_ITEMS,
        LIQUID_GLASS to DEFAULT_LIQUID_GLASS,
        GLASS_EFFECT to DEFAULT_GLASS_EFFECT,
        LANGUAGE to DEFAULT_LANGUAGE,
        THEME to DEFAULT_THEME,
        PREDICTIVE_BACK to DEFAULT_PREDICTIVE_BACK,
        LOG_LEVEL to DEFAULT_LOG_LEVEL,
    )
}
