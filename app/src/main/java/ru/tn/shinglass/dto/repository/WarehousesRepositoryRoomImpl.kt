package ru.tn.shinglass.dto.repository

import ru.tn.shinglass.dao.room.WarehousesDao
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.entity.WarehousesEntity
import ru.tn.shinglass.models.Warehouse

class WarehousesRepositoryRoomImpl(private val dao: WarehousesDao) : WarehousesRepository {

    override fun getAllWarehouses(): List<Warehouse> =
        dao.getAllWarehouses().map { warehousesEntity ->
            warehousesEntity.toDto()
        }

    override fun getAllWarehousesByDivision(divisionGuid: String) =
        dao.getAllWarehousesByDivision(divisionGuid).map { warehousesEntity ->
            warehousesEntity.toDto()
        }

    override fun getWarehouseByGuid(guid: String): Warehouse? {
        val warehouse = dao.getWarehouseByGuid(guid) ?: return null
        return warehouse.toDto()
    }

    override fun getWarehousesCountRecords() = dao.getCountAllRecords()

    override fun getWarehousesCountRecordsByDivision(divisionGuid: String) =
        dao.getCountRecordsByDivision(divisionGuid)

    override fun saveWarehouses(warehouses: List<Warehouse>) {
        warehouses.forEach { warehouse -> dao.save(WarehousesEntity.fromDto(warehouse)) }
    }

}