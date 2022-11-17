package ru.tn.shinglass.dto.repository

import retrofit2.Response
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.dao.room.TableScanDao
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.dto.models.CreatedDocumentDetails
import ru.tn.shinglass.dto.models.DocHeaders
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.DocumentToUploaded
import ru.tn.shinglass.entity.TableScanEntity
import ru.tn.shinglass.error.ApiError
import ru.tn.shinglass.error.ApiServiceError
import ru.tn.shinglass.error.NetworkError
import ru.tn.shinglass.models.Counterparty
import ru.tn.shinglass.models.DocType
import ru.tn.shinglass.models.ExternalDocument
import ru.tn.shinglass.models.TableScan
import java.io.IOException

class TableScanRepositoryImpl(private val dao: TableScanDao) : TableScanRepository {

    private val apiService = ApiUtils.getApiService()

    override fun saveScanRecord(record: TableScan, forceOverwrite: Boolean) {
        var existingRecord: TableScanEntity? = null
        //На основании внешнего документа, если в нем не указана ячейка
        if (record.docGuid != "" && dao.getScanRecordById(record.id)?.cellGuid == "") {
            existingRecord = dao.getExistingRecordWithoutCell(
                record.OperationId,
                record.ItemGUID,
                record.ItemMeasureOfUnitGUID,
                record.WorkwearOrdinary,
                record.WorkwearDisposable,
                record.docHeaders.getWarehouse()?.warehouseGuid ?: "",
                record.PurposeOfUse,
                record.docHeaders.getPhysicalPerson()?.physicalPersonGuid ?: "",
                record.OwnerGuid
            )
        } else {
            //Без документа-основания или если в документе-основании укзана ячейка
            existingRecord = dao.getExistingRecord(
                record.OperationId,
                record.cellGuid,
                record.ItemGUID,
                record.ItemMeasureOfUnitGUID,
                record.WorkwearOrdinary,
                record.WorkwearDisposable,
                //record.warehouseGuid,
                record.docHeaders.getWarehouse()?.warehouseGuid ?: "",
                record.PurposeOfUse,
                //record.PhysicalPersonGUID,
                record.docHeaders.getPhysicalPerson()?.physicalPersonGuid ?: "",
                record.OwnerGuid
            )
        }

        val tempRecord = TableScanEntity.fromDto(record)
        if (existingRecord == null || forceOverwrite) {
            dao.save(tempRecord)
        } else {
            dao.save(
                tempRecord.copy(
                    id = existingRecord.id,
                    Count = (record.Count + existingRecord.Count)
                )
            )
        }
    }

    override suspend fun createDocumentIn1C(
        scanRecords: List<TableScan>,
        docType: DocType
    ): CreatedDocumentDetails {
        try {
            if (apiService != null) {
                val headers =
                    if (scanRecords.isEmpty()) DocHeaders(DocumentHeaders) else DocHeaders(
                        scanRecords[0].docHeaders
                    )

                var response: Response<CreatedDocumentDetails>
                if (docType == DocType.INVENTORY_IN_CELLS) {
                    response =
                        apiService.createInventoryOfGoods(
                            DocumentToUploaded(
                                docType,
                                headers,
                                scanRecords
                            )
                        )
                }
                if (docType == DocType.STANDARD_ACCEPTANCE) {
                    response =
                        apiService.createGoodsReceiptOrder(
                            DocumentToUploaded(
                                docType,
                                headers,
                                scanRecords
                            )
                        )
                } else {
                    response =
                        apiService.createRequirementInvoice(
                            DocumentToUploaded(
                                docType,
                                headers,
                                scanRecords
                            )
                        )
                }
                if (!response.isSuccessful) {
                    throw ApiServiceError("Code: ${response.code()}\n${response.message()}")
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
//                dao.saveDivisions(body.toEntity())
                scanRecords.forEach { record ->
                    dao.save(
                        TableScanEntity.fromDto(record.copy(uploaded = true))
                            .copy(docNameIn1C = "${body.docTitle} ${body.docNumber}")
                    )
                }
                return body
            } else {
                throw ApiServiceError("API service not ready")
            }
        } catch (e: ApiServiceError) {
            throw ApiServiceError(e.message.toString())
        } catch (e: IOException) {
            //throw NetworkError
            throw ApiServiceError(e.message.toString())
        } catch (e: Exception) {
            throw ApiServiceError(e.message.toString())
        }
    }

    override suspend fun getCounterpartiesList(title: String): List<Counterparty> {
        try {
            if (apiService != null) {
                val response =
                    apiService.getCounterpartiesList(title)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                return response.body() ?: throw ApiError(response.code(), response.message())
            } else {
                throw ApiServiceError("API service not ready")
            }
        } catch (e: IOException) {
            //throw NetworkError
            throw ApiServiceError(e.message.toString())
        } catch (e: Exception) {
            throw ApiServiceError(e.message.toString())
        }
    }

    override fun getScanRecordById(id: Long): TableScan? {
        return dao.getScanRecordById(id)?.toDto()
    }

    override fun getAllScanRecordsByOwner(
        ownerGuid: String,
        operationId: Long
    ): List<TableScan> {
        return dao.getAllScanRecordsByOwner(ownerGuid, operationId).map { it.toDto() }
    }

    override fun deleteRecordById(id: Long) {
        dao.deleteRecordById(id)
    }

    override fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long) {
        dao.deleteRecordsByOwnerAndOperationId(ownerGuid, operationId)
    }

    override fun updateRecordUpload(ownerGuid: String, operationId: Long) {
        val recordsEntityList = dao.getAllScanRecordsByOwner(ownerGuid, operationId)
        recordsEntityList.forEach { record -> dao.save(record.copy(uploaded = true)) }
    }

    override suspend fun getInternalOrderList(): List<ExternalDocument> {
        try {
            if (apiService != null) {
                val response =
                    apiService.getInternalOrderList()
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                return response.body() ?: throw ApiError(response.code(), response.message())
            } else {
                throw ApiServiceError("API service not ready")
            }
        } catch (e: IOException) {
            //throw NetworkError
            throw ApiServiceError(e.message.toString())
        } catch (e: Exception) {
            throw ApiServiceError(e.message.toString())
        }
    }

    override fun getExistingRecordCountSum(record: TableScan?): Double =
        if (record == null) 0.0
        else
            dao.getExistingRecordCountSum(
                operationId = record.OperationId,
                itemGUID = record.ItemGUID,
                itemMeasureOfUnitGUID = record.ItemMeasureOfUnitGUID,
                workwearOrdinary = record.WorkwearOrdinary,
                workwearDisposable = record.WorkwearDisposable,
                warehouseGuid = record.docHeaders.getWarehouse()?.warehouseGuid ?: "",
                purposeOfUse = record.PurposeOfUse,
                physicalPersonGUID = record.docHeaders.getPhysicalPerson()?.physicalPersonGuid
                    ?: "",
                ownerGuid = record.OwnerGuid
            )

}