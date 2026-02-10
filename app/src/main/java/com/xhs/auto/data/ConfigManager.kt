package com.xhs.auto.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.xhs.auto.data.model.BehaviorConfig

class ConfigManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "config_prefs"
        private const val KEY_BEHAVIOR_CONFIG = "behavior_config"
        
        @Volatile
        private var instance: ConfigManager? = null
        
        fun getInstance(context: Context): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: ConfigManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    fun getBehaviorConfig(): BehaviorConfig {
        val json = prefs.getString(KEY_BEHAVIOR_CONFIG, null)
        return if (json != null) {
            try {
                gson.fromJson(json, BehaviorConfig::class.java)
            } catch (e: Exception) {
                BehaviorConfig()
            }
        } else {
            BehaviorConfig()
        }
    }
    
    fun saveBehaviorConfig(config: BehaviorConfig) {
        val json = gson.toJson(config)
        prefs.edit().putString(KEY_BEHAVIOR_CONFIG, json).apply()
    }
    
    fun updateBrowseConfig(browseConfig: BehaviorConfig.BrowseConfig) {
        val config = getBehaviorConfig().copy(browseConfig = browseConfig)
        saveBehaviorConfig(config)
    }
    
    fun updateInteractionConfig(interactionConfig: BehaviorConfig.InteractionConfig) {
        val config = getBehaviorConfig().copy(interactionConfig = interactionConfig)
        saveBehaviorConfig(config)
    }
    
    fun updateTagConfig(tagConfig: BehaviorConfig.TagConfig) {
        val config = getBehaviorConfig().copy(tagConfig = tagConfig)
        saveBehaviorConfig(config)
    }
    
    fun updateSearchConfig(searchConfig: BehaviorConfig.SearchConfig) {
        val config = getBehaviorConfig().copy(searchConfig = searchConfig)
        saveBehaviorConfig(config)
    }
    
    fun updateHumanBehaviorConfig(humanBehaviorConfig: BehaviorConfig.HumanBehaviorConfig) {
        val config = getBehaviorConfig().copy(humanBehaviorConfig = humanBehaviorConfig)
        saveBehaviorConfig(config)
    }
    
    fun resetToDefault() {
        saveBehaviorConfig(BehaviorConfig())
    }
    
    fun validateConfig(config: BehaviorConfig): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 验证浏览配置
        with(config.browseConfig) {
            if (dailyTargetMin < 50 || dailyTargetMax > 200) {
                errors.add("每日浏览目标应在50-200之间")
            }
            if (dailyTargetMin > dailyTargetMax) {
                errors.add("最小目标不能大于最大目标")
            }
            if (stayTimeMin > stayTimeMax) {
                errors.add("最小停留时间不能大于最大停留时间")
            }
            if (stayTimeMean < stayTimeMin || stayTimeMean > stayTimeMax) {
                errors.add("平均停留时间应在最小和最大之间")
            }
        }
        
        // 验证互动配置
        with(config.interactionConfig) {
            if (targetContentProbMin > targetContentProbMax) {
                errors.add("目标内容互动概率范围无效")
            }
            if (otherContentProbMin > otherContentProbMax) {
                errors.add("其他内容互动概率范围无效")
            }
            if (likeRatio + collectRatio != 1.0f) {
                errors.add("点赞和收藏比例之和应等于1")
            }
        }
        
        // 验证标签配置
        with(config.tagConfig) {
            if (targetTags.isEmpty()) {
                errors.add("至少需要设置一个目标标签")
            }
            if (targetTags.size > 20 || whitelistTags.size > 20) {
                errors.add("标签数量不能超过20个")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val errors: List<String>) : ValidationResult()
    }
}
