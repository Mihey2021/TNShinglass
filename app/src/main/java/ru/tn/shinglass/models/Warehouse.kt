package ru.tn.shinglass.models

import java.io.Serializable

data class Warehouse(
    val id: Long = 0,
    val title: String,
    val guid: String,
    val division: Long,
) :Serializable