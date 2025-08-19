package com.example.dive_app.data.repository

import android.util.Log
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.model.WeatherData
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import org.json.JSONArray
import org.json.JSONObject

class WearDataRepository(
    private val weatherViewModel: WeatherViewModel,
    private val tideViewModel: TideViewModel,
    private val fishingPointViewModel: FishingPointViewModel,
    private val locationViewModel: LocationViewModel
) {
    fun handleMessage(path: String, data: String) {
        try {
            when (path) {
                "/response_weather" -> handleWeather(data)
                "/response_tide" -> handleTide(data)
                "/response_point" -> handlePoints(data)
                "/response_location" -> handleLocation(data)
                "/response_air_quality" -> handleAirQuality(data)
                else -> Log.d("WatchMsg", "📩 알 수 없는 응답 → path=$path, data=$data")
            }
        } catch (e: Exception) {
            Log.e("WatchMsg", "⚠️ 데이터 처리 오류 (${path}): ${e.message}")
        }
    }

    private fun handleWeather(data: String) {
        val json = JSONObject(data)
        val weather = WeatherData(
            sky = json.getString("sky"),
            temp = json.getString("temp"),
            humidity = json.getString("humidity"),
            windspd = json.getString("windspd"),
            rain = json.getString("rain"),
            winddir = json.getString("winddir"),
            waveHt = json.getString("waveHt"),
            waveDir = json.getString("waveDir"),
            obsWt = json.getString("obsWt")
        )
        weatherViewModel.updateWeather(weather)
        Log.d("WatchMsg", "✅ 날씨 업데이트 완료")
    }

    private fun handleTide(data: String) {
        val json = JSONObject(data)
        val tidesArray = JSONArray(json.getString("tides"))

        val tideList = mutableListOf<TideInfoData>()
        for (i in 0 until tidesArray.length()) {
            val obj = tidesArray.getJSONObject(i)
            tideList.add(
                TideInfoData(
                    pThisDate = obj.getString("pThisDate"),
                    pName = obj.getString("pName"),
                    pMul = obj.getString("pMul"),
                    pSun = obj.getString("pSun"),
                    pMoon = obj.getString("pMoon"),
                    jowi1 = obj.optString("jowi1", ""),
                    jowi2 = obj.optString("jowi2", ""),
                    jowi3 = obj.optString("jowi3", ""),
                    jowi4 = obj.optString("jowi4", "")
                )
            )
        }
        tideViewModel.updateTide(tideList)
        Log.d("WatchMsg", "✅ 조석 업데이트 완료")
    }

    private fun handlePoints(data: String) {
        val json = JSONObject(data)
        val pointsArray = JSONArray(json.getString("points"))

        val pointList = mutableListOf<FishingPoint>()
        for (i in 0 until pointsArray.length()) {
            val obj = pointsArray.getJSONObject(i)
            pointList.add(
                FishingPoint(
                    name = obj.getString("name"),
                    point_nm = obj.getString("point_nm"),
                    dpwt = obj.getString("dpwt"),
                    material = obj.getString("material"),
                    tide_time = obj.getString("tide_time"),
                    target = obj.getString("target"),
                    lat = obj.getDouble("lat"),
                    lon = obj.getDouble("lon"),
                    point_dt = obj.getString("point_dt")
                )
            )
        }
        fishingPointViewModel.updatePoints(pointList)
        Log.d("WatchMsg", "✅ 포인트 업데이트 완료")
    }

    private fun handleLocation(data: String) {
        val json = JSONObject(data)
        val lat = json.getDouble("lat")
        val lon = json.getDouble("lon")
        locationViewModel.updateLocation(lat, lon)
        Log.d("WatchMsg", "✅ 위치 업데이트 완료")
    }

    private fun handleAirQuality(data: String) {
        Log.d("WatchMsg", "✅ 미세먼지 업데이트 완료")
    }
}
