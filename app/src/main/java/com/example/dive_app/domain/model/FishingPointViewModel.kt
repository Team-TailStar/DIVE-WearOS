package com.example.dive_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.dive_app.domain.model.FishingPoint

class FishingPointViewModel : ViewModel() {
    private val _points = MutableStateFlow<List<FishingPoint>>(emptyList())
    val points: StateFlow<List<FishingPoint>> = _points

    init {
        // ⚡ 예시 데이터 삽입
        _points.value = listOf(
            FishingPoint(
                name = "부산광역시",
                point_nm = "광안리 해수욕장 포인트",
                dpwt = "5m",
                material = "모래",
                tide_time = "4물",
                target = "숭어, 도다리",
                lat = 35.1532,
                lon = 129.1186,
                point_dt = "3km"
            ),
            FishingPoint(
                name = "제주특별자치도",
                point_nm = "성산일출봉 포인트",
                dpwt = "10m",
                material = "암초",
                tide_time = "7물",
                target = "우럭, 노래미",
                lat = 33.4589,
                lon = 126.9425,
                point_dt = "3km"
            )
        )
    }

    fun updatePoints(newPoints: List<FishingPoint>) {
        _points.value = newPoints
    }
}
