package ru.tn.shinglass.models

import com.google.gson.annotations.SerializedName

data class Cell (
    val title: String = "",
    val guid: String = "",
    @SerializedName("barcode_label")
    val barcodeLabel: String = "",
): java.io.Serializable