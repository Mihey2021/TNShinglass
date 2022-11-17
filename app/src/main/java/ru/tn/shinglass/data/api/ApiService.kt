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
    suspend fun getCellByBarcode(@Query("barcode") barcode: String, @Query("warehouseGuid") warehouseGuid: String): Response<Cells>

    @GET("getItemByBarcode")
    fun getItemByBarcode(@Query("barcode") barcode: String): Call<Nomenclature>

    @GET("getDivisionsList")
    suspend fun getAllDivisionsList(): Response<List<Division>>

    @GET("getCounterpartiesList")
    suspend fun getCounterpartiesList(@Query("part_name_inn") searchParam: String = ""): Response<List<Counterparty>>

    @GET("getInternalOrderList")
    suspend fun getInternalOrderList(): Response<List<ExternalDocument>>

    @POST("createInventoryOfGoods")
    suspend fun createInventoryOfGoods(@Body scanRecords: DocumentToUploaded): Response<CreatedDocumentDetails>

    @POST("createGoodsReceiptOrder")
    suspend fun createGoodsReceiptOrder(@Body scanRecords: DocumentToUploaded): Response<CreatedDocumentDetails>

    @POST("createRequirementInvoice")
    suspend fun createRequirementInvoice(@Body scanRecords: DocumentToUploaded): Response<CreatedDocumentDetails>
}