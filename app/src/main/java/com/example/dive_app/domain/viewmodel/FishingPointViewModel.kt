package com.example.dive_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.dive_app.domain.model.FishingPoint

class FishingPointViewModel : ViewModel() {
    private val _points = MutableStateFlow<List<FishingPoint>>(emptyList())
    val points: StateFlow<List<FishingPoint>> = _points

    fun updatePoints(newPoints: List<FishingPoint>) {
        _points.value = newPoints
    }
}
