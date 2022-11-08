package ru.tn.shinglass.data.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import ru.tn.shinglass.dto.models.CreatedDocumentDetails
import ru.tn.shinglass.dto.models.DocumentToUploaded
import ru.tn.shinglass.dto.models.RequestLogin
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.entity.Nomenclature
import ru.tn.shinglass.models.*

interface ApiService {
    @POST("validationAuth")
    fun authorization(@Body authUser: RequestLogin): Call<User1C>

    @GET("getWarehousesList")
    fun getWarehousesListByDivision(@Query("division") divisionGuid: String = ""): Call<List<Warehouse>>

    @GET("getWarehousesList")
    suspend fun getAllWarehousesList(): Response<List<Warehouse>>

//    fun getWarehousesList(divisionGuid: String = "") {
//        if (divisionGuid.isBlank()) getAllWarehousesList() else getWarehousesListByDivision(divisionGuid)
//    }

    @GET("getPhysicalPersonList")
    suspend fun getPhysicalPersonList(): Response<List<PhysicalPerson>>

    @GET("getCellByBarcode")
    fun getCellByBarcode(@Query("barcode") barcode: String): Call<Cells>

    @GET("getItemByBarcode")
    fun getItemByBarcode(@Query("barcode") barcode: String): Call<Nomenclature>

    @POST("createInventoryOfGoods")
    suspend fun createInventoryOfGoods(@Body scanRecords: DocumentToUploaded): Response<CreatedDocumentDetails>

    @GET("getDivisionsList")
    suspend fun getAllDivisionsList(): Response<List<Division>>

    @GET("getCounterpartiesList")
    suspend fun getCounterpartiesList(@Query("part_name_inn") searchParam: String = ""): Response<List<Counterparty>>

    @POST("createGoodsReceiptOrder")
    suspend fun createGoodsReceiptOrder(@Body scanRecords: DocumentToUploaded): Response<CreatedDocumentDetails>

}