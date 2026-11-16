package io.github.MVennn.LsposedUITemplate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.MVennn.LsposedUITemplate.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card

/**
 * 免责声明全文（二级页内容）。
 *
 * 正文是一整块 Markdown（见 `R.string.disclaimer_full`），交给 [MarkdownText] 渲染：
 * 一级标题、二级标题、加粗、斜体、列表、引用各走各的字号与字重，长文才有结构。
 *
 * 文案必须是「单行 + `\n` 转义」形态 —— Android 会折叠字符串里的真实换行，
 * 一折叠空行就没了，Markdown 的分段和标题全部失效。
 */
@Composable
internal fun DisclaimerContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp + LocalTopInset.current))
        Card(modifier = Modifier.cardPadding()) {
            MarkdownText(
                markdown = stringResource(R.string.disclaimer_full),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }
        // 二级页里 LocalDockInset 恒为 0（那口子是给一级页悬浮底栏的），
        // 这里让的是手势导航条的高度 —— 见 SubScreen 的注释。
        Spacer(Modifier.height(24.dp + LocalNavBarInset.current))
    }
}

/**
 * 第三方组件声明（二级页内容）。
 *
 * 正文读的是随 APK 分发的 `assets/NOTICE.txt`（由根目录 [NOTICE] 复制进来，
 * 改许可证信息时两处要同步）。与免责声明不同它是一份真文件而非 string 资源
 * —— Apache-2.0 §4 要求分发包里带着 NOTICE，string 资源在编译期会并进
 * resources.arsc，作为「文件随包分发」的载体不够直白。
 */
@Composable
internal fun NoticeContent() {
    val ctx = LocalContext.current
    var notice by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        // 挪到 IO：5KB 虽小，但这段在首帧路径上，读盘 + 解码没有理由占主线程
        notice = withContext(Dispatchers.IO) {
            runCatching {
                ctx.assets.open("NOTICE.txt").bufferedReader().use { it.readText() }
            }.getOrElse { "" }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp + LocalTopInset.current))
        Card(modifier = Modifier.cardPadding()) {
            if (notice.isNotEmpty()) {
                MarkdownText(
                    markdown = notice,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                )
            }
        }
        // 同免责声明页：这里让的是手势导航条的高度 —— 见 SubScreen 的注释。
        Spacer(Modifier.height(24.dp + LocalNavBarInset.current))
    }
}
