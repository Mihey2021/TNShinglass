package ru.tn.shinglass.dao.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.tn.shinglass.entity.WarehousesEntity

@Dao
interface WarehousesDao {
    @Query("SELECT * FROM WarehousesEntity")
    fun getAllWarehouses(): List<WarehousesEntity>

    @Query("SELECT id, title, guid, division_id FROM WarehousesEntity WHERE division_id=:divisionId")
    fun getAllWarehousesByDivision(divisionId: Long): List<WarehousesEntity>

    @Query("SELECT id, title, guid, division_id FROM WarehousesEntity WHERE guid=:guid")
    fun getWarehouseByGuid(guid: String): WarehousesEntity?

    @Query("SELECT COUNT(id) FROM WarehousesEntity")
    fun getCountAllRecords(): Long

    @Query("SELECT COUNT(id) FROM WarehousesEntity WHERE division_id=:divisionId")
    fun getCountRecordsByDivision(divisionId: Long): Long

    @Insert
    fun insert(warehouse: WarehousesEntity)

    fun save(warehouse: WarehousesEntity) {
        insert(warehouse)
    }
}