package com.xhs.auto.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.xhs.auto.data.AccountManager
import java.util.Calendar

class TaskAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "TaskAlarmReceiver"
        private const val REQUEST_CODE = 1001
        
        // 默认启动时间：9:00
        private const val DEFAULT_START_HOUR = 9
        private const val DEFAULT_START_MINUTE = 0
        
        /**
         * 设置定时任务
         */
        fun scheduleTask(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TaskAlarmReceiver::class.java)
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // 设置每天9:00触发
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, DEFAULT_START_HOUR)
                set(Calendar.MINUTE, DEFAULT_START_MINUTE)
                set(Calendar.SECOND, 0)
                
                // 如果今天的时间已过，设置为明天
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            // 设置精确闹钟
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Task scheduled for ${calendar.time}")
        }
        
        /**
         * 取消定时任务
         */
        fun cancelTask(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TaskAlarmReceiver::class.java)
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Task cancelled")
        }
    }
    
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "Alarm received, starting task...")
        
        // 检查是否有默认账号
        val accountManager = AccountManager.getInstance(context)
        val defaultAccount = accountManager.getDefaultAccount()
        
        if (defaultAccount == null) {
            Log.w(TAG, "No default account found")
            return
        }
        
        // 启动Accessibility Service执行任务
        // 注意：Accessibility Service需要通过用户界面启动
        // 这里发送广播通知主界面启动任务
        val startIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.xhs.auto.action.START_SCHEDULED_TASK"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("account_id", defaultAccount.id)
        }
        context.startActivity(startIntent)
        
        // 重新设置明天的定时任务
        scheduleTask(context)
    }
}
