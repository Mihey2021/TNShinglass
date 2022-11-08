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
    //val id: Long = 0,
    val title: String,
    @PrimaryKey(autoGenerate = false)
    val guid: String,
    val defaultWarehouseGuid: String = "",
){
    fun toDto() =
        Division(divisionTitle = title, divisionGuid = guid, divisionDefaultWarehouseGuid = defaultWarehouseGuid)

    companion object {
        fun fromDto(dto: Division) =
            DivisionsEntity(
                title = dto.divisionTitle,
                guid = dto.divisionGuid,
                defaultWarehouseGuid = dto.divisionDefaultWarehouseGuid,
            )
    }
}

fun List<DivisionsEntity>.toDto(): List<Division> = map(DivisionsEntity::toDto)
fun List<Division>.toEntity(): List<DivisionsEntity> = map(DivisionsEntity::fromDto)