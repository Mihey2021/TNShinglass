package ru.tn.shinglass.domain.repository

import androidx.lifecycle.LiveData
import ru.tn.shinglass.entity.DivisionsEntity
import ru.tn.shinglass.models.Division

interface DivisionRepository {
    var divisionsList: LiveData<List<Division>>
    suspend fun getAllDivisions()
    fun getDivisionByGuid(guid: String): Division?
    fun saveDivisions(divisions: List<Division>)
}