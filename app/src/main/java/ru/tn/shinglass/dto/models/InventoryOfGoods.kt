package ru.tn.shinglass.dto.models

import ru.tn.shinglass.models.TableScan

class InventoryOfGoods (
    val docName: String = "InventoryOfGoods",
    val records: List<TableScan>,
)