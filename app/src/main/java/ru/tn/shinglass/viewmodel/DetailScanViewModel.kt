package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.PrefsRepository
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.dto.repository.PrefsRepositoryImpl
import ru.tn.shinglass.dto.repository.TableScanRepositoryImpl
import ru.tn.shinglass.dto.repository.WarehousesRepositoryRoomImpl
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.models.Warehouse

class DetailScanViewModel(application: Application): AndroidViewModel(application) {

    private val repositoryPrefs: PrefsRepository =
        PrefsRepositoryImpl(application)

    private val repositoryTableScan: TableScanRepository =
        TableScanRepositoryImpl(AppDb.getInstance(context = application).tableScanDao())

    private val repositoryWarehouses: WarehousesRepository =
        WarehousesRepositoryRoomImpl(AppDb.getInstance(context = application).warehousesDao())

    fun <T> getPreferenceByKey(key: String, default: T, basicPrefs: Boolean = true) = repositoryPrefs.getPreferenceByKey(key, default, basicPrefs)

    fun getWarehouseByGuid(guid: String) = repositoryWarehouses.getWarehouseByGuid(guid)

    fun saveWarehouses(warehouses: List<Warehouse>) = repositoryWarehouses.saveWarehouses(warehouses)

    //fun getAllWarehousesList() = repositoryWarehouses.getAllWarehouses()

    fun saveScanRecord(record: TableScan, forceOverwrite: Boolean) = repositoryTableScan.saveScanRecord(record, forceOverwrite)

}