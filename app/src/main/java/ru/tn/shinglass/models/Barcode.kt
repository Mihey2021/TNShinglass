package ru.tn.shinglass.models

data class Barcode (
    val barcode: String = "",
    val type: String = "",
    val unitOfMeasurementGuid: String = "",
    val unitOfMeasurementTitle: String = "",
): java.io.Serializable {
    override fun toString(): String {
        return this.unitOfMeasurementTitle
    }
}
