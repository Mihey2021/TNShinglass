package ru.tn.shinglass.models

data class NomenclatureStocks(
    val nomenclature: Nomenclature,
    val cell: Cell,
    val isGroup: Boolean = false,
    val totalCount: Double = 0.0,
)
