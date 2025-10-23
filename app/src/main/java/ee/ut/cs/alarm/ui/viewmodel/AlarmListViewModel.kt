package ee.ut.cs.alarm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.Day
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.experimental.or

class AlarmListViewModelFactory(private val packageName: String) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AlarmListViewModel(packageName) as T
}

class AlarmListViewModel(private val packageName: String) : ViewModel() {
    private val _items = MutableStateFlow<List<Alarm>>(emptyList())
    val items = _items.asStateFlow()

    init {
        _items.value = listOf(
            Alarm(time = 30600u, days = Day.MONDAY or  Day.WEDNESDAY or Day.FRIDAY, label = "Wake up, time to go to work!", ringtoneUri = "android.resource://$packageName/ringtones/miku_ringtone.mp3", enabled = true),
            Alarm(time = 36000u, days = Day.TUESDAY, label = "Another wakeup call", ringtoneUri = "android.resource://$packageName/ringtones/miku_ringtone.mp3", enabled = true)
        )
    }

    fun hasAlarm(alarm: Alarm): Boolean {
        return _items.value.filter {
            it.id == alarm.id
        }.size == 1
    }

    fun addAlarm(alarm: Alarm) {
        _items.value = _items.value + alarm
    }

    fun removeAlarm(alarm: Alarm) {
        _items.value = _items.value - alarm
    }

    fun updateItem(alarm: Alarm) {
        _items.value = _items.value.map {
            if (it.id == alarm.id) alarm else it
        }
    }

    fun toggleEnabled(alarm: Alarm) {
        updateItem(alarm.copy(enabled = !alarm.enabled))
    }
}