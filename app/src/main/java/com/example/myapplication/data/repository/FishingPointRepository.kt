package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.domain.model.FishingPoint
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

class FishingPointRepository(private val context: Context) : DataClient.OnDataChangedListener {

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private var fishingPoints: MutableList<FishingPoint> = mutableListOf()

    // 📌 예시 데이터 (테스트용)
    fun getSamplePoints(): List<FishingPoint> {
        return listOf(
            FishingPoint(
                point_nm = "한숨자리 인근",
                distance = "3.9 km",
                lat = 34.64098028,
                lon = 128.35788140
            ),
            FishingPoint(
                point_nm = "백아도 큰말 선착장 갯바위",
                distance = "5.5 km",
                lat = 37.07538889,
                lon = 125.94497220
            )
        )
    }

    fun registerListener() {
        dataClient.addListener(this)
    }

    fun unregisterListener() {
        dataClient.removeListener(this)
    }

    fun getFishingPoints(): List<FishingPoint> = fishingPoints

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event: DataEvent in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/fishing_points") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val pointsList = dataMap.getDataMapArrayList("points")?.map {
                        FishingPoint(
                            point_nm = it.getString("point_nm") ?: "",
                            distance = it.getString("distance") ?: "",
                            lat = it.getDouble("lat"),
                            lon = it.getDouble("lon")
                        )
                    } ?: emptyList()
                    fishingPoints.clear()
                    fishingPoints.addAll(pointsList)
                }
            }
        }
    }
}
