package ru.tn.shinglass.models

import java.io.Serializable

data class Division(
    val id: Long = 0L,
    val title: String,
    val guid: String,
    val defaultWarehouseGuid: String = "",
) : Serializable
