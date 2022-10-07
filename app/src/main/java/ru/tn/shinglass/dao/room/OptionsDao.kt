package ru.tn.shinglass.dao.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.tn.shinglass.entity.OptionsEntity

@Dao
interface OptionsDao {
    @Query("SELECT * FROM OptionsEntity ORDER BY id ASC")
    fun getAllOptions(): LiveData<List<OptionsEntity>>

    @Query("SELECT COUNT(id) FROM OptionsEntity")
    fun getCountRecords(): Long

    @Insert
    fun insert(option: OptionsEntity)

    @Query("UPDATE OptionsEntity SET type = :type, title = :title WHERE id = :id")
    fun updateOptionById(id: Long, type: String, title: String)

    fun save(option: OptionsEntity) {
        if (option.id == 0L) insert(option) else updateOptionById(option.id, option.type, option.title)
    }
}