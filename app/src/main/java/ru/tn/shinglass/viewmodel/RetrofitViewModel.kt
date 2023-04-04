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

    private val _cellData: MutableLiveData<Cell> = MutableLiveData(null)
    val cellData: LiveData<Cell>
        get() = _cellData

    private val _virtualCellData: MutableLiveData<Cell> = MutableLiveData(null)
    val virtualCellData: LiveData<Cell>
        get() = _virtualCellData

    private val _cellListData: MutableLiveData<List<Cell>> = MutableLiveData(null)
    val cellListData: LiveData<List<Cell>>
        get() = _cellListData

    private val _itemData: MutableLiveData<Nomenclature> = MutableLiveData(null)
    val itemData: LiveData<Nomenclature>
        get() = _itemData

//    private val _docCreated: MutableLiveData<CreatedDocumentDetails?> = MutableLiveData(null)
//    val docCreated: LiveData<CreatedDocumentDetails?>
//        get() = _docCreated

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

    fun getCellByBarcode(barcode: String, warehouseGuid: String) {
        try {
            viewModelScope.launch {
                _dataState.value = ModelState(loading = true)
                _cellData.value = repository.getCellByBarcode(barcode, warehouseGuid)
                //_requestError.value = null
                _dataState.value = ModelState()
            }

        } catch (e: Exception) {
            //_requestError.value = RequestError(e.message.toString(), "getCellByBarcode")
            _dataState.value = ModelState(
                error = true,
                errorMessage = e.message.toString(),
                requestName = "getCellByBarcode"
            )
        }
    }

    fun getCellsList(warehouseGuid: String) {
        try {
            viewModelScope.launch {
                _dataState.value = ModelState(loading = true)
                _cellListData.value = repository.getCellsList(warehouseGuid)
                _dataState.value = ModelState()
            }

        } catch (e: Exception) {
            _dataState.value = ModelState(
                error = true,
                errorMessage = e.message.toString(),
                requestName = "getCellsList"
            )
        }
    }

    fun getCellByGuid(cellGuid: String) {
        try {
            viewModelScope.launch {
                _dataState.value = ModelState(loading = true)
                _virtualCellData.value = repository.getCellByGuid(cellGuid)
                _dataState.value = ModelState()
            }

        } catch (e: Exception) {
            _dataState.value = ModelState(
                error = true,
                errorMessage = e.message.toString(),
                requestName = "getCellByGuid"
            )
        }
    }

//    fun createInventoryOfGoods(scanRecords: List<TableScan>){
//        repository.createInventoryOfGoods(scanRecords, object : RetrofitRepository.Callback<CreatedDocumentDetails> {
//            override fun onSuccess(receivedData: CreatedDocumentDetails) {
//                _docCreated.value = receivedData
//                _requestError.value = null
//            }
//
//            override fun onError(e: Exception) {
//                _requestError.value = RequestError(e.message.toString(), "createInventoryOfGoods")
//            }
//        })
//
//    }

    fun getItemByBarcode(barcode: String) {
        repository.getItemByBarcode(barcode, object : RetrofitRepository.Callback<Nomenclature> {
            override fun onSuccess(receivedData: Nomenclature) {
                _itemData.value = receivedData
                _requestError.value = null
            }

            override fun onError(e: Exception) {
                _requestError.value = RequestError(e.message.toString(), "getItemByBarcode")
            }
        })
    }

}