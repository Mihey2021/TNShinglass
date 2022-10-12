package ru.tn.shinglass.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.PrefsRepository
import ru.tn.shinglass.domain.repository.RetrofitRepository
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.dto.repository.PrefsRepositoryImpl
import ru.tn.shinglass.dto.repository.RetrofitRepositoryImpl
import ru.tn.shinglass.dto.repository.WarehousesRepositoryRoomImpl
import ru.tn.shinglass.models.PhisicalPerson

class DetailScanViewModel(application: Application): AndroidViewModel(application) {

    private val repositoryPrefs: PrefsRepository =
        PrefsRepositoryImpl(application)

    private val repositoryRetrofit: RetrofitRepository = RetrofitRepositoryImpl()

    private val repositoryWarehouses: WarehousesRepository =
        WarehousesRepositoryRoomImpl(AppDb.getInstance(context = application).warehousesDao())

    fun getPreferenceByKey(key: String) = repositoryPrefs.getPreferenceByKey(key)

    fun getWarehouseByGuid(guid: String) = repositoryWarehouses.getWarehouseByGuid(guid)

    fun getAllWarehousesList() = repositoryWarehouses.getAllWarehouses()

}