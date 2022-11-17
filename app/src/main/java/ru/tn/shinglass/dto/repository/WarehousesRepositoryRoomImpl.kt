package ru.tn.shinglass.dto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.dao.room.WarehousesDao
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.entity.WarehousesEntity
import ru.tn.shinglass.entity.toDto
import ru.tn.shinglass.entity.toEntity
import ru.tn.shinglass.error.ApiError
import ru.tn.shinglass.error.ApiServiceError
import ru.tn.shinglass.error.NetworkError
import ru.tn.shinglass.error.UnknownError
import ru.tn.shinglass.models.Warehouse
import java.io.IOException
import java.lang.Exception

class WarehousesRepositoryRoomImpl(private val dao: WarehousesDao) : WarehousesRepository {
    private val apiService = ApiUtils.getApiService()

    override val warehousesList: LiveData<List<Warehouse>> = dao.getAllWarehouses().map(List<WarehousesEntity>::toDto)

//    override fun getAllWarehousesFromDb(): List<Warehouse> =
//        dao.getAllWarehouses().map { warehousesEntity ->
//            warehousesEntity.toDto()
//        }

    override suspend fun getAllWarehousesList() {
        try {
            if(apiService != null) {
                val response = apiService.getAllWarehousesList()
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.saveWarehouses(body.toEntity())
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

    override fun getAllWarehousesByDivision(divisionGuid: String) =
        dao.getAllWarehousesByDivision(divisionGuid).map { warehousesEntity ->
            warehousesEntity.toDto()
        }

    override fun getWarehouseByGuid(guid: String): Warehouse? {
        val warehouse = dao.getWarehouseByGuid(guid) ?: return null
        return warehouse.toDto()
    }

    override fun getWarehousesCountRecords() = dao.getCountAllRecords()

    override fun getWarehousesCountRecordsByDivision(divisionGuid: String) =
        dao.getCountRecordsByDivision(divisionGuid)

    override fun saveWarehouses(warehouses: List<Warehouse>) {
        //warehouses.forEach { warehouse -> dao.save(WarehousesEntity.fromDto(warehouse)) }
    }

}