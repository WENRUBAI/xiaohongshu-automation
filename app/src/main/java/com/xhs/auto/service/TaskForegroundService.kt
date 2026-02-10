package com.xhs.auto.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.xhs.auto.R
import com.xhs.auto.ui.MainActivity

class TaskForegroundService : Service() {
    
    companion object {
        private const val TAG = "TaskForegroundService"
        private const val CHANNEL_ID = "xhs_auto_channel"
        private const val NOTIFICATION_ID = 1
        
        const val ACTION_START = "com.xhs.auto.action.START_FOREGROUND"
        const val ACTION_STOP = "com.xhs.auto.action.STOP_FOREGROUND"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification())
                Log.d(TAG, "Foreground service started")
            }
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
                Log.d(TAG, "Foreground service stopped")
            }
        }
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "小红书养号助手",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持养号任务在后台运行"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("小红书养号助手")
            .setContentText("任务正在运行中...")
            .setSmallIcon(android.R.drawable.ic_menu_rotate)  // 使用系统图标，实际项目应替换
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
