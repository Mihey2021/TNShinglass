package ru.tn.shinglass.data.api

import retrofit2.Call
import retrofit2.http.*
import ru.tn.shinglass.dto.models.CreatedDocumentDetails
import ru.tn.shinglass.dto.models.InventoryOfGoods
import ru.tn.shinglass.dto.models.RequestLogin
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.entity.Nomenclature
import ru.tn.shinglass.models.Cells
import ru.tn.shinglass.models.PhisicalPerson
import ru.tn.shinglass.models.Warehouse

interface ApiService {
    @POST("validationAuth")
    fun authorization(@Body authUser: RequestLogin): Call<User1C>

    @GET("getWarehousesList")
    fun getWarehousesListByDivision(@Query("division") divisionGuid: String = ""): Call<List<Warehouse>>

    @GET("getWarehousesList")
    fun getAllWarehousesList(): Call<List<Warehouse>>

    fun getWarehousesList(divisionGuid: String = "") {
        if (divisionGuid.isBlank()) getAllWarehousesList() else getWarehousesListByDivision(divisionGuid)
    }

    @GET("getPhisicalPersonList")
    fun getPhisicalPersonList(): Call<List<PhisicalPerson>>

    @GET("getCellByBarcode")
    fun getCellByBarcode(@Query("barcode") barcode: String): Call<Cells>

    @GET("getItemByBarcode")
    fun getItemByBarcode(@Query("barcode") barcode: String): Call<Nomenclature>

    @POST("createInventoryOfGoods")
    fun createInventoryOfGoods(@Body scanRecords: InventoryOfGoods): Call<CreatedDocumentDetails>
}