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
    val jowi1: String = "",
    val jowi2: String = "",
    val jowi3: String = "",
    val jowi4: String = "",
) : Parcelable