package ru.tn.shinglass.models

class CurrentScanData(
    val id: Long,
    val guid: String,
    val barcode: String,
    val title: String,
    val unitOfMeasureTitle: String,
    val unitOfMeasureGuid: String,
    val count: Double,
)