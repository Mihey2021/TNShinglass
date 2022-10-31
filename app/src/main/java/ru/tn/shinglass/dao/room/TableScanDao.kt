package ru.tn.shinglass.dao.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.tn.shinglass.entity.OptionsEntity
import ru.tn.shinglass.entity.TableScanEntity
import ru.tn.shinglass.models.TableScan

@Dao
interface TableScanDao {
//    @Query("SELECT * FROM TableScanEntity WHERE OwnerGuid =:owner AND OperationId=:operationId")
//    fun getUserRecords(operationId: Long, owner: String)

    @Insert
    fun insert(record: TableScanEntity)

    @Query(
        "UPDATE TableScanEntity SET " +
                "OperationId =:OperationId, " +
                "OperationTitle =:OperationTitle, " +
                "cellTitle =:cellTitle, " +
                "cellGuid =:cellGuid, " +
                "ItemTitle =:ItemTitle, " +
                "ItemGUID =:ItemGUID, " +
                "ItemMeasureOfUnitTitle =:ItemMeasureOfUnitTitle, " +
                "ItemMeasureOfUnitGUID =:ItemMeasureOfUnitGUID, " +
                "Count =:Count, " +
                "coefficient =:coefficient, " +
                "qualityGuid =:qualityGuid, " +
                "qualityTitle =:qualityTitle, " +
                "WorkwearOrdinary =:WorkwearOrdinary, " +
                "WorkwearDisposable =:WorkwearDisposable, " +
                "DivisionId =:DivisionId, " +
                "DivisionOrganization =:DivisionOrganization, " +
                "warehouseGuid =:warehouseGuid, " +
                "PurposeOfUseTitle =:PurposeOfUseTitle, " +
                "PurposeOfUse =:PurposeOfUse, " +
                "PhysicalPersonTitle =:PhysicalPersonTitle, " +
                "PhysicalPersonGUID =:PhysicalPersonGUID, " +
                "OwnerGuid =:OwnerGuid, " +
                "uploaded =:uploaded, " +
                "docNameIn1C =:docNameIn1C " +
                " WHERE id = :id"
    )
    fun updateRecord(
        id: Long,
        OperationId: Long,
        OperationTitle: String,
        cellTitle: String,
        cellGuid: String,
        ItemTitle: String,
        ItemGUID: String,
        ItemMeasureOfUnitTitle: String,
        ItemMeasureOfUnitGUID: String,
        Count: Double,
        coefficient: Double,
        qualityGuid: String,
        qualityTitle: String,
        WorkwearOrdinary: Boolean,
        WorkwearDisposable: Boolean,
        DivisionId: Long,
        DivisionOrganization: Long,
        warehouseGuid: String,
        PurposeOfUseTitle: String,
        PurposeOfUse: String,
        PhysicalPersonTitle: String,
        PhysicalPersonGUID: String,
        OwnerGuid: String,
        uploaded: Boolean,
        docNameIn1C: String,
    )

    fun save(record: TableScanEntity) {
        if (record.id == 0L) insert(record)
        else updateRecord(
            id = record.id,
            OperationId = record.OperationId,
            OperationTitle = record.OperationTitle,
            cellTitle = record.cellTitle,
            cellGuid = record.cellGuid,
            ItemTitle = record.ItemTitle,
            ItemGUID = record.ItemGUID,
            ItemMeasureOfUnitTitle = record.ItemMeasureOfUnitTitle,
            ItemMeasureOfUnitGUID = record.ItemMeasureOfUnitGUID,
            Count = record.Count,
            coefficient = record.coefficient,
            qualityGuid = record.qualityGuid,
            qualityTitle = record.qualityTitle,
            WorkwearOrdinary = record.WorkwearOrdinary,
            WorkwearDisposable = record.WorkwearDisposable,
            DivisionId = record.DivisionId,
            DivisionOrganization = record.DivisionOrganization,
            warehouseGuid = record.warehouseGuid,
            PurposeOfUseTitle = record.PurposeOfUseTitle,
            PurposeOfUse = record.PurposeOfUse,
            PhysicalPersonTitle = record.PhysicalPersonTitle,
            PhysicalPersonGUID = record.PhysicalPersonGUID,
            OwnerGuid = record.OwnerGuid,
            uploaded = record.uploaded,
            docNameIn1C =  record.docNameIn1C,
        )
    }

    @Query("SELECT * FROM TableScanEntity WHERE id =:id")
    fun getScanRecordById(id: Long): TableScanEntity?

    @Query("SELECT * FROM TableScanEntity WHERE OwnerGuid =:ownerGuid AND OperationId =:operationId AND uploaded = 0 ORDER BY id DESC")
    fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long): List<TableScanEntity>

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
    ) : TableScanEntity?

    @Query("DELETE FROM TableScanEntity WHERE id =:id AND uploaded = 0")
    fun deleteRecordById(id: Long)

    @Query("DELETE FROM TableScanEntity WHERE OwnerGuid =:ownerGuid AND OperationId =:operationId")
    fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long)

}


//id, OperationId, OperationTitle, ItemTitle, ItemGUID, ItemMeasureOfUnitTitle, ItemMeasureOfUnitGUID, Count, WorkwearOrdinary, WorkwearDisposable, DivisionId, DivisionOrganization, WarehouseId, PurposeOfUseTitle, PurposeOfUse, PhysicalPersonTitle, PhysicalPersonGUID, OwnerGuid