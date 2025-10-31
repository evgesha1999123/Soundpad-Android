package recorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    private var _timeInMillis = MutableStateFlow(0L)
    var timeInMillis: StateFlow<Long> = _timeInMillis.asStateFlow()

    private var timerJob: Job? = null
    private var startTime = 0L

    fun startTimer() {
        startTime = System.currentTimeMillis() - _timeInMillis.value
        timerJob = viewModelScope.launch {
            while (isActive) {
                _timeInMillis.value = System.currentTimeMillis() - startTime
                delay(500) // Обновление каждые 100мс для плавности
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        stopTimer()
        _timeInMillis.value = 0L
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}