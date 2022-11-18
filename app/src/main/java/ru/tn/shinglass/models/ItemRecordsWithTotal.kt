package ru.tn.shinglass.models

data class ItemRecordsWithTotal(
    val record: TableScan,
    val totalCount: Double
)