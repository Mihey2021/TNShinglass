package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.entity.DivisionsEntity
import ru.tn.shinglass.models.Division

interface DivisionRepository {
    fun getAllDivisions(): List<Division>
    fun getDivisionByGuid(guid: String): Division?
    fun saveDivisions(divisions: List<Division>)
}