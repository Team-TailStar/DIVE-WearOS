package com.example.dive_app.domain.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.naver.maps.map.MapView

class LocationViewModel(app: Application) : AndroidViewModel(app) {
    private val _location = MutableLiveData<Pair<Double, Double>?>()
    val location: LiveData<Pair<Double, Double>?> get() = _location

    // ✅ MapView를 싱글톤처럼 ViewModel에 보관
    val mapView: MapView = MapView(app).apply {
        onCreate(null)
        onStart()
        onResume()
    }

    fun updateLocation(lat: Double, lon: Double) {
        _location.value = Pair(lat, lon)
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel이 완전히 파괴될 때 MapView도 정리
        mapView.onPause()
        mapView.onStop()
        mapView.onDestroy()
    }
}