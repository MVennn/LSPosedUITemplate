package io.github.MVennn.LsposedUITemplate.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.MVennn.LsposedUITemplate.R
import io.github.MVennn.LsposedUITemplate.app.ModulePreferences
import io.github.MVennn.LsposedUITemplate.core.ConfigKeys
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 模板示例页：演示本 UI 骨架的四件核心事，套用时整页删除换成你自己的功能页。
 *
 *  1. 开关行：自绘 GsSwitchPreference（副标题随状态变、出厂默认关）
 *  2. 列表逐条行：一行一条，行尾 ✕ 用中性色 + 40dp 热区，单条直接删不确认
 *  3. 新建行：＋ 与标题同用 primary，点开 TextInputDialogSpec，校验拦在弹层里
 *  4. 清空整批：独立一行 → ConfirmDialogSpec，message 写清范围与后果
 *
 * 所有写操作经 ModulePreferences.edit 即时落盘；弹层动作一律走
 * DialogController.dismiss 排在收起动画之后（见 AppDialogSpec 头注）。
 * 布局间距 / 图标 / 文案严格遵循 docs/DESIGN-SYSTEM.md §3.7 / §3.8 / §4.4。
 */
@Composable
internal fun DemoScreen() {
    val ctx = LocalContext.current
    val dialogs = LocalDialogController.current

    val enabled by rememberBoolPref(ConfigKeys.FEATURE_ENABLED, ConfigKeys.DEFAULT_FEATURE_ENABLED)
    val saved by rememberStringPref(ConfigKeys.DEMO_ITEMS, ConfigKeys.DEFAULT_DEMO_ITEMS)
    val items = remember(saved) { saved.lines().map { it.trim() }.filter { it.isNotEmpty() } }

    fun toast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()

    fun write(text: String) = ModulePreferences.edit { putString(ConfigKeys.DEMO_ITEMS, text) }

    fun addItem(raw: String) {
        val value = raw.trim()
        if (value.isEmpty()) return
        val base = saved.trimEnd('\n', '\r')
        write(if (base.isEmpty()) value else base + "\n" + value)
        toast(ctx.getString(R.string.demo_item_added))
    }

    fun removeItem(value: String) {
        val lines = saved.lines().filter { it.trim() != value }.joinToString("\n").trimEnd('\n')
        write(lines)
        toast(ctx.getString(R.string.demo_item_deleted))
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp + LocalTopInset.current))

        SmallTitle(stringResource(R.string.demo_section_basic))
        Card(modifier = Modifier.cardPadding()) {
            GsSwitchPreference(
                checked = enabled,
                onCheckedChange = { checked ->
                    ModulePreferences.edit { putBoolean(ConfigKeys.FEATURE_ENABLED, checked) }
                },
                title = stringResource(R.string.demo_switch_title),
                summary = stringResource(
                    if (enabled) R.string.demo_switch_summary_on else R.string.demo_switch_summary_off,
                ),
            )
            BasicComponent(
                title = stringResource(R.string.demo_status_title),
                summary = stringResource(R.string.demo_status_summary, items.size),
            )
        }

        Spacer(Modifier.height(12.dp))

        SmallTitle(stringResource(R.string.demo_section_items))
        val deleteLabel = stringResource(R.string.demo_item_delete)
        Card(modifier = Modifier.cardPadding()) {
            if (items.isEmpty()) {
                BasicComponent(
                    title = stringResource(R.string.demo_empty),
                    summary = stringResource(R.string.demo_empty_summary),
                )
            }
            items.forEach { value ->
                BasicComponent(
                    title = value,
                    endActions = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(onClickLabel = deleteLabel, role = Role.Button) {
                                    removeItem(value)
                                },
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            TintedIcon(
                                imageVector = AppIcons.Close,
                                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                contentDescription = deleteLabel,
                            )
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.cardPadding()) {
            BasicComponent(
                title = stringResource(R.string.demo_new_item),
                endActions = {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.CenterEnd) {
                        TintedIcon(
                            imageVector = AppIcons.PlusThin,
                            tint = MiuixTheme.colorScheme.primary,
                        )
                    }
                },
                onClick = {
                    dialogs?.showTextInput(
                        TextInputDialogSpec(
                            title = ctx.getString(R.string.demo_new_item),
                            message = ctx.getString(R.string.demo_new_item_tip),
                            initial = "",
                            hint = ctx.getString(R.string.demo_new_item_hint),
                            confirmText = ctx.getString(R.string.action_confirm),
                            cancelText = ctx.getString(R.string.action_cancel),
                            onConfirm = { addItem(it) },
                            validate = { v ->
                                when {
                                    v.isBlank() -> ctx.getString(R.string.demo_new_item_empty)
                                    v.trim() in items -> ctx.getString(R.string.demo_new_item_exists)
                                    else -> null
                                }
                            },
                        ),
                    )
                },
            )
            if (items.isNotEmpty()) {
                BasicComponent(
                    title = stringResource(R.string.demo_clear_items),
                    onClick = {
                        dialogs?.showConfirm(
                            ConfirmDialogSpec(
                                title = ctx.getString(R.string.demo_clear_title),
                                message = ctx.getString(R.string.demo_clear_message),
                                confirmText = ctx.getString(R.string.action_confirm),
                                cancelText = ctx.getString(R.string.action_cancel),
                                onConfirm = {
                                    write("")
                                    toast(ctx.getString(R.string.demo_cleared))
                                },
                            ),
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** 示例二级页：直接复用示例页内容，套用时换成你自己的功能详情页。 */
@Composable
internal fun DemoDetailScreen() = DemoScreen()
