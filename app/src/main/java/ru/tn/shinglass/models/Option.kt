package ru.tn.shinglass.models

import java.io.Serializable

data class Option(
    val id: Long = 0L,
    val subOptionId: Long = 0L,
    val type: String,
    val title: String,
    val description: String = "",
) : Serializable

