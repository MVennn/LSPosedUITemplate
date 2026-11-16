package io.github.MVennn.LsposedUITemplate.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import io.github.MVennn.LsposedUITemplate.R
import io.github.MVennn.LsposedUITemplate.app.ModulePreferences
import io.github.MVennn.LsposedUITemplate.core.ConfigKeys
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 极简路由（UI 模板通用骨架）：一级首页 + 若干二级页 + 可选三级页。
 *
 * 新增二级页 = 在 [Route] 加一个 data object + 在 [RouteSaver] 的 save / restore
 * 各加一个分支（when 穷尽，漏写编译失败）+ 在 [MainScreen] 的 when (shownSub) 挂页面；
 * 三级页还要在 [editParent] 登记它的父页。
 *
 * 三层叠放 + 二级 / 三级两条独立滑动进度：
 *  - 进二级页：二级进度 0→1，三级保持 0；
 *  - 进三级页：二级保持 1，三级 0→1；
 *  - 三级返回：只滑三级，露出底下的二级页；
 *  - 二级返回：滑走二级回首页。
 * 进度只在 graphicsLayer lambda 里读，组合期不读（避免 300ms 滑动里每帧重组整棵树）。
 */
internal sealed interface Route {
    data object Home : Route
    data object Disclaimer : Route
    data object Notice : Route
    /** 示例二级页：套用时换成你的功能设置页 */
    data object DemoDetail : Route
}

/** 三级页 → 它压在下面的二级页；非三级页返回 null。模板暂无三级页，保留扩展点。 */
private fun editParent(route: Route): Route? = when (route) {
    else -> null
}

private const val RouteAnimMillis = 300

/** Route 的存档器：把 sealed interface 编成短字符串，转屏 / 进程回收后能带回当前层级。 */
private object RouteSaver : Saver<Route, String> {
    override fun SaverScope.save(value: Route): String = when (value) {
        Route.Home -> "home"
        Route.Disclaimer -> "disclaimer"
        Route.Notice -> "notice"
        Route.DemoDetail -> "demo_detail"
    }

    override fun restore(value: String): Route = when (value) {
        "disclaimer" -> Route.Disclaimer
        "notice" -> Route.Notice
        "demo_detail" -> Route.DemoDetail
        else -> Route.Home
    }
}

@Composable
internal fun MainScreen(initialRoute: Route = Route.Home) {
    val scope = rememberCoroutineScope()

    var currentTab by rememberSaveable { mutableStateOf(Tab.MODULE) }
    var route by rememberSaveable(stateSaver = RouteSaver) { mutableStateOf<Route>(initialRoute) }

    val agreedDisclaimer by rememberLocalBoolPref(
        ConfigKeys.DISCLAIMER_ACCEPTED,
        ConfigKeys.DEFAULT_DISCLAIMER_ACCEPTED,
    )
    val showAgreement = !agreedDisclaimer && route == Route.Home

    val editRouteNow: Route? = route.takeIf { editParent(it) != null }
    val subRouteNow = editParent(route) ?: route
    var shownSub by remember { mutableStateOf(subRouteNow) }
    var shownEdit by remember { mutableStateOf(editRouteNow) }
    if (subRouteNow != Route.Home && subRouteNow != shownSub) shownSub = subRouteNow
    if (editRouteNow != null && editRouteNow != shownEdit) shownEdit = editRouteNow

    val predictiveBack = rememberPredictiveBack()
    val subProgress = remember { Animatable(if (route == Route.Home) 0f else 1f) }
    val editProgress = remember { Animatable(if (editParent(route) != null) 1f else 0f) }

    fun backTarget(): Route = editParent(route) ?: Route.Home

    LaunchedEffect(route) {
        when {
            route == Route.Home -> {
                if (shownEdit != null) {
                    editProgress.animateTo(0f, tween(RouteAnimMillis))
                    shownEdit = null
                }
                subProgress.animateTo(0f, tween(RouteAnimMillis))
            }
            editParent(route) != null -> {
                subProgress.animateTo(1f, tween(RouteAnimMillis))
                editProgress.animateTo(1f, tween(RouteAnimMillis))
            }
            else -> {
                if (shownEdit != null) {
                    editProgress.animateTo(0f, tween(RouteAnimMillis))
                    shownEdit = null
                }
                subProgress.animateTo(1f, tween(RouteAnimMillis))
            }
        }
    }

    if (predictiveBack) {
        PredictiveBackHandler(enabled = route != Route.Home) { flow ->
            val onEdit = editParent(route) != null
            try {
                flow.collect { (if (onEdit) editProgress else subProgress).snapTo(1f - it.progress) }
                route = backTarget()
            } catch (e: CancellationException) {
                scope.launch { (if (onEdit) editProgress else subProgress).animateTo(1f, tween(200)) }
                throw e
            }
        }
    } else {
        BackHandler(enabled = route != Route.Home) { route = backTarget() }
    }

    val subMounted by remember { derivedStateOf { subProgress.value > 0.001f } }
    val editMounted by remember { derivedStateOf { editProgress.value > 0.001f } }
    val background = MiuixTheme.colorScheme.surface

    Box(modifier = Modifier.fillMaxSize()) {
        // 第 0 层：底色层（不参与手势动画）
        Box(modifier = Modifier.fillMaxSize().background(background))

        // 第 1 层：一级首页（常驻底衬）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = subProgress.value
                    val s = 1f - 0.06f * p
                    scaleX = s; scaleY = s; alpha = 1f - 0.35f * p
                },
        ) {
            HomeScaffold(
                currentTab = currentTab,
                onSelectTab = { currentTab = it },
                onOpenDisclaimer = { route = Route.Disclaimer },
                onOpenNotice = { route = Route.Notice },
                onOpenFeatureDetail = { id ->
                    route = when (id) {
                        "demo" -> Route.DemoDetail
                        else -> Route.Home
                    }
                },
                showAgreement = showAgreement,
                onAgreeDisclaimer = {
                    ModulePreferences.setLocalBoolean(ConfigKeys.DISCLAIMER_ACCEPTED, true)
                },
            )
        }

        // 第 2 层：二级页（右滑进出）
        if (route != Route.Home || subMounted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = size.width * (1f - subProgress.value) }
                    .background(background),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (editMounted) {
                                Modifier.graphicsLayer {
                                    val s = 1f - 0.06f * editProgress.value
                                    scaleX = s; scaleY = s
                                }
                            } else Modifier,
                        ),
                ) {
                    when (shownSub) {
                        Route.Disclaimer -> SubScreen(
                            title = stringResource(R.string.disclaimer_detail_title),
                            onBack = { route = Route.Home },
                        ) { DisclaimerScreen() }

                        Route.Notice -> SubScreen(
                            title = stringResource(R.string.notice_detail_title),
                            onBack = { route = Route.Home },
                        ) { NoticeScreen() }

                        Route.DemoDetail -> SubScreen(
                            title = stringResource(R.string.demo_detail_title),
                            onBack = { route = Route.Home },
                        ) { DemoDetailScreen() }

                        else -> Unit
                    }
                }
            }
        }
    }
}
