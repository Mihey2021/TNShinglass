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
    @SerializedName("usesLogistics")
    val usesLogistics: Boolean = false,
) :Serializable

data class WarehouseReceiver(

    @SerializedName("title")
    val warehouseReceiverTitle: String,
    @SerializedName("guid")
    val warehouseReceiverGuid: String,
    @SerializedName("division_guid")
    val warehouseReceiverDivisionGuid: String,
    @SerializedName("responsibleGuid")
    val warehouseReceiverResponsibleGuid: String,
    @SerializedName("usesLogistics")
    val warehouseReceiverUsesLogistics: Boolean = false,
) :Serializable