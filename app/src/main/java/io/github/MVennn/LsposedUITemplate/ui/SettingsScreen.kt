package io.github.MVennn.LsposedUITemplate.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.MVennn.LsposedUITemplate.R
import io.github.MVennn.LsposedUITemplate.app.ModulePreferences
import io.github.MVennn.LsposedUITemplate.core.ConfigKeys
import io.github.MVennn.LsposedUITemplate.core.GLog
import io.github.MVennn.LsposedUITemplate.core.LogLevel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle


/**
 * Tab 3 · 设置 —— **只放模块范围内的设置项**。
 *
 * 分组：
 *  - 界面：桌面图标
 *  - 材质设置：液态玻璃（总开关，+ 其下的玻璃光效）
 *  - 通用：深色模式、语言、可预测返回
 *  - 数据管理：备份 / 恢复 / 重置
 *  - 日志：日志等级、导出日志
 *
 * ## 选择类设置一律走自绘弹窗（2.8.0 起）
 *
 * 深色模式 / 语言 / 日志等级这三项，历史上用 Miuix 的
 * `OverlayDropdownPreference`，表现为**一点就闪退**，反复修了三个版本都没根治。
 * 现在全部改成「一行 [ArrowPreference]（右侧显示当前值）→ 点开自绘弹窗选」，
 * 弹窗实现见 [AppDialog]，材质跟随「液态玻璃」总开关。
 *
 * ## 为什么 Miuix 弹层治不干净
 *
 * 两条**互相独立**的根因，历史上一直只修其中一条：
 *
 *  1. **同步写配置撞上弹层自己的收起动画。** Miuix 的弹层在它自己的 `onClick` 里
 *     **同步开始收起动画**；此时立刻 `commit()` 会经由
 *     `OnSharedPreferenceChangeListener` 同步回调，把 Compose 状态在动画进行中改写，
 *     弹层入参（`selectedIndex`）中途变化 → 动画与重组打架 → 崩。
 *     治法：`ModulePreferences.editDeferred`（延迟到动画结束）+ `notifyChanged()`
 *     一律 post。3.2.x 弹层换成自绘之后，第 1 条换了形态但本质还在 ——
 *     所以所有写入都排在弹窗收起动画播完之后（`DialogController.dismiss { }`），
 *     动作真正执行时弹窗已经彻底关闭，可以同步写。
 *     （editDeferred 的 360ms 曾因此拖出「弹窗收回去了、副标题顿一会才切换」，
 *     已随 Miuix 弹层一起移除。）
 *  2. **`LocaleHelper.wrap()` 返回的 Context 不是 `ContextWrapper`。**
 *     `createConfigurationContext()` 产出的是 `ContextImpl`，`baseContext` 链是断的，
 *     androidx 沿 `ContextWrapper` 链找宿主时抛 `ClassCastException`。
 *     这条在 2.5.0 已修（见 [io.github.MVennn.LsposedUITemplate.app.LocaleHelper] 的类注释）。
 *
 * 现在改成自绘弹窗之后，连第 1 条的触发面都不存在了 ——
 * 弹窗只是普通 Composition，关闭就是一次状态翻转，没有「库的收起动画」这回事。
 * 但「写入排在关闭动画之后」仍然保留（`DialogController.dismiss { }`），
 * 挡住「主题/语言整树重组」与弹窗关闭动画重叠的情况。
 */

@Composable
internal fun SettingsScreen() {
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()
    val dialogs = LocalDialogController.current

    fun demoToast(label: String) {
        Toast.makeText(ctx, ctx.getString(R.string.template_demo_hint, label), Toast.LENGTH_SHORT).show()
    }

    // 预取文案：onClick / onConfirm 是非 Composable lambda，里面不能再调 stringResource。
    val labelDesktopIcon = stringResource(R.string.item_desktop_icon)
    val labelBackup = stringResource(R.string.item_backup)
    val labelRestore = stringResource(R.string.item_restore)
    val labelReset = stringResource(R.string.item_reset)
    val labelExportLog = stringResource(R.string.item_export_log)
    val resetTitle = stringResource(R.string.dialog_reset_title)
    val resetMessage = stringResource(R.string.dialog_reset_message)
    val confirmText = stringResource(R.string.action_confirm)
    val cancelText = stringResource(R.string.action_cancel)


    // 三个选择项弹窗的触发控件位置（贴着控件弹）。
    var themeAnchor by remember { mutableStateOf<Rect?>(null) }
    var languageAnchor by remember { mutableStateOf<Rect?>(null) }
    var logLevelAnchor by remember { mutableStateOf<Rect?>(null) }

    val desktopIcon by rememberLocalBoolPref(ConfigKeys.DESKTOP_ICON_LOCAL, ConfigKeys.DEFAULT_DESKTOP_ICON)
    val liquidGlass by rememberBoolPref(ConfigKeys.LIQUID_GLASS, ConfigKeys.DEFAULT_LIQUID_GLASS)
    val glassEffect by rememberBoolPref(ConfigKeys.GLASS_EFFECT, ConfigKeys.DEFAULT_GLASS_EFFECT)
    val theme by rememberStringPref(ConfigKeys.THEME, ConfigKeys.DEFAULT_THEME)
    val language by rememberStringPref(ConfigKeys.LANGUAGE, ConfigKeys.DEFAULT_LANGUAGE)
    val predictiveBack by rememberBoolPref(ConfigKeys.PREDICTIVE_BACK, ConfigKeys.DEFAULT_PREDICTIVE_BACK)
    val logLevel by rememberIntPref(ConfigKeys.LOG_LEVEL, ConfigKeys.DEFAULT_LOG_LEVEL)

    val themeLabels = stringArrayResource(R.array.theme_labels)
    val languageLabels = stringArrayResource(R.array.language_labels)
    val logLabels = stringArrayResource(R.array.log_level_labels)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Spacer(Modifier.height(8.dp + LocalTopInset.current))


        // --- 界面 ---
        SmallTitle(stringResource(R.string.section_interface))
        Card(modifier = Modifier.cardPadding()) {
            GsSwitchPreference(
                checked = desktopIcon,
                onCheckedChange = { v ->
                    ModulePreferences.setLocalBoolean(ConfigKeys.DESKTOP_ICON_LOCAL, v)
                    demoToast(labelDesktopIcon)
                },
                title = stringResource(R.string.item_desktop_icon),
                summary = if (desktopIcon) null else stringResource(R.string.item_desktop_icon_summary_off),
            )
        }


        //
        // 「液态玻璃」是**顶栏 + 悬浮底栏的总开关**，形态完全由它决定，所以单独成组，
        // 不和桌面图标混在「界面」里（原来它叫「Dock 变换」，只描述底栏，语义已经过时）。
        SmallTitle(stringResource(R.string.section_dock))
        Card(modifier = Modifier.cardPadding()) {
            GsSwitchPreference(
                checked = liquidGlass,
                onCheckedChange = { v ->
                    ModulePreferences.edit { putBoolean(ConfigKeys.LIQUID_GLASS, v) }
                },
                title = stringResource(R.string.item_liquid_glass),
                // 开着的时候标题本身就说清了，不再重复；关掉时说明当前是纯色
                summary = if (liquidGlass) {
                    null
                } else {
                    stringResource(R.string.liquid_glass_off)
                },
            )
            // 只有液态玻璃才谈得上「光效」，所以在它开启时展开到下方。
            // 动画与功能页的二级入口保持一致（见 FeatureTab.FeatureEntry）。
            AnimatedVisibility(
                visible = liquidGlass,
                enter = expandVertically(tween(220)) + fadeIn(tween(180)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(120)),
            ) {
                GsSwitchPreference(
                    checked = glassEffect,
                    onCheckedChange = { v ->
                        ModulePreferences.edit { putBoolean(ConfigKeys.GLASS_EFFECT, v) }
                    },
                    title = stringResource(R.string.item_glass_effect),
                    // 副标题跟随开关走（2.9.9）：开 = 完整版光效、关 = 简约版。
                    // 之前是一条静态说明（「完整的玻璃光效和简约版本」），读不出当前处于哪一档。
                    summary = stringResource(
                        if (glassEffect) {
                            R.string.item_glass_effect_summary_on
                        } else {
                            R.string.item_glass_effect_summary_off
                        }
                    ),
                )
            }
        }


        // --- 通用 ---
        SmallTitle(stringResource(R.string.section_general))
        Card(modifier = Modifier.cardPadding()) {
            // 主题设置：右侧直接显示当前值，点开弹窗选（弹窗贴在控件右下方）。
            // 写入排在弹窗收起动画之后（dismiss { }），执行时弹窗已关闭，可同步落盘 ——
            // 主题变化会整树重组，不能在动画进行中改写状态。
            BasicComponent(
                title = stringResource(R.string.item_theme),
                modifier = Modifier.onGloballyPositioned { themeAnchor = it.boundsInWindow() },
                summary = themeLabels.getOrElse(
                    ConfigKeys.THEME_VALUES.indexOf(theme).coerceAtLeast(0),
                ) { "" },
                // 朝下箭头：这一行是**原地弹窗**，不是进入下一级（见 DownArrowIcon）
                endActions = { DownArrowIcon() },
                onClick = {
                    dialogs?.showChoice(
                        ChoiceDialogSpec(
                            items = themeLabels.toList(),
                            selectedIndex = ConfigKeys.THEME_VALUES.indexOf(theme).coerceAtLeast(0),
                            anchor = themeAnchor,
                            onSelect = { idx ->
                                ConfigKeys.THEME_VALUES.getOrNull(idx)
                                    ?.takeIf { it != theme }
                                    ?.let { v -> ModulePreferences.edit { putString(ConfigKeys.THEME, v) } }
                            },
                        ),
                    )
                },
            )
            BasicComponent(
                title = stringResource(R.string.item_language),
                modifier = Modifier.onGloballyPositioned { languageAnchor = it.boundsInWindow() },
                summary = languageLabels.getOrElse(
                    ConfigKeys.LANGUAGE_VALUES.indexOf(language).coerceAtLeast(0),
                ) { "" },
                endActions = { DownArrowIcon() },
                onClick = {
                    dialogs?.showChoice(
                        ChoiceDialogSpec(
                            items = languageLabels.toList(),
                            selectedIndex = ConfigKeys.LANGUAGE_VALUES.indexOf(language).coerceAtLeast(0),
                            anchor = languageAnchor,
                            onSelect = { idx ->
                                ConfigKeys.LANGUAGE_VALUES.getOrNull(idx)
                                    ?.takeIf { it != language }
                                    ?.let { v -> ModulePreferences.edit { putString(ConfigKeys.LANGUAGE, v) } }
                            },
                        ),
                    )
                },
            )
            GsSwitchPreference(
                checked = predictiveBack,
                onCheckedChange = { v ->
                    ModulePreferences.edit { putBoolean(ConfigKeys.PREDICTIVE_BACK, v) }
                },
                title = stringResource(R.string.item_predictive_back),
                summary = stringResource(
                    if (predictiveBack) {
                        R.string.item_predictive_back_summary_on
                    } else {
                        R.string.item_predictive_back_summary_off
                    }
                ),
            )
        }


        // --- 数据管理 ---
        SmallTitle(stringResource(R.string.section_data))
        Card(modifier = Modifier.cardPadding()) {
            BasicComponent(
                title = stringResource(R.string.item_backup),
                endActions = { ArrowRightIcon() },
                onClick = { demoToast(labelBackup) },
            )
            BasicComponent(
                title = stringResource(R.string.item_restore),
                endActions = { ArrowRightIcon() },
                onClick = { demoToast(labelRestore) },
            )
            BasicComponent(
                title = stringResource(R.string.item_reset),
                summary = stringResource(R.string.item_reset_summary),
                endActions = { ArrowRightIcon() },
                onClick = {
                    dialogs?.showConfirm(
                        ConfirmDialogSpec(
                            title = resetTitle,
                            message = resetMessage,
                            confirmText = confirmText,
                            cancelText = cancelText,
                            onConfirm = { demoToast(labelReset) },
                        ),
                    )
                },
            )
        }


        // --- 日志 ---
        SmallTitle(stringResource(R.string.section_log))
        Card(modifier = Modifier.cardPadding()) {
            BasicComponent(
                title = stringResource(R.string.item_log_level),
                modifier = Modifier.onGloballyPositioned { logLevelAnchor = it.boundsInWindow() },
                summary = logLabels.getOrElse(
                    LogLevel.VALUES.indexOf(logLevel).coerceAtLeast(0),
                ) { "" },
                endActions = { DownArrowIcon() },
                onClick = {
                    dialogs?.showChoice(
                        ChoiceDialogSpec(
                            items = logLabels.toList(),
                            selectedIndex = LogLevel.VALUES.indexOf(logLevel).coerceAtLeast(0),
                            anchor = logLevelAnchor,
                            onSelect = { idx ->
                                val value = LogLevel.VALUES.getOrNull(idx) ?: LogLevel.INFO
                                if (value != logLevel) {
                                    ModulePreferences.edit { putInt(ConfigKeys.LOG_LEVEL, value) }
                                    GLog.level = value
                                    if (value == LogLevel.OFF) GLog.clearStore()
                                }
                            },
                        ),
                    )
                },
            )
            BasicComponent(
                title = stringResource(R.string.item_export_log),
                summary = stringResource(R.string.item_export_log_summary),
                endActions = { ArrowRightIcon() },
                onClick = { demoToast(labelExportLog) },
            )
        }

        Spacer(Modifier.height(24.dp + LocalNavBarInset.current))
    }
}
