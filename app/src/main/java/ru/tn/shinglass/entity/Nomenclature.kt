package ru.tn.shinglass.entity

data class Nomenclature(
    val itemGuid: String,
    val itemTitle: String,
    val unitOfMeasurementGuid: String,
    val unitOfMeasurementTitle: String,
    val coefficient: Double,
    val qualityGuid: String,
    val qualityTitle: String,
)
