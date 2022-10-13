package ru.tn.shinglass.adapters

import ru.tn.shinglass.models.TableScan


interface OnTableScanItemInteractionListener {
    fun selectItem(item: TableScan) {}
}