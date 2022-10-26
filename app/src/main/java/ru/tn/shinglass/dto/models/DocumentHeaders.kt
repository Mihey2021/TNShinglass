package ru.tn.shinglass.dto.models

import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse

object DocumentHeaders{
    private var warehouse: Warehouse? = null
    private var physicalPerson: PhysicalPerson? = null

    fun getWarehouse(): Warehouse? = warehouse
    fun setWarehouse(warehouse: Warehouse?){
        this.warehouse = warehouse
    }

    fun getPhysicalPerson(): PhysicalPerson? = physicalPerson
    fun setPhysicalPerson(physicalPerson: PhysicalPerson?) {
        this.physicalPerson = physicalPerson
    }
}
