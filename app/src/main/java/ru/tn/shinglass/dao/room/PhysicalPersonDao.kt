package ru.tn.shinglass.dao.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.tn.shinglass.entity.PhysicalPersonEntity
import ru.tn.shinglass.models.PhysicalPerson

@Dao
interface PhysicalPersonDao {
    @Query("SELECT * from PhysicalPersonEntity")
    fun getAllPhysicalPerson(): LiveData<List<PhysicalPersonEntity>>

    @Query("SELECT * from PhysicalPersonEntity WHERE guid = :guid")
    fun getPhysicalPersonByGuid(guid: String): PhysicalPersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePhysicalPerson(physicalPerson: List<PhysicalPersonEntity>)
}