package com.example.dive_app.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TideInfoData(
    val pThisDate: String = "",
    val pName : String = "",
    val pMul: String = "",
    val pSun: String = "",
    val pMoon: String = "",
    // API 필드
    val pTime1: String = "",
    val pTime2: String = "",
    val pTime3: String = "",
    val pTime4: String = "",
    // 레거시/호환
    val jowi1: String = "",
    val jowi2: String = "",
    val jowi3: String = "",
    val jowi4: String = "",
) : Parcelable
