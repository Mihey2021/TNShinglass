package ru.tn.shinglass.dto.models

import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse

object DocumentHeaders{
    private var warehouse: Warehouse? = null
    private var physicalPerson: PhysicalPerson? = null
    private var division: Division? = null
    private var incomingDate: String = ""
    private var incomingNumber: String = ""

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
    fun setIncomingDate(incomingDate: String) {
        this.incomingDate = incomingDate
    }

    fun getIncomingNumber() = incomingNumber
    fun setIncomingNumber(incomingNumber: String) {
        this.incomingNumber = incomingNumber
    }
}
