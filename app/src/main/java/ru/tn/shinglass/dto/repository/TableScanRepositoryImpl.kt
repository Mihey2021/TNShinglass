package ru.tn.shinglass.dto.repository

import ru.tn.shinglass.dao.room.TableScanDao
import ru.tn.shinglass.dao.room.WarehousesDao
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.entity.TableScanEntity
import ru.tn.shinglass.models.TableScan

class TableScanRepositoryImpl(private val dao: TableScanDao) : TableScanRepository {

    override fun saveScanRecord(record: TableScan, forceOverwrite: Boolean) {
       val existingRecord = dao.getExistingRecord(
            record.OperationId,
            record.cellGuid,
            record.ItemGUID,
            record.ItemMeasureOfUnitGUID,
            record.WorkwearOrdinary,
            record.WorkwearDisposable,
            record.warehouseGuid,
            record.PurposeOfUse,
            record.PhysicalPersonGUID,
            record.OwnerGuid
        )

        val tempRecord = TableScanEntity.fromDto(record)
        if(existingRecord == null || forceOverwrite) {
            dao.save(tempRecord)
        }
        else {
            dao.save(tempRecord.copy(id = existingRecord.id, Count = (record.Count + existingRecord.Count)))
        }
    }

    override fun getScanRecordById(id: Long): TableScan? {
        return dao.getScanRecordById(id)?.toDto()
    }

    override fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long): List<TableScan> {
        return dao.getAllScanRecordsByOwner(ownerGuid, operationId).map { it.toDto() }
    }

    override fun deleteRecordById(id: Long) {
        dao.deleteRecordById(id)
    }

    override fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long) {
        dao.deleteRecordsByOwnerAndOperationId(ownerGuid, operationId)
    }

}