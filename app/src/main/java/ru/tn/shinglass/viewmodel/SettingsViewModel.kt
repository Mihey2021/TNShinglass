package ru.tn.shinglass.viewmodel

import android.app.Application
import android.content.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
//import ru.tn.shinglass.activity.apiService
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.PrefsRepository
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.dto.repository.PrefsRepositoryImpl
import ru.tn.shinglass.dto.repository.WarehousesRepositoryRoomImpl
import ru.tn.shinglass.models.Warehouse

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repositoryPrefs: PrefsRepository =
        PrefsRepositoryImpl(application)

    private val repositoryWarehouses: WarehousesRepository =
        WarehousesRepositoryRoomImpl(AppDb.getInstance(context = application).warehousesDao())

    private val _basicPrefs: MutableLiveData<SharedPreferences?> = MutableLiveData(null)
    val basicPrefs: LiveData<SharedPreferences?>
        get() = _basicPrefs

//    private val _warehousesList = MutableLiveData<ArrayList<Warehouse>>()
//    val warehousesList: LiveData<ArrayList<Warehouse>>
//        get() = _warehousesList
//
//    private val _divisionList = MutableLiveData<ArrayList<Division>>()
//    val divisionList: LiveData<ArrayList<Division>>
//        get() = _divisionList

    fun updateBasicPreferences() {
        _basicPrefs.value = repositoryPrefs.getBasicPreferences()
    }

    fun getBasicPreferences() = repositoryPrefs.getBasicPreferences()

    fun getPreferenceByKey(key: String) = repositoryPrefs.getPreferenceByKey(key)

    fun getAllWarehouses() = repositoryWarehouses.getAllWarehouses()
//    fun getAllWarehouses(): List<Warehouses> {
//        if (getWarehousesCountRecords() != 0L) {
//            return repositoryWarehouses.getAllWarehouses()
//        } else {
//            val apiService = ApiUtils.getApiService(getBasicPreferences()) ?: return listOf<Warehouses>()
//
//            return apiService.getWarehousesList()
//        }
//    }

    fun save(warehouses: List<Warehouse>) = repositoryWarehouses.saveWarehouses(warehouses)
    fun getWarehouseByGuid(guid: String) = repositoryWarehouses.getWarehouseByGuid(guid)
    fun getAllWarehousesByDivision(divisionId: Long) = repositoryWarehouses.getAllWarehousesByDivision(divisionId)
    fun getWarehousesCountRecords() = repositoryWarehouses.getWarehousesCountRecords()
    fun getWarehousesCountRecordsByDivision(divisionId: Long) = repositoryWarehouses.getWarehousesCountRecordsByDivision(divisionId)

//    fun updateWarehousesDataList(apiService: ApiService?) {
//        val warehousesArrayListData = ArrayList<Warehouse>()
//        val dataListFromDb = getAllWarehouses()
//
//        if (dataListFromDb.isNotEmpty()) {
//            dataListFromDb.forEach { warehousesArrayListData.add(it) }
//        } else {
//            apiService?.getAllWarehousesList()?.enqueue(object : Callback<List<Warehouse>> {
//                override fun onResponse(
//                    call: Call<List<Warehouse>>,
//                    response: Response<List<Warehouse>>
//                ) {
//                    if (!response.isSuccessful) {
//                        //TODO: Обработка не 2хх кода ответа
//                        return
//                    }
//
//                    val warehousesList = response.body()
//                    if (warehousesList == null) {
//                        //TODO: Обработка пустого ответа
//                        return
//                    }
//                    //Сохраним полученные склады в базу данных
//                    save(warehousesList)
//                    //Прочитаем из БД
//                    getAllWarehouses().forEach { warehousesArrayListData.add(it) }
//                    //Установим список
//                    //warehouseListPreference.setDataListArray(arrayListWarehouse)
//                        _warehousesList.value = warehousesArrayListData as ArrayList<Warehouse> /* = java.util.ArrayList<ru.tn.shinglass.models.Warehouse> */
//                    return
//                }
//
//                override fun onFailure(call: Call<List<Warehouse>>, t: Throwable) {
//                    //TODO:"Not yet implemented"
//                    return
//                }
//
//            })
//        }
//        //warehouseListPreference.setDataListArray(arrayListWarehouse)
//            _warehousesList.value = warehousesArrayListData
//        //return arrayListWarehouse
//    }

}