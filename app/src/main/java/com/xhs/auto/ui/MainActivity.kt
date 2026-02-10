package com.xhs.auto.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xhs.auto.data.AccountManager
import com.xhs.auto.databinding.ActivityMainBinding
import com.xhs.auto.service.TaskAlarmReceiver
import com.xhs.auto.service.XhsAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_ACCESSIBILITY = 1001
    }
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var accountManager: AccountManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        accountManager = AccountManager.getInstance(this)
        
        setupUI()
        checkAccessibilityService()
        
        // 处理定时任务启动
        handleScheduledTask(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleScheduledTask(intent)
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
    
    private fun setupUI() {
        // 账号管理按钮
        binding.btnAccount.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }
        
        // 行为配置按钮
        binding.btnConfig.setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }
        
        // 任务状态按钮
        binding.btnTask.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
        
        // 开始/停止任务按钮
        binding.btnStartStop.setOnClickListener {
            if (XhsAccessibilityService.isRunning) {
                stopTask()
            } else {
                startTask()
            }
        }
        
        // 无障碍服务设置按钮
        binding.btnAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }
    }
    
    private fun updateUI() {
        // 更新任务状态
        val isRunning = XhsAccessibilityService.isRunning
        binding.tvStatus.text = if (isRunning) "运行中" else "空闲"
        binding.btnStartStop.text = if (isRunning) "停止任务" else "开始任务"
        
        // 更新账号信息
        val defaultAccount = accountManager.getDefaultAccount()
        if (defaultAccount != null) {
            binding.tvCurrentAccount.text = "当前账号: ${defaultAccount.name}"
        } else {
            binding.tvCurrentAccount.text = "当前账号: 未设置"
        }
        
        // 检查无障碍服务
        val hasAccessibility = isAccessibilityServiceEnabled()
        binding.btnAccessibility.visibility = if (hasAccessibility) View.GONE else View.VISIBLE
    }
    
    private fun startTask() {
        // 检查账号
        if (!accountManager.hasAccount()) {
            Toast.makeText(this, "请先添加账号", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 检查无障碍服务
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityDialog()
            return
        }
        
        val defaultAccount = accountManager.getDefaultAccount()
        if (defaultAccount == null) {
            Toast.makeText(this, "请设置默认账号", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 启动任务
        // 注意：实际启动需要通过Accessibility Service
        Toast.makeText(this, "正在启动任务...", Toast.LENGTH_SHORT).show()
        
        // 这里简化处理，实际应该通过Service连接来启动
        lifecycleScope.launch {
            delay(1000)
            updateUI()
        }
    }
    
    private fun stopTask() {
        // 停止任务
        Toast.makeText(this, "正在停止任务...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            delay(1000)
            updateUI()
        }
    }
    
    private fun checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityDialog()
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            0
        }
        
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                return settingValue.contains(packageName)
            }
        }
        return false
    }
    
    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要无障碍权限")
            .setMessage("小红书养号助手需要无障碍权限来实现自动化操作。请在设置中开启。")
            .setPositiveButton("去开启") { _, _ ->
                openAccessibilitySettings()
            }
            .setNegativeButton("取消", null)
            .setCancelable(false)
            .show()
    }
    
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
    
    private fun handleScheduledTask(intent: Intent?) {
        if (intent?.action == "com.xhs.auto.action.START_SCHEDULED_TASK") {
            val accountId = intent.getStringExtra("account_id")
            Log.d(TAG, "Scheduled task received for account: $accountId")
            
            // 检查无障碍服务
            if (isAccessibilityServiceEnabled()) {
                // 延迟启动，等待用户解锁屏幕
                lifecycleScope.launch {
                    delay(3000)
                    startTask()
                }
            } else {
                Toast.makeText(this, "请开启无障碍服务以执行定时任务", Toast.LENGTH_LONG).show()
            }
        }
    }
}
