package ru.tn.shinglass.adapters

import ru.tn.shinglass.models.CurrentScanData


interface OnTableScanItemInteractionListener {
    fun selectItem(item: CurrentScanData) {}
}