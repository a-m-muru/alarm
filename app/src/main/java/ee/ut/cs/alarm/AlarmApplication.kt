package ee.ut.cs.alarm

import android.app.Application
import ee.ut.cs.alarm.data.repo.AlarmRepository
import ee.ut.cs.alarm.data.repo.AlarmRepositoryImpl

class AlarmApplication : Application() {
    lateinit var repo: AlarmRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repo = AlarmRepositoryImpl.getInstance(this)
    }
}