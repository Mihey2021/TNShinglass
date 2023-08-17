package ru.tn.shinglass.dao.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.tn.shinglass.entity.WarehousesEntity
import ru.tn.shinglass.models.Warehouse

@Dao
interface WarehousesDao {
    @Query("SELECT * FROM WarehousesEntity")
    fun getAllWarehouses(): LiveData<List<WarehousesEntity>>

    @Query("SELECT title, guid, divisionGuid, responsibleGuid, usesLogistics FROM WarehousesEntity WHERE divisionGuid=:divisionGuid")
    fun getAllWarehousesByDivision(divisionGuid: String): List<WarehousesEntity>

    @Query("SELECT title, guid, divisionGuid, responsibleGuid, usesLogistics FROM WarehousesEntity WHERE guid=:guid")
    fun getWarehouseByGuid(guid: String): WarehousesEntity?

//    @Query("SELECT title, guid, divisionGuid, responsibleGuid FROM WarehousesEntity WHERE id=:id")
//    fun getWarehouseById(id: Long): WarehousesEntity?

    @Query("SELECT COUNT(guid) FROM WarehousesEntity")
    fun getCountAllRecords(): Long

    @Query("SELECT COUNT(guid) FROM WarehousesEntity WHERE divisionGuid=:divisionGuid")
    fun getCountRecordsByDivision(divisionGuid: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWarehouses(warehouses: List<WarehousesEntity>)

    @Query("DELETE FROM WarehousesEntity")
    fun clearWarehousesTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveWarehousesReceiver(warehousesReceiver: List<WarehousesEntity>)

}