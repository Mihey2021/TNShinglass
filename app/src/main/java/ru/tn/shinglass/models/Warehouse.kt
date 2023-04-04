package ru.tn.shinglass.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Warehouse(

    @SerializedName("title")
    val warehouseTitle: String,
    @SerializedName("guid")
    val warehouseGuid: String,
    @SerializedName("division_guid")
    val warehouseDivisionGuid: String,
    @SerializedName("responsibleGuid")
    val warehouseResponsibleGuid: String,
) :Serializable