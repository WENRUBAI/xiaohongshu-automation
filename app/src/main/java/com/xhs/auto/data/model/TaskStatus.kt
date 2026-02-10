package com.xhs.auto.data.model

data class TaskStatus(
    val taskId: String,
    val accountId: String,
    val status: Status,
    val startTime: Long,
    val elapsedTime: Long = 0,
    val progress: Progress = Progress(),
    val currentAction: CurrentAction? = null,
    val stats: TaskStats = TaskStats()
) {
    enum class Status {
        PENDING,        // 等待中
        RUNNING,        // 运行中
        COMPLETED,      // 已完成
        ERROR,          // 异常
        PAUSED          // 已暂停
    }
    
    data class Progress(
        val completed: Int = 0,
        val target: Int = BehaviorConfig.DEFAULT_DAILY_TARGET
    )
    
    data class CurrentAction(
        val type: ActionType,
        val noteTitle: String? = null,
        val stayTime: Long? = null,
        val interaction: InteractionType? = null,
        val interactionProb: Float? = null,
        val scrollType: ScrollType? = null
    )
    
    enum class ActionType {
        BROWSE,         // 浏览
        LIKE,           // 点赞
        COLLECT,        // 收藏
        SCROLL,         // 滑动
        SEARCH,         // 搜索
        REST            // 休息
    }
    
    enum class InteractionType {
        LIKE,
        COLLECT
    }
    
    enum class ScrollType {
        BEZIER,         // 贝塞尔曲线
        LINEAR          // 直线
    }
    
    data class TaskStats(
        val browseCount: Int = 0,
        val likeCount: Int = 0,
        val collectCount: Int = 0,
        val avgStayTime: Long = 0,
        val bezierScrollCount: Int = 0,
        val randomMistakeCount: Int = 0,
        val readingPauseCount: Int = 0
    )
}
