package com.example.dive_app.data.repository

import android.content.Context
import android.util.Log
import com.example.dive_app.domain.model.FishingPoint
import com.example.dive_app.domain.model.TideInfoData
import com.example.dive_app.domain.model.TideViewModel
import com.example.dive_app.domain.model.WeatherData
import com.example.dive_app.domain.model.WeatherViewModel
import com.example.dive_app.ui.viewmodel.FishingPointViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.PutDataMapRequest

class WearDataRepository(
    private val context: Context,
    private val fishingViewModel: FishingPointViewModel,
    private val weatherViewModel: WeatherViewModel,
    private val tideViewModel : TideViewModel
) : DataClient.OnDataChangedListener {

    private val dataClient: DataClient = Wearable.getDataClient(context)

    fun registerListener() {
        dataClient.addListener(this)
    }

    fun unregisterListener() {
        dataClient.removeListener(this)
    }

    // 요청 매서드
    fun requestWeather() {
        val request = PutDataMapRequest.create("/request_weather").apply {
            dataMap.putLong("time", System.currentTimeMillis()) // 매번 고유하게 보내기 위해 time 추가
        }
        val requestTask = dataClient.putDataItem(request.asPutDataRequest().setUrgent())
        requestTask.addOnSuccessListener {
            Log.d("WearDataRepo", "✅ 날씨 요청 전송 성공 → /request_weather")
        }.addOnFailureListener { e ->
            Log.e("WearDataRepo", "❌ 날씨 요청 전송 실패", e)
        }
    }

    fun requestTide() {
        val req = PutDataMapRequest.create("/request_tide")
        req.dataMap.putLong("ts", System.currentTimeMillis())
        dataClient.putDataItem(req.asPutDataRequest())
    }

    fun requestFishingPoints() {
        val req = PutDataMapRequest.create("/request_fishing_points")
        req.dataMap.putLong("ts", System.currentTimeMillis())
        dataClient.putDataItem(req.asPutDataRequest())
    }

    // 수신 메서드
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event: DataEvent in dataEvents) {
            val dataItem = event.dataItem
            when (dataItem.uri.path) {
                // 낚시포인트 수신
                "/fishing_points" -> {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val count = dataMap.getInt("count", 0)
                    val pointsList = mutableListOf<FishingPoint>()
                    for (i in 0 until count) {
                        val item = dataMap.getDataMap(i.toString())
                        pointsList.add(
                            FishingPoint(
                                name = item?.getString("name") ?: "",
                                point_nm = item?.getString("point_nm") ?: "",
                                dpwt = item?.getString("dpwt") ?: "",
                                material = item?.getString("material") ?: "",
                                tide_time = item?.getString("tide_time") ?: "",
                                target = item?.getString("target") ?: "",
                                lat = item?.getDouble("lat"),
                                lon = item?.getDouble("lon"),
                                point_dt = item?.getString("point_dt") ?: "",
                            )
                        )
                    }
                    fishingViewModel.updatePoints(pointsList)
                }

                // 날씨 수신
                "/weather_info" -> {
                    val dm = DataMapItem.fromDataItem(dataItem).dataMap
                    weatherViewModel.updateWeather(
                        WeatherData(
                            dm.getString("sky") ?: "",
                            dm.getString("windspd") ?: "",
                            dm.getString("temp") ?: "",
                            dm.getString("humidity") ?: "",
                            dm.getString("rain") ?: "",
                            dm.getString("winddir") ?: "",
                            dm.getString("waveHt") ?: "",
                            dm.getString("waveDir") ?: "",
                            dm.getString("obs_wt") ?: ""
                        )
                    )
                }

                // 조석 수신
                "/tide_info" -> {
                    val dm = DataMapItem.fromDataItem(dataItem).dataMap
                    val tide = TideInfoData(
                        date = dm.getString("pThisDate") ?: "",
                        name = dm.getString("pName") ?: "",
                        mul = dm.getString("pMul") ?: "",
                        sun = dm.getString("pSun") ?: "",
                        moon = dm.getString("pMoon") ?: "",
                        jowi1 = dm.getString("jowi1") ?: "",
                        jowi2 = dm.getString("jowi2") ?: "",
                        jowi3 = dm.getString("jowi3") ?: "",
                        jowi4 = dm.getString("jowi4") ?: ""
                    )
                    tideViewModel.updateTide(listOf(tide))
                }
            }
        }
    }
}
