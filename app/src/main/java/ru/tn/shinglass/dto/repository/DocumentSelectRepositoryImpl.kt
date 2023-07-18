package ru.tn.shinglass.dto.repository

import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.domain.repository.DocumentSelectRepository
import ru.tn.shinglass.error.ApiError
import ru.tn.shinglass.error.ApiServiceError
import ru.tn.shinglass.models.ExternalDocument
import java.io.IOException

class DocumentSelectRepositoryImpl : DocumentSelectRepository {

    //private val apiService = ApiUtils.getApiService()

    override suspend fun getInternalOrderList(): List<ExternalDocument> {
        try {
            if (ApiUtils.getApiService() != null) {
                val response =
                    ApiUtils.getApiService()!!.getInternalOrderList()
                if (!response.isSuccessful) {
                    throw ApiServiceError(response.errorBody()?.string() ?: response.message()) //ApiError(response.code(), response.message())
                }
                return response.body() ?: throw ApiServiceError(response.errorBody()?.string() ?: response.message()) //ApiError(response.code(), response.message())
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

    override suspend fun getRepairEstimate(): List<ExternalDocument> {
        try {
            if (ApiUtils.getApiService() != null) {
                val response =
                    ApiUtils.getApiService()!!.getRepairEstimate()
                if (!response.isSuccessful) {
                    throw ApiServiceError(response.errorBody()?.string() ?: response.message()) //ApiError(response.code(), response.message())
                }
                return response.body() ?: throw ApiServiceError(response.errorBody()?.string() ?: response.message()) //ApiError(response.code(), response.message())
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
}