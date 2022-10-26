package ru.tn.shinglass.dto.repository

import ru.tn.shinglass.dao.room.DivisionsDao
import ru.tn.shinglass.domain.repository.DivisionRepository
import ru.tn.shinglass.entity.DivisionsEntity
import ru.tn.shinglass.models.Division

class DivisionRepositoryImpl(private val dao: DivisionsDao) : DivisionRepository {
    override fun getAllDivisions(): List<Division> =
        dao.getAllDivisions().map { divisionsEntity ->
            divisionsEntity.toDto()
        }

    override fun getDivisionByGuid(guid: String): Division? {
        return dao.getDivisionByGuid(guid)?.toDto()
    }

    override fun saveDivisions(divisions: List<Division>) {
        divisions.forEach { division -> dao.save(DivisionsEntity.fromDto(division)) }
    }
}