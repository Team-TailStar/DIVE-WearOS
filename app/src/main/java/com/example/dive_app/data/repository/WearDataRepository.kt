package com.example.dive_app.data.repository

import android.util.Log
import com.example.dive_app.domain.model.AirQuality
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.viewmodel.LocationViewModel
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.viewmodel.TideViewModel
import com.example.dive_app.domain.model.WeatherData
import com.example.dive_app.domain.viewmodel.AirQualityViewModel
import com.example.dive_app.domain.viewmodel.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import org.json.JSONArray
import org.json.JSONObject

class WearDataRepository(
    private val weatherViewModel: WeatherViewModel,
    private val tideViewModel: TideViewModel,
    private val fishingPointViewModel: FishingPointViewModel,
    private val locationViewModel: LocationViewModel,
    private val airQualityViewModel: AirQualityViewModel
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
        val tidesArray = json.getJSONArray("tides")

        val tideList = mutableListOf<TideInfoData>()
        for (i in 0 until tidesArray.length()) {
            val obj = tidesArray.getJSONObject(i)
            tideList.add(
                TideInfoData(
                    pThisDate = obj.getString("pThisDate"),
                    pName = obj.getString("pSelArea"),
                    pMul = obj.getString("pMul"),
                    pSun = obj.getString("pSun"),
                    pMoon = obj.getString("pMoon"),
                    jowi1 = obj.optString("pTime1", ""),
                    jowi2 = obj.optString("pTime2", ""),
                    jowi3 = obj.optString("pTime3", ""),
                    jowi4 = obj.optString("pTime4", "")
                )
            )
        }
        tideViewModel.updateTide(tideList)
        Log.d("WatchMsg", "✅ 조석 업데이트 완료")
    }

    private fun handlePoints(data: String) {
        val json = JSONObject(data)
        val pointsArray: JSONArray? = json.optJSONArray("points")

        val pointList = mutableListOf<FishingPoint>()
        if (pointsArray != null) {
            for (i in 0 until pointsArray.length()) {
                val obj = pointsArray.getJSONObject(i)
                pointList.add(
                    FishingPoint(
                        name = obj.optString("name"),  
                        point_nm = obj.optString("point_nm"),
                        dpwt = obj.optString("dpwt"),
                        material = obj.optString("material"),
                        tide_time = obj.optString("tide_time"),
                        target = obj.optString("target"),
                        lat = obj.optDouble("lat", 0.0),
                        lon = obj.optDouble("lon", 0.0),
                        point_dt = obj.optString("point_dt")
                    )
                )
            }
        } else {
            Log.e("WatchMsg", "⚠️ 데이터가 없음 : /response_point")
        }
        fishingPointViewModel.updatePoints(pointList)
        Log.d("WatchMsg", "✅ 포인트 업데이트 완료 ${pointList}")
    }
    

    private fun handleLocation(data: String) {
        val json = JSONObject(data)
        val lat = json.getDouble("lat")
        val lon = json.getDouble("lon")
        locationViewModel.updateLocation(lat, lon)
        Log.d("WatchMsg", "✅ 위치 업데이트 완료")
    }

    private fun handleAirQuality(data: String) {
        val json = JSONObject(data)
        val airQuality = AirQuality(
            no2Value = json.optDouble("no2Value", -1.0),
            o3Value = json.optDouble("o3Value", -1.0),
            pm10Value = json.optDouble("pm10Value", -1.0),
            pm25Value = json.optDouble("pm25Value", -1.0),
            o3Grade = json.optInt("o3Grade", -1),
            no2Grade = json.optInt("no2Grade", -1),
            pm10Grade = json.optInt("pm10Grade", -1),
            pm25Grade = json.optInt("pm25Grade", -1),
        )
        Log.d("WatchMsg", "미세먼지 데이터: $data")
        airQualityViewModel.updateAirQuality(airQuality)
        Log.d("WatchMsg", "✅ 미세먼지 업데이트 완료: $airQuality")
    }
}
