package ru.tn.shinglass.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse
import ru.tn.shinglass.models.WarehouseReceiver

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
    val usesLogistics: Boolean = false,
) {

    fun toDto() =
        Warehouse(warehouseTitle = title, warehouseGuid = guid, warehouseDivisionGuid = divisionGuid, warehouseResponsibleGuid =  responsibleGuid, usesLogistics = usesLogistics)
    fun toWarehouseReceiverDto() =
        WarehouseReceiver(warehouseReceiverTitle = title, warehouseReceiverGuid = guid, warehouseReceiverDivisionGuid = divisionGuid, warehouseReceiverResponsibleGuid =  responsibleGuid, warehouseReceiverUsesLogistics = usesLogistics)

    companion object {
        fun fromDto(dto: Warehouse) =
            WarehousesEntity(
                //id = dto.id,
                title = dto.warehouseTitle,
                guid = dto.warehouseGuid,
                divisionGuid = dto.warehouseDivisionGuid,
                responsibleGuid = dto.warehouseResponsibleGuid,
                usesLogistics = dto.usesLogistics,
            )

        fun fromWarehouseReceiverDto(dto: WarehouseReceiver) =
            WarehousesEntity(
                //id = dto.id,
                title = dto.warehouseReceiverTitle,
                guid = dto.warehouseReceiverGuid,
                divisionGuid = dto.warehouseReceiverDivisionGuid,
                responsibleGuid = dto.warehouseReceiverResponsibleGuid,
                usesLogistics = dto.warehouseReceiverUsesLogistics,
            )

    }

}

fun List<WarehousesEntity>.toDto(): List<Warehouse> = map(WarehousesEntity::toDto)
fun List<WarehousesEntity>.toWarehouseReceiverDto(): List<WarehouseReceiver> = map(WarehousesEntity::toWarehouseReceiverDto)
fun List<Warehouse>.toEntity(): List<WarehousesEntity> = map(WarehousesEntity::fromDto)
fun List<WarehouseReceiver>.toWarehouseReceiverEntity(): List<WarehousesEntity> = map(WarehousesEntity::fromWarehouseReceiverDto)

