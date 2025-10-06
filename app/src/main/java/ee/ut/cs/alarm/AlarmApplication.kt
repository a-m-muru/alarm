package ee.ut.cs.alarm

import android.app.Application
import ee.ut.cs.alarm.data.AlarmRepository

class AlarmApplication : Application() {
    
    lateinit var alarmRepository: AlarmRepository
        private set

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository.getInstance(this)
    }
}
