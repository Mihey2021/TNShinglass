package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.domain.repository.RetrofitRepository
import ru.tn.shinglass.dto.models.CreatedDocumentDetails
import ru.tn.shinglass.dto.repository.RetrofitRepositoryImpl
import ru.tn.shinglass.entity.Nomenclature
import ru.tn.shinglass.models.*
import java.lang.Exception


class RetrofitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RetrofitRepository = RetrofitRepositoryImpl()

    private val _listDataPhisicalPersons: MutableLiveData<List<PhisicalPerson>> =
        MutableLiveData(listOf())
    val listDataPhisicalPersons: LiveData<List<PhisicalPerson>>
        get() = _listDataPhisicalPersons

    private val _listDataWarehouses: MutableLiveData<List<Warehouse>> = MutableLiveData(listOf())
    val listDataWarehouses: LiveData<List<Warehouse>>
        get() = _listDataWarehouses

    private val _cellData: MutableLiveData<Cells> = MutableLiveData(null)
    val cellData: LiveData<Cells>
        get() = _cellData

    private val _itemData: MutableLiveData<Nomenclature> = MutableLiveData(null)
    val itemData: LiveData<Nomenclature>
        get() = _itemData

    private val _docCreated: MutableLiveData<CreatedDocumentDetails?> = MutableLiveData(null)
    val docCreated: LiveData<CreatedDocumentDetails?>
        get() = _docCreated

    private val _requestError: MutableLiveData<RequestError?> = MutableLiveData(null)
    val requestError: LiveData<RequestError?>
        get() = _requestError

    fun getPhysicalPersonList() {
        repository.getPhysicalPersonList(object :
            RetrofitRepository.Callback<List<PhisicalPerson>> {
            override fun onSuccess(receivedData: List<PhisicalPerson>) {
                _listDataPhisicalPersons.value = receivedData
                _requestError.value = null
            }

            override fun onError(e: Exception) {
                _requestError.value = RequestError(e.message.toString(), "getPhisicalPersonList")
                //super.onError(e)
            }
        })
    }

    fun getAllWarehouses() {
        repository.getAllWarehousesList(object : RetrofitRepository.Callback<List<Warehouse>> {
            override fun onSuccess(receivedData: List<Warehouse>) {
                _listDataWarehouses.value = receivedData
                _requestError.value = null
            }

            override fun onError(e: Exception) {
                _requestError.value = RequestError(e.message.toString(), "getAllWarehousesList")
                //super.onError(e)
            }
        })
    }

    fun getCellByBarcode(barcode: String) {
        repository.getCellByBarcode(barcode, object : RetrofitRepository.Callback<Cells> {
            override fun onSuccess(receivedData: Cells) {
                _cellData.value = receivedData
                _requestError.value = null
            }

            override fun onError(e: Exception) {
                _requestError.value = RequestError(e.message.toString(), "getCellByBarcode")
                //super.onError(e)
            }
        })
    }

    fun createInventoryOfGoods(scanRecords: List<TableScan>){
        repository.createInventoryOfGoods(scanRecords, object : RetrofitRepository.Callback<CreatedDocumentDetails> {
            override fun onSuccess(receivedData: CreatedDocumentDetails) {
                _docCreated.value = receivedData
                _requestError.value = null
            }

            override fun onError(e: Exception) {
                _requestError.value = RequestError(e.message.toString(), "createInventoryOfGoods")
            }
        })

    }

    fun resetTheDocumentCreatedFlag() {
        _docCreated.value = null
    }

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