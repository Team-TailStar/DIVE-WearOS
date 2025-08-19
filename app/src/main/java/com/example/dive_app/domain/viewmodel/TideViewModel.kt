package com.example.dive_app.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.dive_app.domain.model.TideInfoData

data class TideUiState(
    val tideList: List<TideInfoData> = emptyList()
)

class TideViewModel : ViewModel() {
    private val _uiState = mutableStateOf(TideUiState())
    val uiState: State<TideUiState> = _uiState

    fun updateTide(tideList: List<TideInfoData>) {
        _uiState.value = _uiState.value.copy(
            tideList = tideList
        )
    }
}