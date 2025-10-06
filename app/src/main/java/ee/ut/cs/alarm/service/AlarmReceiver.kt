package ee.ut.cs.alarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ee.ut.cs.alarm.R

class AlarmReceiver : BroadcastReceiver() {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val alarmHour = intent.getIntExtra("alarm_hour", 0)
        val alarmMinute = intent.getIntExtra("alarm_minute", 0)
        val alarmLabel = intent.getStringExtra("alarm_label")

        // Start alarm activity/service
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("alarm_hour", alarmHour)
            putExtra("alarm_minute", alarmMinute)
            putExtra("alarm_label", alarmLabel)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        context.startActivity(alarmIntent)

        // Show notification
        showAlarmNotification(context, alarmHour, alarmMinute, alarmLabel)
        
        // Start ringtone and vibration
        startAlarmSound(context)
        startVibration(context)
    }

    private fun showAlarmNotification(context: Context, hour: Int, minute: Int, label: String?) {
        val timeString = String.format("%02d:%02d", hour, minute)
        val title = label ?: "Alarm"
        
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setContentTitle(title)
            .setContentText("Alarm at $timeString")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, notification)
    }

    private fun startAlarmSound(context: Context) {
        try {
            // Use default alarm sound
            val alarmUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Fallback to system default alarm sound
            try {
                val defaultAlarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, defaultAlarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (ex: Exception) {
                // If all else fails, just vibrate
            }
        }
    }

    private fun startVibration(context: Context) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationPattern = longArrayOf(0, 1000, 1000) // Wait 0ms, vibrate 1000ms, wait 1000ms
            val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, 0)
            vibrator?.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
        }
    }

    fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        
        vibrator?.cancel()
        vibrator = null
    }
}
