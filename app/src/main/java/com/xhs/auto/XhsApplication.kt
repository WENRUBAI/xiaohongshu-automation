package com.xhs.auto

import android.app.Application
import com.xhs.auto.data.AccountManager
import com.xhs.auto.data.ConfigManager

class XhsApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化单例
        AccountManager.getInstance(this)
        ConfigManager.getInstance(this)
    }
}
