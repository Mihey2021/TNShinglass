package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.dto.repository.TableScanRepositoryImpl
import ru.tn.shinglass.dto.repository.WarehousesRepositoryRoomImpl
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.models.Warehouse

class TableScanFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repositoryTableScan: TableScanRepository =
        TableScanRepositoryImpl(AppDb.getInstance(context = application).tableScanDao())

    private val repositoryWarehouses: WarehousesRepository =
        WarehousesRepositoryRoomImpl(AppDb.getInstance(context = application).warehousesDao())

    private val _data: MutableLiveData<List<TableScan>> = MutableLiveData()
    val data: LiveData<List<TableScan>>
        get() = _data


    fun getAllWarehousesList() = repositoryWarehouses.getAllWarehouses()

    fun saveWarehouses(warehouses: List<Warehouse>) = repositoryWarehouses.saveWarehouses(warehouses)

    fun refreshTableScan(ownerGuid: String, operationId: Long){
        _data.value = repositoryTableScan.getAllScanRecordsByOwner(ownerGuid, operationId)
    }

    fun deleteRecordById(record: TableScan){
        repositoryTableScan.deleteRecordById(record.id)
        refreshTableScan(record.OwnerGuid, record.OperationId)
    }

    fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long) {
        repositoryTableScan.deleteRecordsByOwnerAndOperationId(ownerGuid, operationId)
        refreshTableScan(ownerGuid, operationId)
    }

}