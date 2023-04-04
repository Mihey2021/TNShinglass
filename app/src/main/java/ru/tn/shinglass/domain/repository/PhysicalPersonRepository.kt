package ru.tn.shinglass.domain.repository

import androidx.lifecycle.LiveData
import ru.tn.shinglass.models.PhysicalPerson

interface PhysicalPersonRepository {
    val physicalPersons: LiveData<List<PhysicalPerson>>
    suspend fun getPhysicalPersonList()
    fun getPhysicalPersonByGuid(guid: String): PhysicalPerson?
    fun savePhysicalPerson(physicalPersons: List<PhysicalPerson>)
}