package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.TableScanRepository
import ru.tn.shinglass.dto.repository.TableScanRepositoryImpl
import ru.tn.shinglass.models.TableScan

class TableScanFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryTableScan: TableScanRepository =
        TableScanRepositoryImpl(AppDb.getInstance(context = application).tableScanDao())

    private val _data: MutableLiveData<List<TableScan>> = MutableLiveData()
    val data: LiveData<List<TableScan>>
        get() = _data


    fun getTableScan(ownerGuid: String, operationId: Long){
        _data.value = repositoryTableScan.getAllScanRecordsByOwner(ownerGuid, operationId)
    }


}