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