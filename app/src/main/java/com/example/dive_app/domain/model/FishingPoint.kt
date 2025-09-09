package com.example.dive_app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FishingPoint(
    val name : String = "",
    val point_nm : String = "",
    val dpwt : String = "",
    val material : String = "",
    val tide_time : String = "",
    val target : String = "",
    val lat : Double = 0.0,
    val lon : Double = 0.0,
    val point_dt : String = ""
) : Parcelable
