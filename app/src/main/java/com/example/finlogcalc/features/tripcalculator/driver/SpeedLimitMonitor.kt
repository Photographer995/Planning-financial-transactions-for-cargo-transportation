package com.example.finlogcalc.features.tripcalculator.driver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.finlogcalc.R

/**
 * Мониторинг превышения скорости и уведомления
 */
class SpeedLimitMonitor(private val context: Context) {
    private var speedLimitKmh: Double = 60.0 // Лимит скорости по умолчанию
    private var lastNotificationTime: Long = 0
    private val notificationCooldownMs = 30000L // 30 секунд между уведомлениями
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    init {
        createNotificationChannel()
    }
    
    fun setSpeedLimit(limitKmh: Double) {
        speedLimitKmh = limitKmh
    }
    
    fun checkSpeed(currentSpeedKmh: Double, hapticFeedback: HapticFeedback? = null) {
        if (currentSpeedKmh > speedLimitKmh) {
            val now = System.currentTimeMillis()
            if (now - lastNotificationTime > notificationCooldownMs) {
                showSpeedWarningNotification(currentSpeedKmh, speedLimitKmh)
                hapticFeedback?.performHaptic(HapticType.WARNING)
                lastNotificationTime = now
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SPEED_WARNING_CHANNEL_ID,
                "Предупреждения о скорости",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о превышении скорости"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showSpeedWarningNotification(currentSpeed: Double, limit: Double) {
        val speedOver = (currentSpeed - limit).toInt()
        val notification = NotificationCompat.Builder(context, SPEED_WARNING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Превышение скорости")
            .setContentText("Текущая скорость: ${currentSpeed.toInt()} км/ч (превышение на $speedOver км/ч)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(SPEED_WARNING_NOTIFICATION_ID, notification)
    }
    
    companion object {
        private const val SPEED_WARNING_CHANNEL_ID = "speed_warning_channel"
        private const val SPEED_WARNING_NOTIFICATION_ID = 1001
    }
}

