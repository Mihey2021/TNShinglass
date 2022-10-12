package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.domain.repository.PrefsRepository
import ru.tn.shinglass.domain.repository.RetrofitRepository
import ru.tn.shinglass.dto.repository.PrefsRepositoryImpl
import ru.tn.shinglass.dto.repository.RetrofitRepositoryImpl
import ru.tn.shinglass.models.PhisicalPerson
import java.lang.Exception


class RetrofitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RetrofitRepository = RetrofitRepositoryImpl()

    private val _listDataPhisicalPersons: MutableLiveData<List<PhisicalPerson>> = MutableLiveData(listOf())
    val listDataPhisicalPersons: LiveData<List<PhisicalPerson>>
        get() = _listDataPhisicalPersons

    fun getPhisicalPersonList() {
        repository.getPhisicalPersonList(object : RetrofitRepository.Callback<List<PhisicalPerson>> {
            override fun onSuccess(receivedData: List<PhisicalPerson>) {
                _listDataPhisicalPersons.value = receivedData
            }

            override fun onError(e: Exception) {
                super.onError(e)
            }
        })
    }


}