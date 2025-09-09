package com.example.dive_app.domain.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppModeViewModel : ViewModel() {
    private val _mode = MutableStateFlow(AppMode.NORMAL)
    val mode: StateFlow<AppMode> = _mode

    fun setMode(m: AppMode) { _mode.value = m }
}
