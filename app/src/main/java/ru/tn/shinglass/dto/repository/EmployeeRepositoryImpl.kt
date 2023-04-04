package ru.tn.shinglass.dto.repository

import androidx.lifecycle.map
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.dao.room.EmployeeDao
import ru.tn.shinglass.domain.repository.EmployeeRepository
import ru.tn.shinglass.entity.EmployeeEntity
import ru.tn.shinglass.entity.toDto
import ru.tn.shinglass.entity.toEntity
import ru.tn.shinglass.error.ApiError
import ru.tn.shinglass.error.ApiServiceError
import ru.tn.shinglass.models.Employee
import java.io.IOException
import java.lang.Exception

class EmployeeRepositoryImpl(private val dao: EmployeeDao) : EmployeeRepository {
    private val apiService = ApiUtils.getApiService()

    override val employees = dao.getAllEmployee().map(List<EmployeeEntity>::toDto)

    override suspend fun getEmployeeList() {
        try {
            if(apiService != null) {
                val response = apiService.getEmployeeList()
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.saveEmployee(body.toEntity())
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

    override fun getEmployeeByGuid(guid: String): Employee? =
        dao.getEmployeeByGuid(guid)?.toDto()

    override fun saveEmployee(Employees: List<Employee>) {
        //Employees.forEach { Employee -> dao.saveEmployee(EmployeeEntity.fromDto(Employee)) }
    }
}