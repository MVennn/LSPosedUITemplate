package io.github.MVennn.LsposedUITemplate;

import android.util.Log;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * 模块入口（模板空壳）：Xposed 框架在目标进程启动时回调 onPackageLoaded。
 *
 * 套用步骤：
 *  1. 把本类改成你的模块名，并同步 META-INF/xposed/java_init.list 里的全限定类名；
 *  2. 在 onPackageLoaded 里用 modern hook API 注入你的目标类（本模板不携带任何业务 hook）；
 *  3. 需要固定作用域时改 META-INF/xposed/scope.list。
 *
 * 注意：设置界面（ui/SettingsActivity）跑在模块自己的进程，不走本类；
 * 本类只负责目标进程内的 hook。父类 XposedModule 提供无参构造，不要自己写构造器。
 */
public class TemplateModule extends XposedModule {

    private static final String TAG = "LsposedUITemplate";

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        log("module loaded, process=" + param.getProcessName());
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageLoadedParam param) {
        // 模板：不挂任何 hook。示例：只记录被加载的包名，套用时删除。
        log("package loaded: " + param.getPackageName());
    }

    /** 同时写 logcat 与 Xposed 框架日志；框架未 attach 时忽略异常。 */
    void log(String msg) {
        Log.i(TAG, msg);
        try {
            super.log(Log.INFO, TAG, msg);
        } catch (Throwable ignored) {
            // 模块还没 attach 到框架时忽略
        }
    }
}
