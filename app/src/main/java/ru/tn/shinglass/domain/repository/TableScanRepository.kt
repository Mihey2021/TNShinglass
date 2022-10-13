package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.models.TableScan

interface TableScanRepository {
    fun saveScanRecord(record: TableScan)
    fun getScanRecordById(id: Long): TableScan?
    fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long): List<TableScan>
}