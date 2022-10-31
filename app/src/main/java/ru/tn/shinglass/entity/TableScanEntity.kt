package ru.tn.shinglass.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.TableScan

@Entity
data class TableScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val OperationId: Long,
    val OperationTitle: String,
    val cellTitle: String,
    val cellGuid: String,
    val ItemTitle: String,
    val ItemGUID: String,
    val ItemMeasureOfUnitTitle: String,
    val ItemMeasureOfUnitGUID: String,
    val Count: Double,
    val coefficient: Double,
    val qualityGuid: String,
    val qualityTitle: String,
    val WorkwearOrdinary: Boolean = false,
    val WorkwearDisposable: Boolean = false,
    val DivisionId: Long,
    val DivisionOrganization: Long,
    val warehouseGuid: String,
    val PurposeOfUseTitle: String,
    val PurposeOfUse: String,
    val PhysicalPersonTitle: String,
    val PhysicalPersonGUID: String,
    val OwnerGuid: String,
    val uploaded: Boolean = false,
    val docNameIn1C: String = "",
) {
    fun toDto() =
        TableScan(
            id = id,
            OperationId = OperationId,
            OperationTitle = OperationTitle,
            cellTitle = cellTitle,
            cellGuid = cellGuid,
            ItemTitle = ItemTitle,
            ItemGUID = ItemGUID,
            ItemMeasureOfUnitTitle = ItemMeasureOfUnitTitle,
            ItemMeasureOfUnitGUID = ItemMeasureOfUnitGUID,
            Count = Count,
            coefficient = coefficient,
            qualityGuid = qualityGuid,
            qualityTitle = qualityTitle,
            WorkwearOrdinary = WorkwearOrdinary,
            WorkwearDisposable = WorkwearDisposable,
            DivisionId = DivisionId,
            DivisionOrganization = DivisionOrganization,
            warehouseGuid = warehouseGuid,
            PurposeOfUseTitle = PurposeOfUseTitle,
            PurposeOfUse = PurposeOfUse,
            PhysicalPersonTitle = PhysicalPersonTitle,
            PhysicalPersonGUID = PhysicalPersonGUID,
            OwnerGuid = OwnerGuid,
            uploaded = uploaded,
        )

    companion object {
        fun fromDto(dto: TableScan) =
            TableScanEntity(
                id = dto.id,
                OperationId = dto.OperationId,
                OperationTitle = dto.OperationTitle,
                cellTitle = dto.cellTitle,
                cellGuid = dto.cellGuid,
                ItemTitle = dto.ItemTitle,
                ItemGUID = dto.ItemGUID,
                ItemMeasureOfUnitTitle = dto.ItemMeasureOfUnitTitle,
                ItemMeasureOfUnitGUID = dto.ItemMeasureOfUnitGUID,
                Count = dto.Count,
                coefficient = dto.coefficient,
                qualityGuid = dto.qualityGuid,
                qualityTitle = dto.qualityTitle,
                WorkwearOrdinary = dto.WorkwearOrdinary,
                WorkwearDisposable = dto.WorkwearDisposable,
                DivisionId = dto.DivisionId,
                DivisionOrganization = dto.DivisionOrganization,
                warehouseGuid = dto.warehouseGuid,
                PurposeOfUseTitle = dto.PurposeOfUseTitle,
                PurposeOfUse = dto.PurposeOfUse,
                PhysicalPersonTitle = dto.PhysicalPersonTitle,
                PhysicalPersonGUID = dto.PhysicalPersonGUID,
                OwnerGuid = dto.OwnerGuid,
                uploaded = dto.uploaded,
            )
    }
}

fun List<TableScanEntity>.toDto(): List<TableScan> = map(TableScanEntity::toDto)
fun List<TableScan>.toEntity(): List<TableScanEntity> = map(TableScanEntity::fromDto)