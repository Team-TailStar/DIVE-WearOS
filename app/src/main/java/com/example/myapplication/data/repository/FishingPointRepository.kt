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

    fun getSamplePoints(): List<FishingPoint> {
        return listOf(
            FishingPoint(
                name = "부산광역시",
                point_nm = "광안리 해수욕장 포인트",
                dpwt = "5m",
                material = "모래",
                tide_time = "4물",
                target = "숭어, 도다리",
                lat = 35.1532,
                lon = 129.1186,
                photo = "https://example.com/point1.jpg",
                addr = "부산광역시 수영구 광안해변로 219",
                seaside = "y",
                intro = "광안대교가 보이는 유명 포인트. 봄철 숭어 낚시로 유명.",
                forecast = "맑음",
                ebbf = "중조",
                notice = "해수욕장 구역은 낚시 금지 구역 주의",
                wtemp_sp = "14도",
                wtemp_su = "24도",
                wtemp_fa = "18도",
                wtemp_wi = "10도",
                fish_sp = "숭어",
                fish_su = "전갱이",
                fish_fa = "도다리",
                fish_wi = "우럭",
                point_dt = "200m"
            ),
            FishingPoint(
                name = "제주특별자치도",
                point_nm = "성산 일출봉 포인트",
                dpwt = "8m",
                material = "암반",
                tide_time = "7물",
                target = "감성돔, 돌돔",
                lat = 33.4589,
                lon = 126.9423,
                photo = "https://example.com/point2.jpg",
                addr = "제주특별자치도 서귀포시 성산읍 일출로 284-12",
                seaside = "y",
                intro = "성산 일출봉 아래쪽 포인트. 조류가 빠르고 다양한 어종 가능.",
                forecast = "흐림",
                ebbf = "사리",
                notice = "파도가 높은 날엔 위험",
                wtemp_sp = "15도",
                wtemp_su = "25도",
                wtemp_fa = "20도",
                wtemp_wi = "12도",
                fish_sp = "감성돔",
                fish_su = "돌돔",
                fish_fa = "노래미",
                fish_wi = "우럭",
                point_dt = "500m"
            )
        )
    }

    fun registerListener() {
        dataClient.addListener(this)
    }

    fun unregisterListener() {
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event: DataEvent in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/fishing_points"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                // 최신 데이터만 바로 Flow에 전달
                val pointsList = dataMap.getDataMapArrayList("points")?.map { it ->
                    FishingPoint(
                        name = it.getString("name") ?: "",
                        point_nm = it.getString("point_nm") ?: "",
                        dpwt = it.getString("dpwt") ?: "",
                        material = it.getString("material") ?: "",
                        tide_time = it.getString("tide_time") ?: "",
                        target = it.getString("target") ?: "",
                        lat = it.getDouble("lat"),
                        lon = it.getDouble("lon"),
                        photo = it.getString("photo") ?: "",
                        addr = it.getString("addr") ?: "",
                        seaside = it.getString("seaside") ?: "",
                        intro = it.getString("intro") ?: "",
                        forecast = it.getString("forecast") ?: "",
                        ebbf = it.getString("ebbf") ?: "",
                        notice = it.getString("notice") ?: "",
                        wtemp_sp = it.getString("wtemp_sp") ?: "",
                        wtemp_su = it.getString("wtemp_su") ?: "",
                        wtemp_fa = it.getString("wtemp_fa") ?: "",
                        wtemp_wi = it.getString("wtemp_wi") ?: "",
                        fish_sp = it.getString("fish_sp") ?: "",
                        fish_su = it.getString("fish_su") ?: "",
                        fish_fa = it.getString("fish_fa") ?: "",
                        fish_wi = it.getString("fish_wi") ?: "",
                        point_dt = it.getString("point_dt") ?: ""
                    )
                } ?: emptyList()
            }
        }
    }
}

