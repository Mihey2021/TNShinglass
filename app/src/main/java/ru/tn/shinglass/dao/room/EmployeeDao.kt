package ru.tn.shinglass.dao.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.tn.shinglass.entity.EmployeeEntity
import ru.tn.shinglass.entity.PhysicalPersonEntity
import ru.tn.shinglass.models.PhysicalPerson

@Dao
interface EmployeeDao {
    @Query("SELECT * from EmployeeEntity")
    fun getAllEmployee(): LiveData<List<EmployeeEntity>>

    @Query("SELECT * from EmployeeEntity WHERE guid = :guid")
    fun getEmployeeByGuid(guid: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveEmployee(employee: List<EmployeeEntity>)

    @Query("DELETE FROM EmployeeEntity")
    fun clearEmployeeTab()
}