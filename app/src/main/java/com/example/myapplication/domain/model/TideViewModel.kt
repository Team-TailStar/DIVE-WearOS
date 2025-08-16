package com.example.myapplication.domain.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State

class TideViewModel : ViewModel() {
    private val _uiState = mutableStateOf(TideInfoData())
    val uiState: State<TideInfoData> = _uiState

    fun updateTide(data: TideInfoData) {
        _uiState.value = data
    }
}