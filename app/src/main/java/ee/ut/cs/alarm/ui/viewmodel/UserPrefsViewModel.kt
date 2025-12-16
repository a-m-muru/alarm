package ee.ut.cs.alarm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.alarm.data.UserPrefs
import ee.ut.cs.alarm.data.repo.UserPrefsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserPrefsViewModelFactory(
    private val repo: UserPrefsRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = UserPrefsViewModel(repo) as T
}

class UserPrefsViewModel(
    private val repo: UserPrefsRepository
) : ViewModel() {
    private val _prefs = MutableStateFlow(UserPrefs());
    val prefs = _prefs.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getPrefs().collect { _prefs.value = it }
        }
    }

    fun updatePrefs(prefs: UserPrefs) {
        viewModelScope.launch {
            repo.savePrefs(prefs)
        }
    }
}