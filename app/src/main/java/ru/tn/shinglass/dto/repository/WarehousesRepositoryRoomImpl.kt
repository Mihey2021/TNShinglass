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

    override fun getAllWarehousesByDivision(divisionId: Long) =
        dao.getAllWarehousesByDivision(divisionId).map { warehousesEntity ->
            warehousesEntity.toDto()
        }

    override fun getWarehouseByGuid(guid: String): Warehouse? {
        val warehouse = dao.getWarehouseByGuid(guid) ?: return null
        return warehouse.toDto()
    }

    override fun getWarehousesCountRecords() = dao.getCountAllRecords()

    override fun getWarehousesCountRecordsByDivision(divisionId: Long) =
        dao.getCountRecordsByDivision(divisionId)

    override fun save(warehouses: List<Warehouse>) {
        warehouses.forEach { warehouse -> dao.save(WarehousesEntity.fromDto(warehouse)) }
    }

}