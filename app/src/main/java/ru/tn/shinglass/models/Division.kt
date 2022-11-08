package ru.tn.shinglass.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Division(
    @SerializedName("title")
    val divisionTitle: String,
    @SerializedName("guid")
    val divisionGuid: String,
    @SerializedName("defaultWarehouseGuid")
    val divisionDefaultWarehouseGuid: String = "",
) : Serializable
