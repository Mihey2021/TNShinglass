package ru.tn.shinglass.dto.models

import ru.tn.shinglass.R
import ru.tn.shinglass.models.Counterparty
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse

object DocumentHeaders{
    private var warehouse: Warehouse? = null
    private var physicalPerson: PhysicalPerson? = null
    private var division: Division? = null
    private var incomingDate: Long? = null
    private var incomingNumber: String = ""
    private var counterparty: Counterparty? = null

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
}

enum class HeaderFields(val viewId: Int) {
    WAREHOUSE(R.id.warehouseTextInputLayout),
    PHYSICAL_PERSON(R.id.physicalPersonTextInputLayout),
    DIVISION(R.id.divisionTextInputLayout),
    COUNTERPARTY(R.id.counterpartyTextInputLayout),
    INCOMING_DATE(R.id.incomingDateTextInputLayout),
    INCOMING_NUMBER(R.id.incomingNumberTextInputLayout),
}
