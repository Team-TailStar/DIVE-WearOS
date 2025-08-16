import com.example.myapplication.domain.model.WeatherData
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import android.content.Context

class WeatherRepository(private val context: Context) : DataClient.OnDataChangedListener {

    private val dataClient: DataClient = Wearable.getDataClient(context)

    // 샘플 데이터 (단일 객체)
    fun getSampleWeather(): WeatherData {
        return WeatherData(
            region = "부산광역시",
            temp = "27℃",
            condition = "맑음",
            wind = "3m/s",
            humidity = "65%",
            wave = "0.5m",
            notice = "낚시하기 좋은 날씨입니다."
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
                event.dataItem.uri.path == "/weather"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                // DataLayer → WeatherData 변환
                val weather = WeatherData(
                    region = dataMap.getString("region") ?: "",
                    temp = dataMap.getString("temp") ?: "",
                    condition = dataMap.getString("condition") ?: "",
                    wind = dataMap.getString("wind") ?: "",
                    humidity = dataMap.getString("humidity") ?: "",
                    wave = dataMap.getString("wave") ?: "",
                    notice = dataMap.getString("notice") ?: ""
                )

                // TODO: 변환한 weather 객체를 UI에 반영하도록 연결
            }
        }
    }
}