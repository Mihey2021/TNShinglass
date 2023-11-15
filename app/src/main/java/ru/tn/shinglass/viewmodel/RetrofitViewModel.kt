package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.tn.shinglass.domain.repository.RetrofitRepository
import ru.tn.shinglass.dto.repository.RetrofitRepositoryImpl
import ru.tn.shinglass.models.Nomenclature
import ru.tn.shinglass.models.*
import java.lang.Exception


class RetrofitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RetrofitRepository = RetrofitRepositoryImpl()

    private val _listDataPhysicalPersons: MutableLiveData<List<PhysicalPerson>> =
        MutableLiveData(listOf())
    val listDataPhysicalPersons: LiveData<List<PhysicalPerson>>
        get() = _listDataPhysicalPersons

    private val _listDataWarehouses: MutableLiveData<List<Warehouse>> = MutableLiveData(listOf())
    val listDataWarehouses: LiveData<List<Warehouse>>
        get() = _listDataWarehouses

    private val _listNomenclatureStocks: MutableLiveData<List<NomenclatureStocks>> = MutableLiveData(listOf())
    val listNomenclatureStocks: LiveData<List<NomenclatureStocks>>
        get() = _listNomenclatureStocks

    private val _listGvzo: MutableLiveData<List<Gvzo>> = MutableLiveData(null)
    val listGvzo: LiveData<List<Gvzo>>
        get() = _listGvzo

    private val _cellData: MutableLiveData<Cell> = MutableLiveData(null)
    val cellData: LiveData<Cell>
        get() = _cellData

    private val _virtualCellData: MutableLiveData<Cell> = MutableLiveData(null)
    val virtualCellData: LiveData<Cell>
        get() = _virtualCellData

    private val _cellListData: MutableLiveData<List<Cell>> = MutableLiveData(listOf())
    val cellListData: LiveData<List<Cell>>
        get() = _cellListData

    private val _itemData: MutableLiveData<Nomenclature> = MutableLiveData(null)
    val itemData: LiveData<Nomenclature>
        get() = _itemData

    private val _itemListData: MutableLiveData<List<Nomenclature>> = MutableLiveData(null)
    val itemListData: LiveData<List<Nomenclature>>
        get() = _itemListData

    private val _physicalPerson: MutableLiveData<PhysicalPerson> = MutableLiveData(PhysicalPerson("", ""))
    val physicalPerson: LiveData<PhysicalPerson>
        get() = _physicalPerson

    private val _barcodesData: MutableLiveData<List<Barcode>> = MutableLiveData(listOf())
    val barcodesData: LiveData<List<Barcode>>
        get() = _barcodesData

//    private val _docCreated: MutableLiveData<CreatedDocumentDetails?> = MutableLiveData(null)
//    val docCreated: LiveData<CreatedDocumentDetails?>
//        get() = _docCreated

    //private val _cellListData: MutableLiveData<List<Cell>> = MutableLiveData(null)


    private val _requestError: MutableLiveData<RequestError?> = MutableLiveData(null)
    val requestError: LiveData<RequestError?>
        get() = _requestError

    private val _dataState = MutableLiveData<ModelState>()
    val dataState: LiveData<ModelState>
        get() = _dataState

//    fun getPhysicalPersonList() {
//        repository.getPhysicalPersonList(object :
//            RetrofitRepository.Callback<List<PhysicalPerson>> {
//            override fun onSuccess(receivedData: List<PhysicalPerson>) {
//                _listDataPhysicalPersons.value = receivedData
//                _requestError.value = null
//            }
//
//            override fun onError(e: Exception) {
//                _requestError.value = RequestError(e.message.toString(), "getPhisicalPersonList")
//                //super.onError(e)
//            }
//        })
//    }

//    fun getAllWarehouses() {
//        repository.getAllWarehousesList(object : RetrofitRepository.Callback<List<Warehouse>> {
//            override fun onSuccess(receivedData: List<Warehouse>) {
//                _listDataWarehouses.value = receivedData
//                _requestError.value = null
//            }
//
//            override fun onError(e: Exception) {
//                _requestError.value = RequestError(e.message.toString(), "getAllWarehousesList")
//                //super.onError(e)
//            }
//        })
//    }

//    fun getCellByBarcode(barcode: String) {
//        repository.getCellByBarcode(barcode, object : RetrofitRepository.Callback<Cells> {
//            override fun onSuccess(receivedData: Cells) {
//                _cellData.value = receivedData
//                _requestError.value = null
//            }
//
//            override fun onError(e: Exception) {
//                _requestError.value = RequestError(e.message.toString(), "getCellByBarcode")
//                //super.onError(e)
//            }
//        })
//    }

    fun getBarcodesByItem(itemGuid: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _barcodesData.value = repository.getBarcodesByItem(itemGuid)
                //_requestError.value = null
                _dataState.value = ModelState()
            } catch (e: Exception) {
                //_requestError.value = RequestError(e.message.toString(), "getCellByBarcode")
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getBarcodesByItem",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("itemGuid", itemGuid))
                )
            }
        }
    }

    fun getCellByBarcode(barcode: String, warehouseGuid: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _cellData.value = repository.getCellByBarcode(barcode, warehouseGuid)
                //_requestError.value = null
                _dataState.value = ModelState()
            } catch (e: Exception) {
                //_requestError.value = RequestError(e.message.toString(), "getCellByBarcode")
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getCellByBarcode",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("barcode", barcode))
                )
            }
        }
    }

    fun getCellsList(warehouseGuid: String, partNameCode: String = "") {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _cellListData.value = repository.getCellsList(warehouseGuid = warehouseGuid, partNameCode = partNameCode)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getCellsList",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("partNameCode", partNameCode), AdditionalRequestOptions("warehouseGuid", warehouseGuid))
                )
            }
        }
    }

    fun getCellByGuid(cellGuid: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _virtualCellData.value = repository.getCellByGuid(cellGuid)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getCellByGuid",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("cellGuid", cellGuid))
                )
            }
        }
    }

    fun getItemByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _itemData.value = repository.getItemByBarcode(barcode)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getItemByBarcode",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("barcode", barcode))
                )
            }
        }
    }

    fun getItemByTitleOrCode(partNameCode: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _itemListData.value = repository.getItemByTitleOrCode(partNameCode)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getItemByTitleOrCode",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("partNameCode", partNameCode))
                )
            }
        }
    }

    fun getNomenclatureStocks(
        warehouseGuid: String,
        nomenclatureGuid: String,
        cellGuid: String = "",
        byCell: Boolean = false,
        gvzoGuid: String = "",
    ) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _listNomenclatureStocks.value = repository.getNomenclatureStocks(warehouseGuid = warehouseGuid, nomenclatureGuid = nomenclatureGuid, cellGuid = cellGuid, byCell = byCell, gvzoGuid = gvzoGuid)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getNomenclatureStocks",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("warehouseGuid", warehouseGuid), AdditionalRequestOptions("nomenclatureGuid", nomenclatureGuid), AdditionalRequestOptions("gvzoGuid", gvzoGuid))
                )
            }
        }
    }

    fun getWarehousesListByGuid(warehouseGuid: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _listDataWarehouses.value = repository.getWarehousesListByGuid(warehouseGuid = warehouseGuid)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getWarehousesListByGuid",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("warehouseGuid", warehouseGuid))
                )
            }
        }
    }

    fun getPhysicalPersonFormUser(userGUID: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _physicalPerson.value = repository.getPhysicalPersonFormUser(userGUID = userGUID)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getPhysicalPersonFormUser",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("userGUID", userGUID))
                )
            }
        }
    }

    fun getGvzoByTitle(partNameCode: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _listGvzo.value = repository.getGvzoByTitle(partNameCode = partNameCode)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getWarehousesListByGuid",
                    additionalRequestProperties = listOf(AdditionalRequestOptions("partNameCode", partNameCode))
                )
            }
        }
    }

}