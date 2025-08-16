package com.example.myapplication.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object LocationUtil {
    private const val KAKAO_API_KEY = "6c70d9ab4ca17bdfa047539c7d8ec0a8"

    fun fetchAddressFromCoords(
        lat: Double,
        lon: Double,
        onResult: (String, String) -> Unit
    ) {
        val url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=$lon&y=$lat"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "KakaoAK $KAKAO_API_KEY")
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val documents = json.getJSONArray("documents")
                    if (documents.length() > 0) {
                        val addressObj = documents.getJSONObject(0).getJSONObject("address")
                        val region1 = addressObj.getString("region_1depth_name") // 시/도
                        val region2 =
                            "${addressObj.getString("region_2depth_name")} ${addressObj.getString("region_3depth_name")}" // 시군구 + 동
                        onResult(region1, region2)
                    } else {
                        onResult("주소 없음", "")
                    }
                } else {
                    onResult("HTTP 오류", "")
                }
            } catch (e: Exception) {
                onResult("주소 불러오기 실패", "")
            }
        }
    }
}