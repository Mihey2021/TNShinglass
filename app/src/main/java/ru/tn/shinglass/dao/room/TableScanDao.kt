package ru.tn.shinglass.dao.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TableScanDao {
    @Query("SELECT * FROM TableScanEntity WHERE Owner =:owner AND OperationId=:operationId")
    fun getUserRecords(operationId: Long, owner: String)


}