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
    val cellReceiverTitle: String = "",
    val cellReceiverGuid: String = "",
    val ItemTitle: String,
    val ItemGUID: String,
    val ItemMeasureOfUnitTitle: String,
    val ItemMeasureOfUnitGUID: String,
    val Count: Double,
    val totalCount: Double,
    val isGroup: Boolean,
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
    val lastModified: Long = System.currentTimeMillis(),
) {
    fun toDto() =
        TableScan(
            id = id,
            OperationId = OperationId,
            OperationTitle = OperationTitle,
            cellTitle = cellTitle,
            cellGuid = cellGuid,
            cellReceiverGuid = cellReceiverGuid,
            cellReceiverTitle = cellReceiverTitle,
            ItemTitle = ItemTitle,
            ItemGUID = ItemGUID,
            ItemMeasureOfUnitTitle = ItemMeasureOfUnitTitle,
            ItemMeasureOfUnitGUID = ItemMeasureOfUnitGUID,
            Count = Count,
            totalCount = totalCount,
            isGroup = isGroup,
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
            docHeaders = docHeaders.toDto(),
            OwnerGuid = OwnerGuid,
            uploaded = uploaded,
            lastModified = lastModified
        )

    companion object {
        fun fromDto(dto: TableScan) =
            TableScanEntity(
                id = dto.id,
                OperationId = dto.OperationId,
                OperationTitle = dto.OperationTitle,
                cellTitle = dto.cellTitle,
                cellGuid = dto.cellGuid,
                cellReceiverTitle = dto.cellReceiverTitle,
                cellReceiverGuid = dto.cellReceiverGuid,
                ItemTitle = dto.ItemTitle,
                ItemGUID = dto.ItemGUID,
                ItemMeasureOfUnitTitle = dto.ItemMeasureOfUnitTitle,
                ItemMeasureOfUnitGUID = dto.ItemMeasureOfUnitGUID,
                Count = dto.Count,
                totalCount = dto.totalCount,
                isGroup = dto.isGroup,
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
                lastModified = dto.lastModified,
            )
    }
}

data class DocHeadersEmbeddable(
    @Embedded
    val warehouse: Warehouse? = null,
    @Embedded
    val warehouseReceiver: WarehouseReceiver? = null,
    @Embedded
    val physicalPerson: PhysicalPerson? = null,
    @Embedded
    val employee: Employee? = null,
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
        DocumentHeaders.setWarehouseReceiver(warehouseReceiver)
        DocumentHeaders.setPhysicalPerson(physicalPerson)
        DocumentHeaders.setEmployee(employee)
        DocumentHeaders.setDivision(division)
        DocumentHeaders.setCounterparty(counterparty)
        DocumentHeaders.setIncomingDate(incomingDate)
        DocumentHeaders.setIncomingNumber(incomingNumber)
        DocumentHeaders.setExternalDocumentSelected(externalDocumentSelected)
        return DocumentHeaders
    }

    companion object {
        fun fromDto(dto: DocumentHeaders) = dto.let {
            DocHeadersEmbeddable(
                warehouse = it.getWarehouse(),
                warehouseReceiver = it.getWarehouseReceiver(),
                physicalPerson = it.getPhysicalPerson(),
                employee = it.getEmployee(),
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