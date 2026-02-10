package com.xhs.auto.data.model

data class BehaviorConfig(
    // 浏览设置
    val browseConfig: BrowseConfig = BrowseConfig(),
    // 互动设置
    val interactionConfig: InteractionConfig = InteractionConfig(),
    // 标签设置
    val tagConfig: TagConfig = TagConfig(),
    // 搜索设置
    val searchConfig: SearchConfig = SearchConfig(),
    // 人类化行为
    val humanBehaviorConfig: HumanBehaviorConfig = HumanBehaviorConfig()
) {
    data class BrowseConfig(
        val dailyTargetMin: Int = 100,
        val dailyTargetMax: Int = 200,
        val stayTimeMean: Int = 8,      // 正态分布均值（秒）
        val stayTimeStdDev: Int = 4,    // 正态分布标准差
        val stayTimeMin: Int = 3,       // 最小停留时间
        val stayTimeMax: Int = 20       // 最大停留时间
    )
    
    data class InteractionConfig(
        val targetContentProbMin: Float = 0.03f,    // 目标内容最小互动概率（3%）
        val targetContentProbMax: Float = 0.08f,    // 目标内容最大互动概率（8%）
        val otherContentProbMin: Float = 0.01f,     // 其他内容最小互动概率（1%）
        val otherContentProbMax: Float = 0.03f,     // 其他内容最大互动概率（3%）
        val likeRatio: Float = 0.7f,                // 点赞比例
        val collectRatio: Float = 0.3f              // 收藏比例
    )
    
    data class TagConfig(
        val targetTags: List<String> = listOf("玩偶", "毛绒玩具", "玩具公仔"),
        val whitelistTags: List<String> = listOf("手工DIY", "礼物推荐")
    )
    
    data class SearchConfig(
        val searchCountMin: Int = 3,
        val searchCountMax: Int = 5
    )
    
    data class HumanBehaviorConfig(
        val enabled: Boolean = true,
        val bezierScroll: Boolean = true,           // 贝塞尔曲线滑动
        val randomMistakes: Boolean = true,         // 随机误操作
        val readingPause: Boolean = true            // 阅读停顿
    )
    
    companion object {
        const val PREFS_KEY = "behavior_config"
        const val DEFAULT_DAILY_TARGET = 150
    }
}
