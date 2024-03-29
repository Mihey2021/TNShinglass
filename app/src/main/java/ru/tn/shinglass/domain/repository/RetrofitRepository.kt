package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.dto.models.RequestLogin
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.Nomenclature
import ru.tn.shinglass.models.*
import java.lang.Exception

interface RetrofitRepository {

    fun authorization(user: RequestLogin, callback: Callback<User1C>)

    //suspend fun getPhysicalPersonList()
    //fun getAllWarehousesList(callback: Callback<List<Warehouse>>)
    //fun getCellByBarcode(barcode: String, callback: Callback<Cells>)
    suspend fun getCellByBarcode(barcode: String, warehouseGuid: String): Cell
    suspend fun getCellsList(warehouseGuid: String): List<Cell>
    suspend fun getCellByGuid(cellGuid: String): Cell
    fun getItemByBarcode(barcode: String, callback: Callback<Nomenclature>)
    //fun createInventoryOfGoods(scanRecords: List<TableScan>, callback: Callback<CreatedDocumentDetails>)
    //fun getAllDivisionsList(callback: Callback<List<Division>>)

    interface Callback<T> {
        fun onSuccess(receivedData: T) {}
        fun onError(e: Exception) {}
    }
}