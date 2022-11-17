package ru.tn.shinglass.models

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class ExternalDocumentItems(
    @SerializedName("cell_title")
    val cellTitle: String = "",
    @SerializedName("cell_guid")
    val cellGuid: String = "",
    @SerializedName("item_title")
    val itemTitle: String = "",
    @SerializedName("item_guid")
    val itemGUID: String = "",
    @SerializedName("item_count")
    val itemCount: Double = 0.0,
    @SerializedName("item_coefficient")
    val itemCoefficient: Double = 0.0,
    @SerializedName("mou_title")
    val itemMeasureOfUnitTitle: String = "",
    @SerializedName("mou_guid")
    val itemMeasureOfUnitGUID: String = "",
    @SerializedName("warehouse")
    val warehouse: Warehouse? = null,
)
