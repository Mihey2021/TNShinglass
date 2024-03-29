package ru.tn.shinglass.dto.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.domain.repository.RetrofitRepository
import ru.tn.shinglass.dto.models.*
import ru.tn.shinglass.models.Nomenclature
import ru.tn.shinglass.error.ApiError
import ru.tn.shinglass.error.ApiServiceError
import ru.tn.shinglass.models.*
import java.io.IOException

class RetrofitRepositoryImpl : RetrofitRepository {

    private val apiService = ApiUtils.getApiService()
    override fun authorization(user: RequestLogin, callback: RetrofitRepository.Callback<User1C>) {
        TODO("Not yet implemented")
    }

//    override suspend fun getPhysicalPersonList() {
//        apiService?.getPhysicalPersonList()?.enqueue(getCallbackHandler(callback))
//    }

//    override fun getAllWarehousesList(callback: RetrofitRepository.Callback<List<Warehouse>>) {
//        apiService?.getAllWarehousesList()?.enqueue(getCallbackHandler(callback))
//    }

//    override fun getCellByBarcode(barcode: String, callback: RetrofitRepository.Callback<Cells>) {
//        apiService?.getCellByBarcode(barcode)?.enqueue(getCallbackHandler(callback))
//    }

    override suspend fun getCellByBarcode(barcode: String, warehouseGuid: String): Cell {
        try {
            if (apiService != null) {
                val response =
                    apiService.getCellByBarcode(barcode, warehouseGuid)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                return response.body() ?: throw ApiError(response.code(), response.message())
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

    override fun getItemByBarcode(
        barcode: String,
        callback: RetrofitRepository.Callback<Nomenclature>
    ) {
        apiService?.getItemByBarcode(barcode)?.enqueue(getCallbackHandler(callback))
    }

    override suspend fun getCellsList(warehouseGuid: String): List<Cell> {
        try {
            if (apiService != null) {
                val response =
                    apiService.getCellsList(warehouseGuid)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                return response.body() ?: throw ApiError(response.code(), response.message())
            } else {
                throw ApiServiceError("API service not ready")
            }
        } catch (e: IOException) {
            throw ApiServiceError(e.message.toString())
        } catch (e: Exception) {
            throw ApiServiceError(e.message.toString())
        }
    }

    override suspend fun getCellByGuid(cellGuid: String): Cell {
        try {
            if (apiService != null) {
                val response =
                    apiService.getCellByGuid(cellGuid)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                return response.body() ?: throw ApiError(response.code(), response.message())
            } else {
                throw ApiServiceError("API service not ready")
            }
        } catch (e: IOException) {
            throw ApiServiceError(e.message.toString())
        } catch (e: Exception) {
            throw ApiServiceError(e.message.toString())
        }
    }

    //    override fun createInventoryOfGoods(
//        scanRecords: List<TableScan>,
//        callback: RetrofitRepository.Callback<CreatedDocumentDetails>
//    ) {
//        apiService?.createInventoryOfGoods(DocumentToUploaded(records = scanRecords))?.enqueue(getCallbackHandler(callback))
//    }

//    override fun getAllDivisionsList(callback: RetrofitRepository.Callback<List<Division>>) {
//        apiService?.getAllDivisionsList()?.enqueue(getCallbackHandler(callback))
//    }

    private fun <T> getCallbackHandler(callback: RetrofitRepository.Callback<T>): Callback<T> {
        return object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (!response.isSuccessful) {
                    //callback.onError(RuntimeException("Operation execution error. The server returned the code: ${response.code()}"))
                    return
                }

                val body = response.body() ?: run {
                    callback.onError(RuntimeException("Operation execution error: The server did not return a response"))
                    return
                }

                callback.onSuccess(body)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onError(RuntimeException("Operation execution error:\n$t"))
            }
        }
    }
}