package ru.tn.shinglass.adapters

import ru.tn.shinglass.models.NomenclatureStocks

interface OnStocksItemInteractionListener {

    fun selectItem(item: NomenclatureStocks){}

}