package ru.tn.shinglass.models

data class ModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val errorMessage: String = "",
    val requestName: String = "",
)
