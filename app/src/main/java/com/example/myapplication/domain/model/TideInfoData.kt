package com.example.myapplication.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TideInfoData(
    val date: String = "",
    val name: String = "",
    val mul: String = "",
    val sun: String = "",
    val moon: String = "",
    val jowi1: String = "",
    val jowi2: String = "",
    val jowi3: String = "",
    val jowi4: String = "",
) : Parcelable