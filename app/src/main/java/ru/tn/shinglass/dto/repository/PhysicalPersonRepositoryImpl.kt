package ru.tn.shinglass.dto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.dao.room.PhysicalPersonDao
import ru.tn.shinglass.domain.repository.PhysicalPersonRepository
import ru.tn.shinglass.entity.PhysicalPersonEntity
import ru.tn.shinglass.entity.toDto
import ru.tn.shinglass.entity.toEntity
import ru.tn.shinglass.error.ApiError
import ru.tn.shinglass.error.ApiServiceError
import ru.tn.shinglass.error.NetworkError
import ru.tn.shinglass.error.UnknownError
import ru.tn.shinglass.models.PhysicalPerson
import java.io.IOException
import java.lang.Exception

class PhysicalPersonRepositoryImpl(private val dao: PhysicalPersonDao) : PhysicalPersonRepository {
    //private val apiService = ApiUtils.getApiService()

    override val physicalPersons = dao.getAllPhysicalPerson().map(List<PhysicalPersonEntity>::toDto)

    override suspend fun getPhysicalPersonList() {
        try {
            if(ApiUtils.getApiService() != null) {
                val response = ApiUtils.getApiService()!!.getPhysicalPersonList()
                if (!response.isSuccessful) {
                    throw ApiServiceError(response.errorBody()?.string() ?: response.message()) //ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiServiceError(response.errorBody()?.string() ?: response.message()) //ApiError(response.code(), response.message())
                dao.savePhysicalPerson(body.toEntity())
            } else {
                throw ApiServiceError("API service not ready")
            }
        } catch (e: IOException) {
            //throw NetworkError
            throw ApiServiceError(e.message.toString())
        } catch (e: Exception) {
            //throw UnknownError
            throw ApiServiceError(e.message.toString())
        }
    }

    override fun getPhysicalPersonByGuid(guid: String): PhysicalPerson? =
        dao.getPhysicalPersonByGuid(guid)?.toDto()

    override fun savePhysicalPerson(physicalPersons: List<PhysicalPerson>) {
        //physicalPersons.forEach { physicalPerson -> dao.savePhysicalPerson(PhysicalPersonEntity.fromDto(physicalPerson)) }
    }
}