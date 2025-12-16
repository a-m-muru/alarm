package ee.ut.cs.alarm

import android.app.Application
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.repo.AlarmRepository
import ee.ut.cs.alarm.data.repo.AlarmRepositoryImpl

class AlarmApplication : Application() {
    companion object {
        // this is a valid object while the alarm foreground service is running
        // its nulled when the alarm is stopped
        var singletonAlarm: Alarm? = null
        var singletonMinigameId: Int? = null // similar story with this
        var singletonStreak: Int? = null // saving the streak in memory
    }

    lateinit var repo: AlarmRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repo = AlarmRepositoryImpl.getInstance(this)
    }
}
