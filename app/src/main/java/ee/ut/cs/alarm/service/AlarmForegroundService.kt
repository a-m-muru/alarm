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
import android.util.Log
import androidx.core.app.NotificationCompat
import ee.ut.cs.alarm.AlarmActivity
import ee.ut.cs.alarm.R
import ee.ut.cs.alarm.data.Alarm
import java.text.DateFormat
import java.util.Calendar
import kotlin.random.Random

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
        if (intent != null && intent.action != null) {
            if (intent.action.equals("STOP_ALARM")) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val alarm: Alarm? =
            (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra("ut.cs.alarm.alarm", Alarm::class.java)
                } else {
                    intent?.getParcelableExtra<Alarm>("ut.cs.alarm.alarm")
                }
            )
        if (alarm == null) {
            Log.e("ALARM FOREGROUND SERVICE", "alarm in parcel was null, stopping anything")
            return START_NOT_STICKY
        }

        val minigameId = intent?.getIntExtra("ut.cs.alarm.minigameId", -999)

        val alarmIntent = Intent(this, AlarmActivity::class.java)
        alarmIntent
            .putExtra("ut.cs.alarm.alarm", alarm)
            .putExtra("ut.cs.alarm.minigameId", minigameId)

        for (a in alarmIntent.extras?.keySet()!!) {
            Log.d("ALARM FOREGROUND SERVICE", a)
        }
        Log.d(
            "ALARM FOREGROUND SERVICE",
            "minigame id should be " + alarmIntent.getIntExtra("ut.cs.alarm.minigameId", -3),
        )
        sendNotification(
            this,
            PendingIntent.getActivity(
                this,
                1,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
            alarm,
        )

        mediaPlayer?.start()

        return START_STICKY
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }

    // thanx CHATGPT 💝💝💝💝💝💝💝
    fun sendNotification(
        ctx: Context,
        notifIntent: PendingIntent,
        alarm: Alarm,
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
        val timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT)
        val cal = Calendar.getInstance()
        cal.timeInMillis = alarm.time.toLong() * 1000
        val titleTexts =
            arrayOf(
                "Rise and SHINE",
                "Your BEAUTIFUL DAY Starts Here",
                "Wake up Wake up Wake    Up",
                "Hey, you. You're finally awake",
                "Sleep no more ...  The world of the Awake awaits you",
                "ALARM",
                "WAKE UP",
            )
        val title = titleTexts[Random.nextInt(titleTexts.size)]

        val notifBuilder =
            NotificationCompat
                .Builder(ctx, channelID)
                .setSmallIcon(R.drawable.alarm_icon)
                .setContentTitle(title)
                .setContentText("Your ${timeFmt.format(cal.time)} alarm is here!\n${alarm.label}")
                .setContentIntent(notifIntent)
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
