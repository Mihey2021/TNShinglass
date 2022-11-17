package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.dto.models.CreatedDocumentDetails
import ru.tn.shinglass.models.Counterparty
import ru.tn.shinglass.models.DocType
import ru.tn.shinglass.models.ExternalDocument
import ru.tn.shinglass.models.TableScan

interface TableScanRepository {
    fun saveScanRecord(record: TableScan, forceOverwrite: Boolean)
    fun getScanRecordById(id: Long): TableScan?
    fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long): List<TableScan>
    fun deleteRecordById(id: Long)
    fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long)
    fun updateRecordUpload(ownerGuid: String, operationId: Long)
    suspend fun createDocumentIn1C(scanRecords: List<TableScan>, docType: DocType): CreatedDocumentDetails
    suspend fun getCounterpartiesList(searchParam: String): List<Counterparty>
    suspend fun getInternalOrderList(): List<ExternalDocument>
    fun getExistingRecordCountSum(record: TableScan?): Double
}