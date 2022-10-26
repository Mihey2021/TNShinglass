package ru.tn.shinglass.dao.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.tn.shinglass.entity.DivisionsEntity
import ru.tn.shinglass.entity.OptionsEntity

@Dao
interface DivisionsDao {
    @Query("SELECT * FROM DivisionsEntity")
    fun getAllDivisions(): List<DivisionsEntity>

    @Query("SELECT * FROM DivisionsEntity WHERE guid = :guid")
    fun getDivisionByGuid(guid: String): DivisionsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(division: DivisionsEntity)

    fun save(division: DivisionsEntity) {
        insert(division)
    }
}