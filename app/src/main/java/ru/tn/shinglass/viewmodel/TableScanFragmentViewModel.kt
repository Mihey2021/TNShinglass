package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.DivisionRepository
import ru.tn.shinglass.domain.repository.PhysicalPersonRepository
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.domain.repository.WarehousesRepository
import ru.tn.shinglass.dto.models.CreatedDocumentDetails
import ru.tn.shinglass.dto.repository.DivisionRepositoryImpl
import ru.tn.shinglass.dto.repository.PhysicalPersonRepositoryImpl
import ru.tn.shinglass.dto.repository.TableScanRepositoryImpl
import ru.tn.shinglass.dto.repository.WarehousesRepositoryRoomImpl
import ru.tn.shinglass.models.*
import java.lang.Exception

class TableScanFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repositoryTableScan: TableScanRepository =
        TableScanRepositoryImpl(AppDb.getInstance(context = application).tableScanDao())

    private val repositoryDivisions: DivisionRepository =
        DivisionRepositoryImpl(AppDb.getInstance(context = application).divisionsDao())
    val divisionsList: LiveData<List<Division>> = repositoryDivisions.divisionsList

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

    private val _docCreated: MutableLiveData<CreatedDocumentDetails?> = MutableLiveData(null)
    val docCreated: LiveData<CreatedDocumentDetails?>
        get() = _docCreated

    private val _counterpartiesList: MutableLiveData<List<Counterparty>> = MutableLiveData(listOf())
    val counterpartiesList: LiveData<List<Counterparty>>
        get() = _counterpartiesList


    fun getAllDivisions() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                repositoryDivisions.getAllDivisions()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getAllDivisions"
                )
            }
        }
    }

    fun getInternalOrderList() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                repositoryTableScan.getInternalOrderList()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getAllDivisions"
                )
            }
        }
    }

    fun saveRecord(record: TableScan, forceOverwrite: Boolean) =
        repositoryTableScan.saveScanRecord(record, forceOverwrite)

    fun getAllWarehousesList() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                repositoryWarehouses.getAllWarehousesList()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getAllWarehousesList"
                )
            }
        }
    }

    fun createDocumentIn1C(scanRecords: List<TableScan>, docType: DocType) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _docCreated.value = repositoryTableScan.createDocumentIn1C(scanRecords, docType)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "createDocumentIn1C"
                )
            }
        }
    }

    fun resetTheDocumentCreatedFlag() {
        _docCreated.value = null
    }

    fun saveWarehouses(warehouses: List<Warehouse>) =
        repositoryWarehouses.saveWarehouses(warehouses)

    fun refreshTableScan(ownerGuid: String, operationId: Long) {
        _data.value = repositoryTableScan.getAllScanRecordsByOwner(ownerGuid, operationId)
    }

    fun getAllScanRecordsByOwner(ownerGuid: String, operationId: Long) =
        repositoryTableScan.getAllScanRecordsByOwner(ownerGuid, operationId)

    fun deleteRecordById(record: TableScan) {
        repositoryTableScan.deleteRecordById(record.id)
        refreshTableScan(record.OwnerGuid, record.OperationId)
    }

    fun deleteRecordsByOwnerAndOperationId(ownerGuid: String, operationId: Long) {
        repositoryTableScan.deleteRecordsByOwnerAndOperationId(ownerGuid, operationId)
        refreshTableScan(ownerGuid, operationId)
    }

    fun getScanRecordById(id: Long) = repositoryTableScan.getScanRecordById(id)

    fun savePhysicalPerson(physicalPersons: List<PhysicalPerson>) =
        repositoryPhysicalPerson.savePhysicalPerson(physicalPersons)

    fun updateRecordUpload(ownerGuid: String, operationId: Long) =
        repositoryTableScan.updateRecordUpload(ownerGuid, operationId)

    fun getAllPhysicalPerson() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                repositoryPhysicalPerson.getPhysicalPersonList()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getAllPhysicalPerson"
                )
            }
        }
    }

    fun getCounterpartiesList(title: String) {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _counterpartiesList.value = repositoryTableScan.getCounterpartiesList(title)
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getCounterpartiesList"
                )
            }
        }
    }

    fun getPhysicalPersonByGuid(guid: String) =
        repositoryPhysicalPerson.getPhysicalPersonByGuid(guid)

    fun getDivisionByGuid(guid: String) =
        repositoryDivisions.getDivisionByGuid(guid)

    fun getExistingRecordCountSum(record: TableScan?) = repositoryTableScan.getExistingRecordCountSum(record)

    fun getTotalCount(
        ownerGuid: String,
        operationId: Long,
        itemGUID: String,
        itemMeasureOfUnitGUID: String
    ) = repositoryTableScan.getTotalCount(ownerGuid, operationId, itemGUID, itemMeasureOfUnitGUID)
}