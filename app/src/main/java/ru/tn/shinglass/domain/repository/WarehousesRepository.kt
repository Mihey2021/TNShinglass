package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.models.Warehouse

interface WarehousesRepository {
    fun getAllWarehouses(): List<Warehouse>
    fun getAllWarehousesByDivision(divisionId: Long): List<Warehouse>
    fun getWarehouseByGuid(guid: String): Warehouse?
    fun getWarehousesCountRecords(): Long
    fun getWarehousesCountRecordsByDivision(divisionId: Long): Long
    fun save(warehouses: List<Warehouse>)
}