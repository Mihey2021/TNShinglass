package ru.tn.shinglass.dto.models

import androidx.room.Embedded
import ru.tn.shinglass.models.*

class DocumentToUploaded (
    val docType: DocType,
    val virtualCellGuid: String = "",
    val docHeaders: DocHeaders,
    val records: List<TableScan>,
)

class DocHeaders(objDocumentHeaders: DocumentHeaders){
    private var warehouse: Warehouse? = null
    private var warehouseReceiver: WarehouseReceiver? = null
    private var physicalPerson: PhysicalPerson? = null
    private var employee: Employee? = null
    private var division: Division? = null
    private var counterparty: Counterparty? = null
    private var incomingDate: Long? = null
    private var incomingNumber: String = ""

    init {
        this.warehouse = objDocumentHeaders.getWarehouse()
        this.warehouseReceiver = objDocumentHeaders.getWarehouseReceiver()
        this.physicalPerson = objDocumentHeaders.getPhysicalPerson()
        this.employee = objDocumentHeaders.getEmployee()
        this.division = objDocumentHeaders.getDivision()
        this.counterparty = objDocumentHeaders.getCounterparty()
        this.incomingDate = objDocumentHeaders.getIncomingDate()
        this.incomingNumber = objDocumentHeaders.getIncomingNumber()
    }
}