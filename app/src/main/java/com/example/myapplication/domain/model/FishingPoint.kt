package com.example.myapplication.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FishingPoint(
    val name: String,        // 지역명
    val point_nm: String,    // 포인트명
    val dpwt: String,       // 수심
    val material: String,   // 저질
    val tide_time: String,  // 적정 물때
    val target: String,     // 포인트 어종
    val lat: Double,
    val lon: Double,
    val photo: String,      // 포인트 사진 URL
    val addr: String,       // 주소
    val seaside: String,    // y/n
    val intro: String,      // 지역소개
    val forecast: String,   // 기상
    val ebbf: String,       // 조류
    val notice: String,     // 주의사항
    val wtemp_sp: String,   // 봄 수온
    val wtemp_su: String,   // 여름 수온
    val wtemp_fa: String,   // 가을 수온
    val wtemp_wi: String,   // 겨울 수온
    val fish_sp: String,    // 봄 어종
    val fish_su: String,    // 여름 어종
    val fish_fa: String,    // 가을 어종
    val fish_wi: String,    // 겨울 어종
    val point_dt: String    // 거리 (m/km)
) : Parcelable
