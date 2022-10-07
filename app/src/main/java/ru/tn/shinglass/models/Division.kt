package ru.tn.shinglass.models

import java.io.Serializable

data class Division(
    val id: Long = 0,
    val title: String,
    val guid: String,
) : Serializable
