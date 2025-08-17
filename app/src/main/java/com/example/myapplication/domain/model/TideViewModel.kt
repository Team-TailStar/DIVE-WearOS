package com.example.myapplication.domain.model

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

data class TideUiState(
    val tideList: List<TideInfoData> = emptyList()
)

class TideViewModel : ViewModel() {
    private val _uiState = mutableStateOf(TideUiState(
        tideList = listOf(
            TideInfoData(
                date = "2025-08-16",
                name = "부산",
                mul = "4물",
                sun = "05:30/19:10",
                moon = "07:15/20:05",
                jowi1 = "03:10 만조",
                jowi2 = "09:45 간조",
                jowi3 = "15:30 만조",
                jowi4 = "22:05 간조"
            ),
            TideInfoData(
                date = "2025-08-17",
                name = "부산",
                mul = "5물",
                sun = "05:31/19:08",
                moon = "08:00/20:40",
                jowi1 = "04:00 만조",
                jowi2 = "10:20 간조",
                jowi3 = "16:05 만조",
                jowi4 = "22:40 간조"
            )
        )
    )
    )
    val uiState: State<TideUiState> = _uiState

    fun updateTide(tideList: List<TideInfoData>) {
        _uiState.value = _uiState.value.copy(
            tideList = tideList
        )
    }
}