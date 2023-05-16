package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.tn.shinglass.domain.repository.DocumentSelectRepository
import ru.tn.shinglass.dto.repository.DocumentSelectRepositoryImpl
import ru.tn.shinglass.models.ExternalDocument
import ru.tn.shinglass.models.ModelState
import java.lang.Exception

class DocumentSelectFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentSelectRepository = DocumentSelectRepositoryImpl()

    private val _dataState = MutableLiveData<ModelState>()
    val dataState: LiveData<ModelState>
        get() = _dataState

    private val _externalDocumentOrderList = MutableLiveData<List<ExternalDocument>>()
    val externalDocumentList: LiveData<List<ExternalDocument>>
        get() = _externalDocumentOrderList

    fun getInternalOrderList() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _externalDocumentOrderList.value = repository.getInternalOrderList()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getInternalOrderList"
                )
            }
        }
    }

    fun getRepairEstimate() {
        viewModelScope.launch {
            try {
                _dataState.value = ModelState(loading = true)
                _externalDocumentOrderList.value = repository.getRepairEstimate()
                _dataState.value = ModelState()
            } catch (e: Exception) {
                _dataState.value = ModelState(
                    error = true,
                    errorMessage = e.message.toString(),
                    requestName = "getRepairEstimate"
                )
            }
        }
    }
}