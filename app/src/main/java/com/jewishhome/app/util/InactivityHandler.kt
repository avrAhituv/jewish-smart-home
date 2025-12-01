package com.jewishhome.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Handles user inactivity detection for screensaver trigger
 */
class InactivityHandler(
    private val scope: CoroutineScope,
    private val defaultTimeoutMinutes: Int = 5
) {
    private var timeoutJob: Job? = null
    private var timeoutMinutes: Int = defaultTimeoutMinutes
    private var isEnabled: Boolean = true

    private val _shouldShowScreensaver = MutableStateFlow(false)
    val shouldShowScreensaver: StateFlow<Boolean> = _shouldShowScreensaver.asStateFlow()

    /**
     * Call this on any user interaction to reset the timer
     */
    fun onUserActivity() {
        _shouldShowScreensaver.value = false
        resetTimer()
    }

    /**
     * Start monitoring for inactivity
     */
    fun start() {
        resetTimer()
    }

    /**
     * Stop monitoring
     */
    fun stop() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    /**
     * Update timeout duration
     */
    fun setTimeoutMinutes(minutes: Int) {
        timeoutMinutes = minutes
        if (isEnabled) {
            resetTimer()
        }
    }

    /**
     * Enable or disable screensaver
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled) {
            resetTimer()
        } else {
            stop()
        }
    }

    /**
     * Dismiss screensaver (called when user touches screensaver)
     */
    fun dismissScreensaver() {
        _shouldShowScreensaver.value = false
        resetTimer()
    }

    private fun resetTimer() {
        timeoutJob?.cancel()
        if (!isEnabled) return

        timeoutJob = scope.launch {
            delay(timeoutMinutes * 60 * 1000L)
            _shouldShowScreensaver.value = true
        }
    }
}
