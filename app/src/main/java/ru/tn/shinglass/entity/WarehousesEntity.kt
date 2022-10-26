package ru.tn.shinglass.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse

@Entity
data class WarehousesEntity(
    //@PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @PrimaryKey(autoGenerate = false)
    val guid: String,
    //@ColumnInfo(name = "division_guid")
    val divisionGuid: String,
    val responsibleGuid: String,
) {

    fun toDto() =
        Warehouse(id = id, title = title, guid = guid, divisionGuid = divisionGuid, responsibleGuid = responsibleGuid)

    companion object {
        fun fromDto(dto: Warehouse) =
            WarehousesEntity(
                id = dto.id,
                title = dto.title,
                guid = dto.guid,
                divisionGuid = dto.divisionGuid,
                responsibleGuid = dto.responsibleGuid
            )

    }
}

fun List<WarehousesEntity>.toDto(): List<Warehouse> = map(WarehousesEntity::toDto)
fun List<Warehouse>.toEntity(): List<WarehousesEntity> = map(WarehousesEntity::fromDto)

