package ru.tn.shinglass.dto.repository

import ru.tn.shinglass.dao.room.TableScanDao
import ru.tn.shinglass.dao.room.WarehousesDao
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.entity.TableScanEntity
import ru.tn.shinglass.models.TableScan

class TableScanRepositoryImpl(private val dao: TableScanDao): TableScanRepository {

    override fun saveScanRecord(record: TableScan){
        dao.save(TableScanEntity.fromDto(record))
    }

    override fun getScanRecordById(id: Long): TableScan? {
        return dao.getScanRecordById(id)?.toDto()
    }

    override fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long): List<TableScan> {
        return dao.getAllScanRecordsByOwner(ownerGuid, operationId).map { it.toDto() }
    }

}