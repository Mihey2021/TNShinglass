package ru.tn.shinglass.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse

@Entity
data class WarehousesEntity(
    //@PrimaryKey(autoGenerate = true)
    //val id: Long = 0,
    val title: String,
    @PrimaryKey(autoGenerate = false)
    val guid: String,
    //@ColumnInfo(name = "division_guid")
    val divisionGuid: String,
    val responsibleGuid: String,
) {

    fun toDto() =
        Warehouse(warehouseTitle = title, warehouseGuid = guid, warehouseDivisionGuid = divisionGuid, warehouseResponsibleGuid =  responsibleGuid)

    companion object {
        fun fromDto(dto: Warehouse) =
            WarehousesEntity(
                //id = dto.id,
                title = dto.warehouseTitle,
                guid = dto.warehouseGuid,
                divisionGuid = dto.warehouseDivisionGuid,
                responsibleGuid = dto.warehouseResponsibleGuid
            )

    }
}

fun List<WarehousesEntity>.toDto(): List<Warehouse> = map(WarehousesEntity::toDto)
fun List<Warehouse>.toEntity(): List<WarehousesEntity> = map(WarehousesEntity::fromDto)

