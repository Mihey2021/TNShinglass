package ru.tn.shinglass.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WarehousesTable (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val guid: String,
    @ColumnInfo(name = "division_id")
    val division: Long,
){
}