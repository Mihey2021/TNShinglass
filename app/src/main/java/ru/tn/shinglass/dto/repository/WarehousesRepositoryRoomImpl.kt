package ru.tn.shinglass.dto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.dao.room.WarehousesDao
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.entity.*
import ru.tn.shinglass.error.ApiServiceError
import ru.tn.shinglass.models.Warehouse
import ru.tn.shinglass.models.WarehouseReceiver
import java.io.IOException
import java.lang.Exception

class WarehousesRepositoryRoomImpl(private val dao: WarehousesDao) : WarehousesRepository {
    //private val apiService = ApiUtils.getApiService()

    override val warehousesList: LiveData<List<Warehouse>> = dao.getAllWarehouses().map(List<WarehousesEntity>::toDto)
    override val warehouseReceiverList: LiveData<List<WarehouseReceiver>> = dao.getAllWarehouses().map(List<WarehousesEntity>::toWarehouseReceiverDto)

//    override fun getAllWarehousesFromDb(): List<Warehouse> =
//        dao.getAllWarehouses().map { warehousesEntity ->
//            warehousesEntity.toDto()
//        }

    override suspend fun getAllWarehousesList() {
        try {
            if(ApiUtils.getApiService() != null) {
                val response = ApiUtils.getApiService()!!.getAllWarehousesList()
                if (!response.isSuccessful) {
                    throw ApiServiceError(response.message()) //ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiServiceError(response.message()) //ApiError(response.code(), response.message())
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

    override suspend fun getAllWarehousesReceiverList() {
        try {
            if(ApiUtils.getApiService() != null) {
                val response = ApiUtils.getApiService()!!.getAllWarehousesReceiverList()
                if (!response.isSuccessful) {
                    throw ApiServiceError(response.message()) //ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiServiceError(response.message()) //ApiError(response.code(), response.message())
                dao.saveWarehousesReceiver(body.toWarehouseReceiverEntity())
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