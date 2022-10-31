package ru.tn.shinglass.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.Warehouse

@Entity
data class DivisionsEntity (
    //@PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @PrimaryKey(autoGenerate = false)
    val guid: String,
    val defaultWarehouseGuid: String = "",
){
    fun toDto() =
        Division(id = id, title = title, guid = guid, defaultWarehouseGuid = defaultWarehouseGuid)

    companion object {
        fun fromDto(dto: Division) =
            DivisionsEntity(
                id = dto.id,
                title = dto.title,
                guid = dto.guid,
                defaultWarehouseGuid = dto.defaultWarehouseGuid,
            )
    }
}

fun List<DivisionsEntity>.toDto(): List<Division> = map(DivisionsEntity::toDto)
fun List<Division>.toEntity(): List<DivisionsEntity> = map(DivisionsEntity::fromDto)