package ru.tn.shinglass.models

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.User1C
import java.io.Serializable

data class TableScan (
    val id: Long = 0L,
    val OperationId: Long = 0L,
    val OperationTitle: String = "",
    val cellTitle: String = "",
    val cellGuid: String = "",
    val ItemTitle: String = "",
    val ItemGUID: String = "",
    val ItemMeasureOfUnitTitle: String = "",
    val ItemMeasureOfUnitGUID: String = "",
    val Count: Double = 0.0,
    val totalCount: Double = 0.0,
    val docCount: Double = 0.0,
    val docTitle: String = "",
    val docGuid: String = "",
    val coefficient: Double = 0.0,
    val qualityGuid: String = "",
    val qualityTitle: String = "",
    val WorkwearOrdinary: Boolean = false,
    val WorkwearDisposable: Boolean = false,
    val PurposeOfUseTitle: String = "",
    val PurposeOfUse: String = "",
    @Embedded
    val docHeaders: DocumentHeaders,
    val OwnerGuid: String,
    val uploaded: Boolean = false,
) : Serializable