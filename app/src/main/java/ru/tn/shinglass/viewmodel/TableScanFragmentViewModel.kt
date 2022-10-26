package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.PhysicalPersonRepository
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.dto.repository.PhysicalPersonRepositoryImpl
import ru.tn.shinglass.dto.repository.TableScanRepositoryImpl
import ru.tn.shinglass.dto.repository.WarehousesRepositoryRoomImpl
import ru.tn.shinglass.models.ModelState
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.models.Warehouse
import java.lang.Exception

class TableScanFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repositoryTableScan: TableScanRepository =
        TableScanRepositoryImpl(AppDb.getInstance(context = application).tableScanDao())

    private val repositoryWarehouses: WarehousesRepository =
        WarehousesRepositoryRoomImpl(AppDb.getInstance(context = application).warehousesDao())
    val warehousesList: LiveData<List<Warehouse>> = repositoryWarehouses.warehousesList

    private val repositoryPhysicalPerson: PhysicalPersonRepository =
        PhysicalPersonRepositoryImpl(AppDb.getInstance(context = application).physicalPersonDao())

    val physicalPersons: LiveData<List<PhysicalPerson>> = repositoryPhysicalPerson.physicalPersons

    private val _data: MutableLiveData<List<TableScan>> = MutableLiveData()
    val data: LiveData<List<TableScan>>
        get() = _data

    private val _dataState = MutableLiveData<ModelState>()
    val dataState: LiveData<ModelState>
        get() = _dataState


    fun getAllWarehousesList() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                repositoryWarehouses.getAllWarehousesList()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(error = true, errorMessage = e.message.toString(), requestName = "getAllWarehousesList")
            }
        }
    }

    fun saveWarehouses(warehouses: List<Warehouse>) =
        repositoryWarehouses.saveWarehouses(warehouses)

    fun refreshTableScan(ownerGuid: String, operationId: Long) {
        _data.value = repositoryTableScan.getAllScanRecordsByOwner(ownerGuid, operationId)
    }

    fun deleteRecordById(record: TableScan) {
        repositoryTableScan.deleteRecordById(record.id)
        refreshTableScan(record.OwnerGuid, record.OperationId)
    }

    fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long) {
        repositoryTableScan.deleteRecordsByOwnerAndOperationId(ownerGuid, operationId)
        refreshTableScan(ownerGuid, operationId)
    }

    fun savePhysicalPerson(physicalPersons: List<PhysicalPerson>) =
        repositoryPhysicalPerson.savePhysicalPerson(physicalPersons)

    fun getAllPhysicalPerson() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                repositoryPhysicalPerson.getPhysicalPersonList()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(error = true, errorMessage = e.message.toString(), requestName = "getPhysicalPersonList")
            }

        }
    }

    fun getPhysicalPersonByGuid(guid: String) =
        repositoryPhysicalPerson.getPhysicalPersonByGuid(guid)

}