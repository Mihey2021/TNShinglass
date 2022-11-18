package ru.tn.shinglass.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.models.*

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
    val totalCount: Double,
    val docCount: Double,
    val docTitle: String,
    val docGuid: String,
    val coefficient: Double,
    val qualityGuid: String,
    val qualityTitle: String,
    val WorkwearOrdinary: Boolean = false,
    val WorkwearDisposable: Boolean = false,
//    val DivisionId: Long,
//    val DivisionOrganization: Long,
//    val warehouseGuid: String,
    val PurposeOfUseTitle: String,
    val PurposeOfUse: String,
//    val PhysicalPersonTitle: String,
//    val PhysicalPersonGUID: String,
    @Embedded
    val docHeaders: DocHeadersEmbeddable,
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
            totalCount = totalCount,
            docCount = docCount,
            docTitle = docTitle,
            docGuid = docGuid,
            coefficient = coefficient,
            qualityGuid = qualityGuid,
            qualityTitle = qualityTitle,
            WorkwearOrdinary = WorkwearOrdinary,
            WorkwearDisposable = WorkwearDisposable,
//            DivisionId = DivisionId,
//            DivisionOrganization = DivisionOrganization,
//            warehouseGuid = warehouseGuid,
            PurposeOfUseTitle = PurposeOfUseTitle,
            PurposeOfUse = PurposeOfUse,
//            PhysicalPersonTitle = PhysicalPersonTitle,
//            PhysicalPersonGUID = PhysicalPersonGUID,
            docHeaders = docHeaders?.toDto(),
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
                totalCount = dto.totalCount,
                docCount = dto.docCount,
                docTitle = dto.docTitle,
                docGuid = dto.docGuid,
                coefficient = dto.coefficient,
                qualityGuid = dto.qualityGuid,
                qualityTitle = dto.qualityTitle,
                WorkwearOrdinary = dto.WorkwearOrdinary,
                WorkwearDisposable = dto.WorkwearDisposable,
//                DivisionId = dto.DivisionId,
//                DivisionOrganization = dto.DivisionOrganization,
//                warehouseGuid = dto.warehouseGuid,
                PurposeOfUseTitle = dto.PurposeOfUseTitle,
                PurposeOfUse = dto.PurposeOfUse,
//                PhysicalPersonTitle = dto.PhysicalPersonTitle,
//                PhysicalPersonGUID = dto.PhysicalPersonGUID,
                docHeaders = DocHeadersEmbeddable.fromDto(dto.docHeaders),
                OwnerGuid = dto.OwnerGuid,
                uploaded = dto.uploaded,
            )
    }
}

data class DocHeadersEmbeddable(
    @Embedded
    val warehouse: Warehouse? = null,
    @Embedded
    val physicalPerson: PhysicalPerson? = null,
    @Embedded
    val division: Division? = null,
    @Embedded
    val counterparty: Counterparty? = null,
    val incomingDate: Long? = null,
    val incomingNumber: String = "",
    val externalDocumentSelected: Boolean = false,
) {
    fun toDto(): DocumentHeaders {
        DocumentHeaders.setWarehouse(warehouse)
        DocumentHeaders.setPhysicalPerson(physicalPerson)
        DocumentHeaders.setDivision(division)
        DocumentHeaders.setCounterparty(counterparty)
        DocumentHeaders.setIncomingDate(incomingDate)
        DocumentHeaders.setIncomingNumber(incomingNumber)
        DocumentHeaders.setExternalDocumentSelected(externalDocumentSelected)
        return DocumentHeaders
    }

    companion object {
        fun fromDto(dto: DocumentHeaders) = dto?.let {
            DocHeadersEmbeddable(
                warehouse = it.getWarehouse(),
                physicalPerson = it.getPhysicalPerson(),
                division = it.getDivision(),
                counterparty = it.getCounterparty(),
                incomingDate = it.getIncomingDate(),
                incomingNumber = it.getIncomingNumber(),
                externalDocumentSelected = it.getExternalDocumentSelected(),
            )
        }
    }
}

fun List<TableScanEntity>.toDto(): List<TableScan> = map(TableScanEntity::toDto)
fun List<TableScan>.toEntity(): List<TableScanEntity> = map(TableScanEntity::fromDto)