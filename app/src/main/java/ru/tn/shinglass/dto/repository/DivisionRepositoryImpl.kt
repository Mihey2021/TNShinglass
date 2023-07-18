package ru.tn.shinglass.dto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.dao.room.DivisionsDao
import ru.tn.shinglass.domain.repository.DivisionRepository
import ru.tn.shinglass.entity.DivisionsEntity
import ru.tn.shinglass.entity.toDto
import ru.tn.shinglass.entity.toEntity
import ru.tn.shinglass.error.*
import ru.tn.shinglass.models.Division
import java.io.IOException

class DivisionRepositoryImpl(private val dao: DivisionsDao) : DivisionRepository {

    //private val apiService = ApiUtils.getApiService()

    override var divisionsList: LiveData<List<Division>> = dao.getAllDivisions().map(List<DivisionsEntity>::toDto)


//    override fun getAllDivisions(): List<Division> =
//        dao.getAllDivisions().map { divisionsEntity ->
//            divisionsEntity.toDto()
//        }

    override suspend fun getAllDivisions() {
        try {
            if (ApiUtils.getApiService() != null) {
                val response = ApiUtils.getApiService()!!.getAllDivisionsList()
                if (!response.isSuccessful) {
                    throw ApiServiceError(
                        "Code: ${response.code()}\n${response.message()}\n${
                            response.errorBody()?.string() ?: ""
                        }"
                    )
                }
                val body = response.body() ?: throw ApiServiceError(response.message()) //ApiError(response.code(), response.message())
                dao.saveDivisions(body.toEntity())
            } else {
                throw ApiServiceError("API service not ready")
            }
        } catch (e: IOException) {
            //throw NetworkError
            throw ApiServiceError(e.message.toString())
        } catch (e: Exception) {
            throw ApiServiceError(e.message.toString())
        }
    }

    override fun getDivisionByGuid(guid: String): Division? {
        return dao.getDivisionByGuid(guid)?.toDto()
    }

    override fun saveDivisions(divisions: List<Division>) {
        //divisions.forEach { division -> dao.save(DivisionsEntity.fromDto(division)) }
    }
}