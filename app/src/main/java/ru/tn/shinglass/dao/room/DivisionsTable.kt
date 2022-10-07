package ru.tn.shinglass.dao.room

import androidx.room.Dao
import androidx.room.Query
import ru.tn.shinglass.entity.DivisionsTable

@Dao
interface DivisionsTable {
    @Query("SELECT * FROM DivisionsTable")
    fun getAllDivisions(): List<DivisionsTable>
}