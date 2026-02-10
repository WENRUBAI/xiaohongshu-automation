package com.xhs.auto.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.xhs.auto.automation.HumanBehaviorSimulator
import com.xhs.auto.data.AccountManager
import com.xhs.auto.data.ConfigManager
import com.xhs.auto.data.model.TaskStatus
import kotlinx.coroutines.*
import java.util.UUID
import kotlin.random.Random

class XhsAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "XhsAccessibilityService"
        private const val PACKAGE_XIAOHONGSHU = "com.xingin.xhs"
        
        @Volatile
        var isRunning = false
            private set
        
        @Volatile
        var currentTaskStatus: TaskStatus? = null
            private set
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var taskJob: Job? = null
    private lateinit var behaviorSimulator: HumanBehaviorSimulator
    private lateinit var accountManager: AccountManager
    private lateinit var configManager: ConfigManager
    
    // 任务状态
    private var currentTaskId: String = ""
    private var currentAccountId: String = ""
    private var targetBrowseCount = 0
    private var completedBrowseCount = 0
    private var likeCount = 0
    private var collectCount = 0
    private var startTime = 0L
    
    override fun onCreate() {
        super.onCreate()
        accountManager = AccountManager.getInstance(this)
        configManager = ConfigManager.getInstance(this)
        Log.d(TAG, "Service created")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 监听小红书App的界面变化
        event?.let {
            if (it.packageName == PACKAGE_XIAOHONGSHU) {
                Log.d(TAG, "Xiaohongshu event: ${it.eventType}, class: ${it.className}")
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        stopTask()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
    
    /**
     * 开始执行任务
     */
    fun startTask(accountId: String): Boolean {
        if (isRunning) {
            Log.w(TAG, "Task already running")
            return false
        }
        
        val config = configManager.getBehaviorConfig()
        behaviorSimulator = HumanBehaviorSimulator(config)
        
        currentAccountId = accountId
        currentTaskId = "task_${System.currentTimeMillis()}"
        targetBrowseCount = behaviorSimulator.getRandomDailyTarget()
        completedBrowseCount = 0
        likeCount = 0
        collectCount = 0
        startTime = System.currentTimeMillis()
        
        isRunning = true
        accountManager.updateAccountStatus(accountId, com.xhs.auto.data.model.Account.AccountStatus.RUNNING)
        
        // 启动前台服务
        startForegroundService()
        
        // 开始执行任务
        taskJob = serviceScope.launch {
            try {
                executeTask()
            } catch (e: Exception) {
                Log.e(TAG, "Task execution error", e)
                handleError(e)
            } finally {
                finishTask()
            }
        }
        
        Log.d(TAG, "Task started: $currentTaskId, target: $targetBrowseCount")
        return true
    }
    
    /**
     * 停止任务
     */
    fun stopTask() {
        Log.d(TAG, "Stopping task...")
        isRunning = false
        taskJob?.cancel()
        taskJob = null
        
        accountManager.updateAccountStatus(currentAccountId, com.xhs.auto.data.model.Account.AccountStatus.AVAILABLE)
        stopForegroundService()
        
        Log.d(TAG, "Task stopped")
    }
    
    /**
     * 执行任务主逻辑
     */
    private suspend fun executeTask() {
        val config = configManager.getBehaviorConfig()
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        
        // 1. 打开小红书并进入目标标签
        openXiaoHongShu()
        delay(2000)
        
        // 2. 执行搜索目标标签
        val searchCount = behaviorSimulator.getRandomSearchCount()
        for (i in 0 until searchCount) {
            if (!isRunning) break
            searchRandomTag(config.tagConfig.targetTags)
            delay(behaviorSimulator.getGaussianStayTime())
        }
        
        // 3. 浏览笔记主循环
        while (isRunning && completedBrowseCount < targetBrowseCount) {
            try {
                // 执行一次浏览流程
                browseNote(screenWidth, screenHeight)
                completedBrowseCount++
                
                // 更新任务状态
                updateTaskStatus()
                
                // 随机休息
                if (shouldRest()) {
                    rest()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Browse note error", e)
                delay(1000)
            }
        }
        
        Log.d(TAG, "Task completed: $completedBrowseCount notes browsed")
    }
    
    /**
     * 浏览单篇笔记
     */
    private suspend fun browseNote(screenWidth: Int, screenHeight: Int) {
        val config = configManager.getBehaviorConfig()
        
        // 1. 停留阅读时间
        val stayTime = behaviorSimulator.getGaussianStayTime()
        
        // 模拟阅读停顿
        behaviorSimulator.simulateReadingPause()
        
        delay(stayTime)
        
        // 2. 判断是否互动
        val isTargetContent = checkIfTargetContent(config.tagConfig.targetTags)
        if (behaviorSimulator.shouldInteract(isTargetContent)) {
            when (behaviorSimulator.decideInteractionType()) {
                HumanBehaviorSimulator.InteractionType.LIKE -> {
                    performLike()
                    likeCount++
                }
                HumanBehaviorSimulator.InteractionType.COLLECT -> {
                    performCollect()
                    collectCount++
                }
            }
            delay(500)
        }
        
        // 3. 随机误操作
        behaviorSimulator.simulateRandomMistake(this, screenWidth, screenHeight)
        
        // 4. 滑动到下一条
        scrollToNextNote(screenWidth, screenHeight)
        
        // 5. 随机间隔
        behaviorSimulator.randomDelay(300, 800)
    }
    
    /**
     * 打开小红书App
     */
    private suspend fun openXiaoHongShu() {
        val intent = packageManager.getLaunchIntentForPackage(PACKAGE_XIAOHONGSHU)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
        delay(3000)  // 等待App启动
    }
    
    /**
     * 搜索随机标签
     */
    private suspend fun searchRandomTag(tags: List<String>) {
        if (tags.isEmpty()) return
        
        val randomTag = tags[Random.nextInt(tags.size)]
        Log.d(TAG, "Searching tag: $randomTag")
        
        // 这里需要实现实际的搜索逻辑
        // 由于小红书UI结构可能变化，这里只提供框架
        // 实际实现需要根据当前UI结构调整
        
        delay(2000)
    }
    
    /**
     * 检查当前内容是否为目标标签内容
     */
    private fun checkIfTargetContent(targetTags: List<String>): Boolean {
        // 通过AccessibilityNodeInfo分析当前页面内容
        // 检查是否包含目标标签关键词
        val rootNode = rootInActiveWindow ?: return false
        
        val content = getAllText(rootNode)
        rootNode.recycle()
        
        return targetTags.any { tag ->
            content.contains(tag)
        }
    }
    
    /**
     * 获取页面所有文本
     */
    private fun getAllText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        
        val text = StringBuilder()
        
        node.text?.let {
            text.append(it).append(" ")
        }
        
        for (i in 0 until node.childCount) {
            text.append(getAllText(node.getChild(i)))
        }
        
        return text.toString()
    }
    
    /**
     * 执行点赞操作
     */
    private fun performLike() {
        Log.d(TAG, "Performing like")
        // 实际实现：找到点赞按钮并点击
        // 这里需要根据小红书UI结构调整
    }
    
    /**
     * 执行收藏操作
     */
    private fun performCollect() {
        Log.d(TAG, "Performing collect")
        // 实际实现：找到收藏按钮并点击
        // 这里需要根据小红书UI结构调整
    }
    
    /**
     * 滑动到下一条笔记
     */
    private fun scrollToNextNote(screenWidth: Int, screenHeight: Int) {
        val centerX = screenWidth / 2f
        val startY = screenHeight * 0.7f
        val distance = -screenHeight * 0.6f  // 向上滑动
        
        val gesture = behaviorSimulator.createHumanLikeScrollGesture(
            centerX, startY, distance, 400L
        )
        
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Scroll completed")
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Scroll cancelled")
            }
        }, null)
    }
    
    /**
     * 判断是否休息
     */
    private fun shouldRest(): Boolean {
        // 随机休息，约每20-30篇休息一次
        return completedBrowseCount > 0 && 
               completedBrowseCount % Random.nextInt(20, 31) == 0
    }
    
    /**
     * 休息
     */
    private suspend fun rest() {
        val restTime = Random.nextLong(30000, 120000)  // 30秒-2分钟
        Log.d(TAG, "Resting for ${restTime / 1000} seconds")
        delay(restTime)
    }
    
    /**
     * 更新任务状态
     */
    private fun updateTaskStatus() {
        currentTaskStatus = TaskStatus(
            taskId = currentTaskId,
            accountId = currentAccountId,
            status = TaskStatus.Status.RUNNING,
            startTime = startTime,
            elapsedTime = System.currentTimeMillis() - startTime,
            progress = TaskStatus.Progress(completedBrowseCount, targetBrowseCount),
            stats = TaskStatus.TaskStats(
                browseCount = completedBrowseCount,
                likeCount = likeCount,
                collectCount = collectCount
            )
        )
    }
    
    /**
     * 处理错误
     */
    private fun handleError(error: Exception) {
        Log.e(TAG, "Task error", error)
        accountManager.updateAccountStatus(currentAccountId, com.xhs.auto.data.model.Account.AccountStatus.ERROR)
    }
    
    /**
     * 完成任务
     */
    private fun finishTask() {
        isRunning = false
        accountManager.updateLastRunTime(currentAccountId)
        accountManager.updateAccountStatus(currentAccountId, com.xhs.auto.data.model.Account.AccountStatus.AVAILABLE)
        stopForegroundService()
        
        Log.d(TAG, "Task finished. Total: $completedBrowseCount, Likes: $likeCount, Collects: $collectCount")
    }
    
    /**
     * 启动前台服务
     */
    private fun startForegroundService() {
        val intent = Intent(this, TaskForegroundService::class.java)
        intent.action = TaskForegroundService.ACTION_START
        startService(intent)
    }
    
    /**
     * 停止前台服务
     */
    private fun stopForegroundService() {
        val intent = Intent(this, TaskForegroundService::class.java)
        intent.action = TaskForegroundService.ACTION_STOP
        startService(intent)
    }
}
