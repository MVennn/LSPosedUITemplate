# 入口类必须 keep（漏一条就是「装上不生效」）。
-keep class io.github.MVennn.LsposedUITemplate.TemplateModule { *; }
-keep class io.github.MVennn.LsposedUITemplate.ui.SettingsActivity { *; }
-dontwarn io.github.libxposed.**
