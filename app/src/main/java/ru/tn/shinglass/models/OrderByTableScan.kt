package ru.tn.shinglass.models

data class OrderByTableScan(
    val operationId: Long,
    val ownerGuid: String,
    val uploaded: Boolean = false,
    val divisionGuid: String,
    val warehouseGuid: String,
    val itemGUID: String,
    val itemMeasureOfUnitGUID: String,
    val docGuid: String,
)
