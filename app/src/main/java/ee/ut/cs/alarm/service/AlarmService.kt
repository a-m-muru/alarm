package ee.ut.cs.alarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import ee.ut.cs.alarm.R

class AlarmService : Service() {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val tag = "AlarmService"
    
    // Store alarm data
    private var alarmId: String? = null
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0
    private var alarmLabel: String? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "alarm_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "AlarmService created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "AlarmService onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            "START_ALARM" -> {
                // Store alarm data from intent
                alarmId = intent.getStringExtra("alarm_id")
                alarmHour = intent.getIntExtra("alarm_hour", 0)
                alarmMinute = intent.getIntExtra("alarm_minute", 0)
                alarmLabel = intent.getStringExtra("alarm_label")
                
                Log.d(tag, "Stored alarm data - ID: $alarmId, Time: $alarmHour:$alarmMinute, Label: $alarmLabel")
                
                startAlarm()
                startForegroundService()
            }
            "STOP_ALARM" -> {
                stopAlarm()
                stopSelf()
            }
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAlarm() {
        Log.d(tag, "Starting alarm")
        startAlarmSound()
        startVibration()
    }

    private fun stopAlarm() {
        Log.d(tag, "Stopping alarm")
        stopAlarmSound()
        stopVibration()
    }

    private fun startAlarmSound() {
        try {
            mediaPlayer = MediaPlayer().apply {
                // Use system default alarm sound directly
                val defaultAlarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                Log.d(tag, "Using default alarm URI: $defaultAlarmUri")
                
                setDataSource(this@AlarmService, defaultAlarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.d(tag, "Alarm sound started successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to start alarm sound", e)
            // Try alternative approach with ringtone
            try {
                val ringtone = android.media.RingtoneManager.getRingtone(
                    this@AlarmService,
                    android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                )
                ringtone?.isLooping = true
                ringtone?.play()
                Log.d(tag, "Ringtone started as fallback")
            } catch (ex: Exception) {
                Log.e(tag, "Failed to start ringtone fallback", ex)
            }
        }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                Log.d(tag, "Alarm sound stopped")
            } catch (e: Exception) {
                Log.e(tag, "Error stopping alarm sound", e)
            }
        }
        mediaPlayer = null
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationPattern = longArrayOf(0, 1000, 1000) // Wait 0ms, vibrate 1000ms, wait 1000ms
            val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, 0)
            vibrator?.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
        }
        Log.d(tag, "Vibration started")
    }

    private fun stopVibration() {
        vibrator?.cancel()
        Log.d(tag, "Vibration stopped")
    }

    private fun startForegroundService() {
        // Use stored alarm data
        val timeString = String.format("%02d:%02d", alarmHour, alarmMinute)
        
        val notificationIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("alarm_hour", alarmHour)
            putExtra("alarm_minute", alarmMinute)
            putExtra("alarm_label", alarmLabel)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Active - $timeString")
            .setContentText(alarmLabel ?: "Tap to open alarm")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Service Channel"
            val descriptionText = "Channel for Alarm Service notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "AlarmService destroyed")
        stopAlarm()
    }
}
