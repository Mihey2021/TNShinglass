package ru.tn.shinglass.dto.models

import ru.tn.shinglass.models.PhisicalPerson
import ru.tn.shinglass.models.Warehouse

object DocumentHeaders{
    private var warehouse: Warehouse? = null
    private var physicalPerson: PhisicalPerson? = null

    fun getWarehouse(): Warehouse? = warehouse
    fun setWarehouse(warehouse: Warehouse?){
        this.warehouse = warehouse
    }

    fun getPhysicalPerson(): PhisicalPerson? = physicalPerson
    fun setPhysicalPerson(physicalPerson: PhisicalPerson?) {
        this.physicalPerson = physicalPerson
    }
}
