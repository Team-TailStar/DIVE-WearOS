package com.example.dive_app.data.repository

import android.content.Context
import com.example.myapplication.domain.model.FishingPoint
import com.example.myapplication.domain.model.TideInfoData
import com.example.myapplication.domain.model.TideViewModel
import com.example.myapplication.domain.model.WeatherViewModel
import com.example.myapplication.ui.viewmodel.FishingPointViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

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

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event: DataEvent in dataEvents) {
            val dataItem = event.dataItem

            when (dataItem.uri.path) {
                // 낚시포인트 수신
                "/fishing_points" -> {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                    val pointsList = dataMap.getDataMapArrayList("points")?.map { item ->
                        FishingPoint(
                            name = item.getString("name") ?: "",
                            point_nm = item.getString("point_nm") ?: "",
                            dpwt = item.getString("dpwt") ?: "",
                            material = item.getString("material") ?: "",
                            tide_time = item.getString("tide_time") ?: "",
                            target = item.getString("target") ?: "",
                            lat = item.getDouble("lat"),
                            lon = item.getDouble("lon"),
                            point_dt = item.getString("point_dt") ?: "",
                        )
                    } ?: emptyList()

                    fishingViewModel.updatePoints(pointsList)
                }

                // 날씨 수신
                "/weather_info" -> {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val sky = dataMap.getString("sky") ?: ""
                    val windspd = dataMap.getString("windspd") ?: ""
                    val temp = dataMap.getString("temp") ?: ""
                    val humidity = dataMap.getString("humidity") ?: ""
                    val rain = dataMap.getString("rain") ?: ""
                    val winddir = dataMap.getString("winddir") ?: ""
                    val waveHt = dataMap.getString("waveHt") ?: ""
                    val waveDir = dataMap.getString("waveDir") ?: ""
                    val obs_wt = dataMap.getString("obs_wt") ?: ""

                    weatherViewModel.updateWeather(
                        sky, windspd, temp, humidity, rain, winddir, waveHt, waveDir, obs_wt
                    )
                }

                // 조석 수신
                "/tide_info" -> {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val tideList = dataMap.getDataMapArrayList("tideList")?.map { item ->
                        TideInfoData(
                            date = dataMap.getString("pThisDate") ?: "",
                            name = dataMap.getString("pName") ?: "",
                            mul = dataMap.getString("pMul") ?: "",
                            sun = dataMap.getString("pSun") ?: "",
                            moon = dataMap.getString("pMoon") ?: "",
                            jowi1 = dataMap.getString("jowi1") ?: "",
                            jowi2 = dataMap.getString("jowi2") ?: "",
                            jowi3 = dataMap.getString("jowi3") ?: "",
                            jowi4 = dataMap.getString("jowi4") ?: ""
                        )
                    } ?: emptyList()

                    tideViewModel.updateTide(tideList)
                }
            }
        }
    }
}
