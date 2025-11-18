package ee.ut.cs.alarm.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ee.ut.cs.alarm.R

class AlarmForegroundService : Service() {
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        // gept chat
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_1)
        // https://stackoverflow.com/a/78187672
        mediaPlayer?.setAudioAttributes(
            AudioAttributes
                .Builder()
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setLegacyStreamType(
                    AudioManager.STREAM_ALARM,
                ).setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(
                    AudioAttributes.CONTENT_TYPE_SONIFICATION,
                ).build(),
        )
        mediaPlayer?.isLooping = true
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val intent: Intent =
            (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra("alarmIntent", Intent::class.java)
                } else {
                    intent?.getParcelableExtra<Intent>("alarmIntent")
                }
            )!!
        sendNotification(
            this,
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE),
        )

        mediaPlayer?.start()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }

    // thanx CHATGPT 💝💝💝💝💝💝💝
    fun sendNotification(
        ctx: Context,
        intent: PendingIntent,
    ) {
        val channelID = "ALARM CHANNEL ALARM++ COOL CHANNEL"
        val channelName = "Alarm++ Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifManager = ctx.getSystemService(NotificationManager::class.java)
            var channel = notifManager.getNotificationChannel(channelID)

            if (channel == null) {
                channel =
                    NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
                notifManager.createNotificationChannel(channel)
            }
        }

        val notifBuilder =
            NotificationCompat
                .Builder(ctx, channelID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("WAKE nice UP!!!!!!!!!!")
                .setContentText(":) pls wake up \uD83D\uDC9D \uD83D\uDC9D \uD83D\uDC9D")
                .setContentIntent(intent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true)
//        val notifManager = ctx.getSystemService(NotificationManager::class.java)
//        notifManager.notify(1, notifBuilder.build())

        startForeground(1, notifBuilder.build())
    }

    // thanx CHATGPT 💝💝💝💝💝💝💝
    private fun playAlarmSound(ctx: Context) {
        if (mediaPlayer != null) {
            mediaPlayer?.release() // Release any existing MediaPlayer
        }
        mediaPlayer = MediaPlayer.create(ctx, R.raw.alarm_1)
        mediaPlayer?.setLooping(true) // Enable looping
        mediaPlayer?.start() // Start playback
    }

    // thanx CHATGPT 💝💝💝💝💝💝💝
    private fun stopAlarmSound() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}
