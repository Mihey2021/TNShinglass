package ru.tn.shinglass.dao.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.tn.shinglass.entity.OptionsEntity
import ru.tn.shinglass.entity.TableScanEntity
import ru.tn.shinglass.models.TableScan

@Dao
interface TableScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: TableScanEntity)

    fun save(record: TableScanEntity) {
        insert(record)
    }

    @Query("SELECT * FROM TableScanEntity WHERE id =:id")
    fun getScanRecordById(id: Long): TableScanEntity?

//    @Query("SELECT * FROM TableScanEntity WHERE OwnerGuid =:ownerGuid AND OperationId =:operationId AND uploaded = 0 ORDER BY id DESC")
//    fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long): List<TableScanEntity>

    @Query("SELECT SUM(Count) FROM TableScanEntity " +
            "WHERE OwnerGuid =:ownerGuid AND OperationId =:operationId AND ItemGUID = :itemGUID AND ItemMeasureOfUnitGUID = :itemMeasureOfUnitGUID AND uploaded = 0 " +
            "GROUP BY OperationId, OwnerGuid, uploaded, divisionGuid, warehouseGuid, ItemGUID, ItemMeasureOfUnitGUID, docGuid")
    fun getTotalCount(
        ownerGuid: String,
        operationId: Long,
        itemGUID: String,
        itemMeasureOfUnitGUID: String
    ): Double

   // @Query("SELECT TableTotalCount.TotalCount AS totalCount, * FROM TableScanEntity " +
    @Query("SELECT TableTotalCount.TotalCount AS totalCount, TableTotalCount.isGroup AS isGroup, " +
            "id, OperationId, OperationTitle, cellTitle, cellGuid, cellReceiverTitle, cellReceiverGuid, ItemTitle, ItemGUID, ItemMeasureOfUnitTitle, " +
            "ItemMeasureOfUnitGUID, Count, docCount, docTitle, docGuid, coefficient, qualityGuid, qualityTitle, " +
            "WorkwearOrdinary, WorkwearDisposable, PurposeOfUseTitle, PurposeOfUse, " +
            "OwnerGuid, uploaded, docNameIn1C, incomingDate, incomingNumber , externalDocumentSelected, " +
            "warehouseTitle, warehouseGuid, warehouseDivisionGuid, warehouseResponsibleGuid, warehouseReceiverTitle, warehouseReceiverGuid, warehouseReceiverDivisionGuid, warehouseReceiverResponsibleGuid, physicalPersonFio, " +
            "physicalPersonGuid, divisionTitle, divisionGuid, divisionDefaultWarehouseGuid, counterpartyTitle, " +
            "counterpartyGuid, counterpartyInn, counterpartyKpp, employeeGuid, employeeFio " +
            "FROM TableScanEntity " +
            "LEFT JOIN (" +
            "SELECT id AS tc_id, SUM(Count) AS TotalCount, 1 AS isGroup FROM TableScanEntity " +
            "GROUP BY OperationId, OwnerGuid, uploaded, divisionGuid, warehouseGuid, ItemGUID, ItemMeasureOfUnitGUID, docGuid)  AS TableTotalCount ON TableScanEntity.id = TableTotalCount.tc_id " +
            "WHERE TableScanEntity.OwnerGuid =:ownerGuid AND TableScanEntity.OperationId =:operationId AND TableScanEntity.uploaded = 0 " +
            "ORDER BY ItemTitle ASC, id DESC")
    fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long): List<TableScanEntity>

    @Query(
        "SELECT SUM(Count) FROM TableScanEntity WHERE " +
                "OwnerGuid =:ownerGuid " +
                "AND OperationId =:operationId " +
                "AND itemGUID =:itemGUID " +
                "AND itemMeasureOfUnitGUID =:itemMeasureOfUnitGUID " +
                "AND workwearOrdinary =:workwearOrdinary " +
                "AND workwearDisposable =:workwearDisposable " +
                "AND warehouseGuid =:warehouseGuid " +
                "AND purposeOfUse =:purposeOfUse " +
                "AND physicalPersonGUID =:physicalPersonGUID " +
                "AND (employeeGuid = :employeeGUID OR employeeGuid is NULL ) " +
                "AND uploaded = 0 "
    )
    fun getExistingRecordCountSum(
        operationId: Long,
        itemGUID: String,
        itemMeasureOfUnitGUID: String,
        workwearOrdinary: Boolean,
        workwearDisposable: Boolean,
        warehouseGuid: String,
        purposeOfUse: String,
        physicalPersonGUID: String,
        ownerGuid: String,
        employeeGUID: String,
    ): Double

    @Query(
        "SELECT * FROM TableScanEntity WHERE " +
                "OwnerGuid =:ownerGuid " +
                "AND OperationId =:operationId " +
                "AND cellGuid =:cellGuid " +
                "AND itemGUID =:itemGUID " +
                "AND itemMeasureOfUnitGUID =:itemMeasureOfUnitGUID " +
                "AND workwearOrdinary =:workwearOrdinary " +
                "AND workwearDisposable =:workwearDisposable " +
                "AND warehouseGuid =:warehouseGuid " +
                "AND purposeOfUse =:purposeOfUse " +
                "AND physicalPersonGUID =:physicalPersonGUID " +
                "AND (employeeGuid = :employeeGUID OR employeeGuid is NULL ) " +
                "AND uploaded = 0 "
    )
    fun getExistingRecord(
        operationId: Long,
        cellGuid: String,
        itemGUID: String,
        itemMeasureOfUnitGUID: String,
        workwearOrdinary: Boolean,
        workwearDisposable: Boolean,
        warehouseGuid: String,
        purposeOfUse: String,
        physicalPersonGUID: String,
        ownerGuid: String,
        employeeGUID: String
    ): TableScanEntity?

    @Query(
        "SELECT * FROM TableScanEntity WHERE " +
                "OwnerGuid =:ownerGuid " +
                "AND OperationId =:operationId " +
                "AND itemGUID =:itemGUID " +
                "AND itemMeasureOfUnitGUID =:itemMeasureOfUnitGUID " +
                "AND workwearOrdinary =:workwearOrdinary " +
                "AND workwearDisposable =:workwearDisposable " +
                "AND warehouseGuid =:warehouseGuid " +
                "AND purposeOfUse =:purposeOfUse " +
                "AND physicalPersonGUID =:physicalPersonGUID " +
                "AND (employeeGuid = :employeeGUID OR employeeGuid is NULL ) " +
                "AND uploaded = 0 "
    )
    fun getExistingRecordWithoutCell(
        operationId: Long,
        itemGUID: String,
        itemMeasureOfUnitGUID: String,
        workwearOrdinary: Boolean,
        workwearDisposable: Boolean,
        warehouseGuid: String,
        purposeOfUse: String,
        physicalPersonGUID: String,
        ownerGuid: String,
        employeeGUID: String,
    ): TableScanEntity?

    @Query("DELETE FROM TableScanEntity WHERE id =:id AND uploaded = 0")
    fun deleteRecordById(id: Long)

    @Query("DELETE FROM TableScanEntity WHERE OwnerGuid =:ownerGuid AND OperationId =:operationId")
    fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long)

//    @Query("SELECT * FROM TableScanEntity WHERE OwnerGuid =:ownerGuid AND OperationId =:operationId AND docGuid = :docGuid AND uploaded = 0 ORDER BY id DESC")
//    fun getDoc1CRecords(operationId: Long, ownerGuid: String, docGuid: String): List<TableScanEntity>

}


//id, OperationId, OperationTitle, ItemTitle, ItemGUID, ItemMeasureOfUnitTitle, ItemMeasureOfUnitGUID, Count, WorkwearOrdinary, WorkwearDisposable, DivisionId, DivisionOrganization, WarehouseId, PurposeOfUseTitle, PurposeOfUse, PhysicalPersonTitle, PhysicalPersonGUID, OwnerGuid