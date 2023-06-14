package ru.tn.shinglass.models

data class RequestError (
    val message: String,
    val requestName: String,
    val barcode: String ="",
)