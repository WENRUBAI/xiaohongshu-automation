package com.xhs.auto.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xhs.auto.databinding.ActivityTaskBinding
import com.xhs.auto.service.XhsAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TaskActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTaskBinding
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTaskStatus()
            handler.postDelayed(this, 1000)  // 每秒更新一次
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }
    
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }
    
    private fun setupUI() {
        // 返回按钮
        binding.btnBack.setOnClickListener { finish() }
    }
    
    private fun updateTaskStatus() {
        val taskStatus = XhsAccessibilityService.currentTaskStatus
        
        if (taskStatus != null && XhsAccessibilityService.isRunning) {
            // 更新进度
            val progress = taskStatus.progress
            binding.tvProgress.text = "${progress.completed} / ${progress.target} 篇"
            binding.progressBar.max = progress.target
            binding.progressBar.progress = progress.completed
            
            // 更新时间
            val elapsedTime = taskStatus.elapsedTime
            val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
            binding.tvElapsedTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            
            // 更新统计
            val stats = taskStatus.stats
            binding.tvBrowseCount.text = "浏览: ${stats.browseCount} 篇"
            binding.tvLikeCount.text = "点赞: ${stats.likeCount} 次"
            binding.tvCollectCount.text = "收藏: ${stats.collectCount} 次"
            
            // 更新当前操作
            val currentAction = taskStatus.currentAction
            binding.tvCurrentAction.text = currentAction?.let {
                "当前: ${it.type.name}"
            } ?: "当前: 准备中"
            
        } else {
            // 任务未运行
            binding.tvProgress.text = "0 / 0 篇"
            binding.progressBar.progress = 0
            binding.tvElapsedTime.text = "00:00:00"
            binding.tvBrowseCount.text = "浏览: 0 篇"
            binding.tvLikeCount.text = "点赞: 0 次"
            binding.tvCollectCount.text = "收藏: 0 次"
            binding.tvCurrentAction.text = "当前: 空闲"
        }
    }
}
