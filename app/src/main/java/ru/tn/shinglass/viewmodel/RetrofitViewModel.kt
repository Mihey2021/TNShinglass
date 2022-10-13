package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.domain.repository.PrefsRepository
import ru.tn.shinglass.domain.repository.RetrofitRepository
import ru.tn.shinglass.dto.repository.PrefsRepositoryImpl
import ru.tn.shinglass.dto.repository.RetrofitRepositoryImpl
import ru.tn.shinglass.models.Cells
import ru.tn.shinglass.models.PhisicalPerson
import ru.tn.shinglass.models.RequestError
import ru.tn.shinglass.models.Warehouse
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
}