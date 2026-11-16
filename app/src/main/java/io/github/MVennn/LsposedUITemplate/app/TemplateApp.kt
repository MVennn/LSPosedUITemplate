package io.github.MVennn.LsposedUITemplate.app

import android.app.Application
import io.github.MVennn.LsposedUITemplate.core.ConfigKeys
import io.github.MVennn.LsposedUITemplate.core.GLog
import io.github.MVennn.LsposedUITemplate.core.LogLevel
import io.github.MVennn.LsposedUITemplate.core.LogStore
import java.io.File

/**
 * 模块自身的 Application（跑在模块自己的进程，**不是**被注入的目标进程）。
 *
 * 模板里做四件事：
 *  1. 初始化配置仓库（本地兜底 + 等待框架服务）
 *  2. 应用「日志等级」，并挂上日志落盘仓库
 *  3. 注册框架服务监听，拿到 binder 后把配置后端切到 remote preferences
 *  4. 设置界面改等级后立即生效
 *
 * 注意：桌面图标切换、广播接收、Provider 回传等业务能力已从模板剥离，
 * 需要时在你的项目里自行添加并在 Manifest 声明。
 */
class TemplateApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ModulePreferences.init(this)
        applyLogLevel()
        GLog.attachStore(LogStore(File(filesDir, "logs")))
        FrameworkBridge.init()
        ModulePreferences.observe { applyLogLevel() }
        GLog.i("模块 App 启动：配置仓库与服务监听就绪")
    }

    private var lastAppliedLevel = Int.MIN_VALUE

    private fun applyLogLevel() {
        val level = ModulePreferences.getInt(ConfigKeys.LOG_LEVEL, ConfigKeys.DEFAULT_LOG_LEVEL)
        GLog.level = level
        if (level != lastAppliedLevel) {
            lastAppliedLevel = level
            ModulePreferences.setLocalInt(ConfigKeys.LOG_LEVEL, level)
        }
    }
}
