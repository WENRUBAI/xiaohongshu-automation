package com.xhs.auto.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF
import com.xhs.auto.data.model.BehaviorConfig
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random

class HumanBehaviorSimulator(
    private val config: BehaviorConfig
) {
    private val random = Random(System.currentTimeMillis())
    
    /**
     * 生成正态分布的停留时间
     */
    fun getGaussianStayTime(): Long {
        val mean = config.browseConfig.stayTimeMean * 1000L  // 转换为毫秒
        val stdDev = config.browseConfig.stayTimeStdDev * 1000L
        
        // Box-Muller变换生成正态分布
        val u1 = random.nextDouble()
        val u2 = random.nextDouble()
        val z0 = kotlin.math.sqrt(-2.0 * kotlin.math.ln(u1)) * kotlin.math.cos(2.0 * Math.PI * u2)
        
        val time = (mean + stdDev * z0).toLong()
        
        // 限制在最小和最大范围内
        val minTime = config.browseConfig.stayTimeMin * 1000L
        val maxTime = config.browseConfig.stayTimeMax * 1000L
        
        return time.coerceIn(minTime, maxTime)
    }
    
    /**
     * 创建贝塞尔曲线滑动路径
     */
    fun createBezierScrollPath(
        startX: Float,
        startY: Float,
        endY: Float,
        controlPointOffset: Float = 100f
    ): Path {
        val path = Path()
        path.moveTo(startX, startY)
        
        // 添加随机控制点，创建曲线效果
        val controlX = startX + randomOffset(-controlPointOffset, controlPointOffset)
        val controlY = (startY + endY) / 2 + randomOffset(-controlPointOffset, controlPointOffset)
        
        path.quadTo(controlX, controlY, startX, endY)
        return path
    }
    
    /**
     * 创建模拟人类滑动的GestureDescription
     */
    fun createHumanLikeScrollGesture(
        startX: Float,
        startY: Float,
        distance: Float,
        duration: Long = 300L
    ): GestureDescription {
        val endY = startY + distance
        
        val path = if (config.humanBehaviorConfig.bezierScroll) {
            createBezierScrollPath(startX, startY, endY)
        } else {
            Path().apply {
                moveTo(startX, startY)
                lineTo(startX, endY)
            }
        }
        
        val gestureBuilder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(
            path,
            0,
            duration + randomOffset(-50, 50)  // 添加随机时长变化
        )
        gestureBuilder.addStroke(strokeDescription)
        
        return gestureBuilder.build()
    }
    
    /**
     * 判断是否进行互动（点赞/收藏）
     */
    fun shouldInteract(isTargetContent: Boolean): Boolean {
        val probRange = if (isTargetContent) {
            config.interactionConfig.targetContentProbMin to 
            config.interactionConfig.targetContentProbMax
        } else {
            config.interactionConfig.otherContentProbMin to 
            config.interactionConfig.otherContentProbMax
        }
        
        // 在范围内生成随机概率
        val randomProb = random.nextFloat() * (probRange.second - probRange.first) + probRange.first
        return random.nextFloat() < randomProb
    }
    
    /**
     * 决定互动类型（点赞或收藏）
     */
    fun decideInteractionType(): InteractionType {
        return if (random.nextFloat() < config.interactionConfig.likeRatio) {
            InteractionType.LIKE
        } else {
            InteractionType.COLLECT
        }
    }
    
    /**
     * 模拟随机误操作
     */
    suspend fun simulateRandomMistake(
        service: AccessibilityService,
        screenWidth: Int,
        screenHeight: Int
    ): Boolean {
        if (!config.humanBehaviorConfig.randomMistakes) return false
        
        // 5%概率执行误操作
        if (random.nextFloat() >= 0.05f) return false
        
        val centerX = screenWidth / 2f
        val currentY = screenHeight * 0.6f
        
        // 快速下滑
        val fastScrollGesture = createHumanLikeScrollGesture(
            centerX,
            currentY,
            800f,  // 快速下滑较大距离
            150L   // 较快速度
        )
        
        service.dispatchGesture(fastScrollGesture, null, null)
        delay(500)
        
        // 回退（上滑）
        val backScrollGesture = createHumanLikeScrollGesture(
            centerX,
            currentY + 800f,
            -400f,  // 回退一部分
            200L
        )
        
        service.dispatchGesture(backScrollGesture, null, null)
        delay(800)
        
        return true
    }
    
    /**
     * 模拟阅读停顿
     */
    suspend fun simulateReadingPause(): Boolean {
        if (!config.humanBehaviorConfig.readingPause) return false
        
        // 20%概率在阅读时停顿
        if (random.nextFloat() >= 0.2f) return false
        
        // 停顿2-5秒
        val pauseTime = random.nextLong(2000, 5000)
        delay(pauseTime)
        
        return true
    }
    
    /**
     * 生成随机搜索次数
     */
    fun getRandomSearchCount(): Int {
        return random.nextInt(
            config.searchConfig.searchCountMin,
            config.searchConfig.searchCountMax + 1
        )
    }
    
    /**
     * 生成随机每日目标
     */
    fun getRandomDailyTarget(): Int {
        return random.nextInt(
            config.browseConfig.dailyTargetMin,
            config.browseConfig.dailyTargetMax + 1
        )
    }
    
    /**
     * 生成随机操作间隔
     */
    suspend fun randomDelay(minMs: Long = 500, maxMs: Long = 1500) {
        delay(random.nextLong(minMs, maxMs))
    }
    
    /**
     * 生成随机偏移量
     */
    private fun randomOffset(min: Float, max: Float): Float {
        return random.nextFloat() * (max - min) + min
    }
    
    private fun randomOffset(min: Long, max: Long): Long {
        return random.nextLong(min, max)
    }
    
    enum class InteractionType {
        LIKE,
        COLLECT
    }
}
