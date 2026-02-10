package com.xhs.auto.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xhs.auto.data.ConfigManager
import com.xhs.auto.data.model.BehaviorConfig
import com.xhs.auto.databinding.ActivityConfigBinding

class ConfigActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConfigBinding
    private lateinit var configManager: ConfigManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        configManager = ConfigManager.getInstance(this)
        
        setupUI()
        loadConfig()
    }
    
    private fun setupUI() {
        // 返回按钮
        binding.btnBack.setOnClickListener { finish() }
        
        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveConfig()
        }
        
        // 重置按钮
        binding.btnReset.setOnClickListener {
            configManager.resetToDefault()
            loadConfig()
            Toast.makeText(this, "已重置为默认配置", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadConfig() {
        val config = configManager.getBehaviorConfig()
        
        // 加载浏览设置
        binding.etDailyTargetMin.setText(config.browseConfig.dailyTargetMin.toString())
        binding.etDailyTargetMax.setText(config.browseConfig.dailyTargetMax.toString())
        binding.etStayTimeMean.setText(config.browseConfig.stayTimeMean.toString())
        binding.etStayTimeStdDev.setText(config.browseConfig.stayTimeStdDev.toString())
        
        // 加载互动设置
        binding.etTargetProbMin.setText((config.interactionConfig.targetContentProbMin * 100).toInt().toString())
        binding.etTargetProbMax.setText((config.interactionConfig.targetContentProbMax * 100).toInt().toString())
        binding.etOtherProbMin.setText((config.interactionConfig.otherContentProbMin * 100).toInt().toString())
        binding.etOtherProbMax.setText((config.interactionConfig.otherContentProbMax * 100).toInt().toString())
        
        // 加载标签设置
        binding.etTargetTags.setText(config.tagConfig.targetTags.joinToString(", "))
        binding.etWhitelistTags.setText(config.tagConfig.whitelistTags.joinToString(", "))
        
        // 加载人类化行为设置
        binding.switchBezierScroll.isChecked = config.humanBehaviorConfig.bezierScroll
        binding.switchRandomMistakes.isChecked = config.humanBehaviorConfig.randomMistakes
        binding.switchReadingPause.isChecked = config.humanBehaviorConfig.readingPause
    }
    
    private fun saveConfig() {
        try {
            val config = BehaviorConfig(
                browseConfig = BehaviorConfig.BrowseConfig(
                    dailyTargetMin = binding.etDailyTargetMin.text.toString().toInt(),
                    dailyTargetMax = binding.etDailyTargetMax.text.toString().toInt(),
                    stayTimeMean = binding.etStayTimeMean.text.toString().toInt(),
                    stayTimeStdDev = binding.etStayTimeStdDev.text.toString().toInt()
                ),
                interactionConfig = BehaviorConfig.InteractionConfig(
                    targetContentProbMin = binding.etTargetProbMin.text.toString().toFloat() / 100f,
                    targetContentProbMax = binding.etTargetProbMax.text.toString().toFloat() / 100f,
                    otherContentProbMin = binding.etOtherProbMin.text.toString().toFloat() / 100f,
                    otherContentProbMax = binding.etOtherProbMax.text.toString().toFloat() / 100f
                ),
                tagConfig = BehaviorConfig.TagConfig(
                    targetTags = binding.etTargetTags.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    whitelistTags = binding.etWhitelistTags.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                ),
                humanBehaviorConfig = BehaviorConfig.HumanBehaviorConfig(
                    bezierScroll = binding.switchBezierScroll.isChecked,
                    randomMistakes = binding.switchRandomMistakes.isChecked,
                    readingPause = binding.switchReadingPause.isChecked
                )
            )
            
            // 验证配置
            when (val result = configManager.validateConfig(config)) {
                is ConfigManager.ValidationResult.Success -> {
                    configManager.saveBehaviorConfig(config)
                    Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ConfigManager.ValidationResult.Error -> {
                    Toast.makeText(this, result.errors.first(), Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "配置格式错误: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
