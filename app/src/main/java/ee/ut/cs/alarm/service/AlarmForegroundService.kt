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
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import ee.ut.cs.alarm.ACTION_STOP_ALARM
import ee.ut.cs.alarm.ACTION_STOP_VIBRATION
import ee.ut.cs.alarm.ALARM_INTENT_EXTRA_ALARM
import ee.ut.cs.alarm.ALARM_INTENT_EXTRA_MINIGAME_ID
import ee.ut.cs.alarm.AlarmActivity
import ee.ut.cs.alarm.AlarmApplication
import ee.ut.cs.alarm.R
import ee.ut.cs.alarm.currentDayIntUtc
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.repo.AlarmRepository
import ee.ut.cs.alarm.data.repo.AlarmRepositoryImpl
import ee.ut.cs.alarm.data.repo.UserPrefsRepositoryImpl
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.util.Calendar
import kotlin.random.Random

class AlarmForegroundService : Service() {
    var mediaPlayer: MediaPlayer? = null
    var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        val scope = this
        val settings = runBlocking { UserPrefsRepositoryImpl.getInstance(scope).getPrefs() }
        val songs = arrayListOf<Int>()
        runBlocking { settings.collect { value -> value.allowedTracks.forEach { v -> songs.add(v.alarmRes) } } }

        if (songs.size > 0) { // gpt
            mediaPlayer = MediaPlayer.create(this, songs[Random.nextInt(songs.size)])
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
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator?
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Log.d("ALARM FOREGROUND SERVICE", "!!start command!!")

        if (intent != null && intent.action != null) {
            if (intent.action.equals(ACTION_STOP_ALARM)) {
                Log.d("ALARM FOREGROUND SERVICE", "stop alarm command received")
                stopAlarm(AlarmRepositoryImpl.getInstance(this))
                return START_NOT_STICKY
            } else if (intent.action.equals(ACTION_STOP_VIBRATION)) {
                stopVibration()
                return START_NOT_STICKY
            }
        }

        val alarm: Alarm? =
            (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(ALARM_INTENT_EXTRA_ALARM, Alarm::class.java)
                } else {
                    intent?.getParcelableExtra<Alarm>(ALARM_INTENT_EXTRA_ALARM)
                }
            )
        if (alarm == null) {
            Log.e("ALARM FOREGROUND SERVICE", "alarm in parcel was null, stopping anything")
            return START_NOT_STICKY
        }

        val minigameId = intent?.getIntExtra(ALARM_INTENT_EXTRA_MINIGAME_ID, -999)

        val alarmIntent = Intent(this, AlarmActivity::class.java)
        alarmIntent
            .putExtra(ALARM_INTENT_EXTRA_ALARM, alarm)
            .putExtra(ALARM_INTENT_EXTRA_MINIGAME_ID, minigameId)

        for (a in alarmIntent.extras?.keySet()!!) {
            Log.d("ALARM FOREGROUND SERVICE", a)
        }
        Log.d(
            "ALARM FOREGROUND SERVICE",
            "minigame id should be " + alarmIntent.getIntExtra(ALARM_INTENT_EXTRA_MINIGAME_ID, -3),
        )
        AlarmApplication.singletonAlarm = alarm
        AlarmApplication.singletonMinigameId = minigameId
        Log.d(
            "ALARM FOREGROUND SERVICE",
            "singleton alarm should be " + AlarmApplication.singletonAlarm,
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

        playAlarmSound()

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
        val hour = alarm.time.toInt() / 3600
        cal.set(Calendar.HOUR_OF_DAY, hour)
        if (hour != 0) {
            cal.set(Calendar.MINUTE, (alarm.time.toInt() / 60) % (hour * 60))
        } else {
            cal.set(Calendar.MINUTE, alarm.time.toInt() / 60)
        }
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

    private fun playAlarmSound() {
        mediaPlayer?.start() // Start playback
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d("ALARM FOREGROUND SERVICE", "starting repeating vibration")
                var vibraLength: Long
                var vibraDelay: Long
                val rand = Random.nextFloat()
                if (rand > 0.8) {
                    vibraLength = Random.nextLong(5000, 10000)
                    vibraDelay = Random.nextLong(100, 10000)
                } else if (rand > 0.5) {
                    vibraLength = Random.nextLong(500, 2000)
                    vibraDelay = Random.nextLong(15, 6000)
                } else {
                    vibraLength = Random.nextLong(50, 100)
                    vibraDelay = Random.nextLong(10, 100)
                }
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, vibraLength, vibraDelay), 0),
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("ALARM FOREGROUND SERVICE", "starting one-time vibration")
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.EFFECT_HEAVY_CLICK,
                    ),
                )
            } else {
                vibrator?.vibrate(500)
            }
        }
    }

    // thanx CHATGPT 💝💝💝💝💝💝💝
    private fun stopAlarmSound() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        stopVibration()
    }

    private fun stopVibration() {
        if (vibrator != null) {
            vibrator?.cancel()
        }
    }

    private fun stopAlarm(repo: AlarmRepository) {
        Log.d("ALARM FOREGROUND SERVICE", "stoppiong alarm")
        if (AlarmApplication.singletonStreak == null) {
            throw RuntimeException(
                "SINGLETON STREAK IS NULL WHEN STOPPING ALARM... IT ALL COMES CRASHING DOWN... THERE IS NOTHING LEFT... GIVE UP.",
            )
        }
        val day = currentDayIntUtc()
        // only increment if we haven't incremented today
        val lastStreakDay = runBlocking { repo.getLastStreakDay() }
        if (lastStreakDay < day) {
            AlarmApplication.singletonStreak =
                AlarmApplication.singletonStreak!! + 1
        }
        runBlocking { repo.setLastStreakDay(day) }
        Log.d("ALARM FOREGROUND SERVICE", "last streak day: $lastStreakDay")
        runBlocking { repo.setStreak(AlarmApplication.singletonStreak!!) }
        AlarmApplication.singletonStreak = null
        stopAlarmSound()
        stopVibration()
        stopSelf()
    }
}
