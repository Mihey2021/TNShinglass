package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.models.Warehouse

interface WarehousesRepository {
    fun getAllWarehouses(): List<Warehouse>
    fun getAllWarehousesByDivision(divisionGuid: String): List<Warehouse>
    fun getWarehouseByGuid(guid: String): Warehouse?
    fun getWarehousesCountRecords(): Long
    fun getWarehousesCountRecordsByDivision(divisionGuid: String): Long
    fun saveWarehouses(warehouses: List<Warehouse>)
}