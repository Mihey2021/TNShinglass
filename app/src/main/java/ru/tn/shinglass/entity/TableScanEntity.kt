package ru.tn.shinglass.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TableScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val OperationId: Long,
    val OperationTitle: String,
    val ItemTitle: String,
    val ItemGUID: String,
    val ItemMeasureOfUnitTitle: String,
    val ItemMeasureOfUnitGUID: String,
    val Count: Double,
    val WorkwearOrdinary: Boolean = false,
    val WorkwearDisposable: Boolean = false,
    val DivisionId: Long,
    val DivisionOrganization: Long,
    val WarehouseId: Long,
    val PurposeOfUseTitle: String,
    val PurposeOfUse: String,
    val PhysicalPersonTitle: String,
    val PhysicalPersonGUID: String,
    val Owner: String,
) {
}