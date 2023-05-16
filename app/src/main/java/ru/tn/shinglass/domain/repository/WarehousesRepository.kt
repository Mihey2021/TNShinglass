package ru.tn.shinglass.domain.repository

import androidx.lifecycle.LiveData
import ru.tn.shinglass.models.Warehouse
import ru.tn.shinglass.models.WarehouseReceiver

interface WarehousesRepository {
    val warehousesList: LiveData<List<Warehouse>>
    val warehouseReceiverList: LiveData<List<WarehouseReceiver>>
    //fun getAllWarehouses(): List<Warehouse>
    suspend fun getAllWarehousesList()
    suspend fun getAllWarehousesReceiverList()
    fun getAllWarehousesByDivision(divisionGuid: String): List<Warehouse>
    fun getWarehouseByGuid(guid: String): Warehouse?
    fun getWarehousesCountRecords(): Long
    fun getWarehousesCountRecordsByDivision(divisionGuid: String): Long
    fun saveWarehouses(warehouses: List<Warehouse>)
}