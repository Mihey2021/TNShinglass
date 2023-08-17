package ru.tn.shinglass.domain.repository

import androidx.lifecycle.LiveData
import ru.tn.shinglass.models.Counterparty
import ru.tn.shinglass.models.Employee
import ru.tn.shinglass.models.PhysicalPerson

interface EmployeeRepository {
    val employees: LiveData<List<Employee>>
    suspend fun getEmployeeList(partName: String? = null): List<Employee>
    fun getEmployeeByGuid(guid: String): Employee?
    fun saveEmployee(physicalPersons: List<Employee>)
}