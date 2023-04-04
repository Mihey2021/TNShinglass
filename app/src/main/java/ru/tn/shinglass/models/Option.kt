package ru.tn.shinglass.models

import java.io.Serializable

data class Option(
    val id: Long = 0L,
    val subOption: SubOptionType? = null,
    val option: OptionType,
    val docType: DocType? = null
) : Serializable

