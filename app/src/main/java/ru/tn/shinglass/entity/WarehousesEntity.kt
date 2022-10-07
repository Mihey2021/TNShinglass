package ru.tn.shinglass.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.Warehouse

@Entity
data class WarehousesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val guid: String,
    @ColumnInfo(name = "division_id")
    val division: Long,
) {

    fun toDto() =
        Warehouse(id = id, title = title, guid = guid, division = division)

    companion object {
        fun fromDto(dto: Warehouse) =
            WarehousesEntity(
                id = dto.id,
                title = dto.title,
                guid = dto.guid,
                division = dto.division,
            )

    }
}

