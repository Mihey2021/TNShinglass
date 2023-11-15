package ru.tn.shinglass.models

data class Nomenclature(
    val itemGuid: String = "",
    val itemTitle: String = "",
    val code: String = "",
    val unitOfMeasurementGuid: String = "",
    val unitOfMeasurementTitle: String = "",
    val coefficient: Double = 0.0,
    val qualityGuid: String = "",
    val qualityTitle: String = "",
): java.io.Serializable
