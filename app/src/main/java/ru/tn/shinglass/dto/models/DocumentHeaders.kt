package ru.tn.shinglass.dto.models

import ru.tn.shinglass.models.*

object DocumentHeaders{
    private var warehouse: Warehouse? = null
    private var physicalPerson: PhysicalPerson? = null
    private var division: Division? = null
    private var incomingDate: Long? = null
    private var incomingNumber: String = ""
    private var counterparty: Counterparty? = null
    private var externalDocumentSelected: Boolean = false

    fun getWarehouse(): Warehouse? = warehouse
    fun setWarehouse(warehouse: Warehouse?){
        this.warehouse = warehouse
    }

    fun getPhysicalPerson(): PhysicalPerson? = physicalPerson
    fun setPhysicalPerson(physicalPerson: PhysicalPerson?) {
        this.physicalPerson = physicalPerson
    }

    fun getDivision(): Division? = division
    fun setDivision(division: Division?) {
        this.division = division
    }

    fun getIncomingDate() = incomingDate
    fun setIncomingDate(incomingDate: Long?) {
        this.incomingDate = incomingDate
    }

    fun getIncomingNumber() = incomingNumber
    fun setIncomingNumber(incomingNumber: String) {
        this.incomingNumber = incomingNumber
    }

    fun getCounterparty() = counterparty
    fun setCounterparty(counterparty: Counterparty?) {
        this.counterparty = counterparty
    }

    fun getExternalDocumentSelected() = externalDocumentSelected
    fun setExternalDocumentSelected(externalDocumentSelected: Boolean) {
        this.externalDocumentSelected = externalDocumentSelected
    }
}
