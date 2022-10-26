package ru.tn.shinglass.models

import java.io.Serializable

class PhysicalPerson(
    val id: Long = 0L,
    val fio: String,
    val guid: String,
) : Serializable
