package ru.tn.shinglass.domain.repository

import androidx.lifecycle.LiveData
import ru.tn.shinglass.models.Warehouse

interface WarehousesRepository {
    val warehousesList: LiveData<List<Warehouse>>
    //fun getAllWarehouses(): List<Warehouse>
    suspend fun getAllWarehousesList()
    fun getAllWarehousesByDivision(divisionGuid: String): List<Warehouse>
    fun getWarehouseByGuid(guid: String): Warehouse?
    fun getWarehousesCountRecords(): Long
    fun getWarehousesCountRecordsByDivision(divisionGuid: String): Long
    fun saveWarehouses(warehouses: List<Warehouse>)
}