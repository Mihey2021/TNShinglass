package ru.tn.shinglass.dao.room

import androidx.room.Dao
import androidx.room.Query
import ru.tn.shinglass.entity.WarehousesTable

@Dao
interface WarehousesTableDao {
    @Query("SELECT id, title, guid, division_id, division_title FROM WarehousesTable, DivisionsTable WHERE division_id=:divisionId")
    fun getAllWarehousesByDivision(divisionId: Long): List<WarehousesTable>
}