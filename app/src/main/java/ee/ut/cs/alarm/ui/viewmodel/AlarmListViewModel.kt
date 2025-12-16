package ee.ut.cs.alarm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.data.repo.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmListViewModelFactory(
    private val repo: AlarmRepository,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AlarmListViewModel(repo) as T
}

class AlarmListViewModel(
    private val repo: AlarmRepository,
) : ViewModel() {
    private val _items = MutableStateFlow<List<Alarm>>(emptyList())
    val items = _items.asStateFlow()

    // thanks chatgpt
    private val _streak = MutableStateFlow(0)
    val streak = _streak.asStateFlow()

    // thanks chatgpt
    private val _previousStreak = MutableStateFlow(0)
    val previousStreak = _previousStreak.asStateFlow()

    init {
        viewModelScope.launch {
            repo
                .getAlarms()
                .collect { alarms -> _items.value = alarms.sortedBy { alarm -> alarm.time } }
        }
        // chat gpt
        viewModelScope.launch {
            repo.streakFlow().collect { _streak.value = it }
        }
        // chat gpt
        viewModelScope.launch {
            repo.previousStreakFlow().collect { _previousStreak.value = it }
        }
    }

    fun hasAlarm(alarm: Alarm): Boolean =
        _items.value
            .filter {
                it.id == alarm.id
            }.size == 1

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repo.saveAlarm(alarm)
        }
        _items.value += alarm
        _items.value = _items.value.sortedBy { alarm -> alarm.time }
    }

    fun removeAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repo.deleteAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repo.saveAlarm(alarm)
        }
        _items.value = _items.value.sortedBy { alarm -> alarm.time }
    }

    fun toggleEnabled(alarm: Alarm) {
        updateAlarm(alarm.copy(enabled = !alarm.enabled))
    }
}
