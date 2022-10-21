package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.dto.models.CreatedDocumentDetails
import ru.tn.shinglass.dto.models.RequestLogin
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.entity.Nomenclature
import ru.tn.shinglass.models.Cells
import ru.tn.shinglass.models.PhisicalPerson
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.models.Warehouse
import java.lang.Exception

interface RetrofitRepository {

    fun authorization(user: RequestLogin, callback:Callback<User1C>)
    fun getPhysicalPersonList(callback: Callback<List<PhisicalPerson>>)
    fun getAllWarehousesList(callback: Callback<List<Warehouse>>)
    fun getCellByBarcode(barcode: String, callback: Callback<Cells>)
    fun getItemByBarcode(barcode: String, callback: Callback<Nomenclature>)
    fun createInventoryOfGoods(scanRecords: List<TableScan>, callback: Callback<CreatedDocumentDetails>)

    interface Callback<T> {
        fun onSuccess(receivedData: T) {}
        fun onError(e: Exception) {}
    }
}