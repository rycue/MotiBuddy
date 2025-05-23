package com.motibuddy.app

import android.util.MutableInt
import androidx.compose.runtime.MutableIntState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PomodoroViewModel : ViewModel() {

    private var _segmentButtonSelectedIndex = MutableLiveData(0)
    var segmentButtonSelectedIndex: LiveData<Int> = _segmentButtonSelectedIndex

    val timeWork = 25 * 1000L
    val timeBreak = 10 * 1000L

    private val _timerJustReset = MutableStateFlow(true)
    val timerJustReset = _timerJustReset.asStateFlow()

    private var _timeLeft = MutableStateFlow(timeWork)
    val timeLeft = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private var timerJob: Job? = null

    /** Starts or resumes the timer. */
    fun runTimer(onFinish: () -> Unit) {
        if (_isRunning.value) return
        _isRunning.value = true
        _timerJustReset.value = false
        timerJob =viewModelScope.launch {
            while (_isRunning.value && _timeLeft.value > 0L) {
                delay(1000L)
                _timeLeft.value -= 1000L
            }
            if (_timeLeft.value == 0L) {
                _isRunning.value = false
                onFinish()
            }
        }
    }

    fun stopTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer(workMode: Boolean) {
        stopTimer()
        _isRunning.value = false
        if (workMode) {
            _timeLeft.value = timeWork
        } else _timeLeft.value = timeBreak
        _timerJustReset.value = true
    }

    fun startTickLoop() {
        runTimer(onFinish = {}) // no-op if already finished
    }

    fun setSegmentButtonSelectedIndex(index: Int) {
        _segmentButtonSelectedIndex.value = index
    }
}
