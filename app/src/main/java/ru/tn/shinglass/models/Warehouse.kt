package ru.tn.shinglass.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Warehouse(
    val id: Long = 0,
    val title: String,
    val guid: String,
    @SerializedName("division_guid")
    val divisionGuid: String,
    val responsibleGuid: String,
) :Serializable