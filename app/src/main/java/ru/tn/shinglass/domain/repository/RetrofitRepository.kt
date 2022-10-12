package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.dto.models.RequestLogin
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.PhisicalPerson
import java.lang.Exception

interface RetrofitRepository {

    fun authorization(user: RequestLogin, callback:Callback<User1C>)
    fun getPhysicalPersonList(callback: Callback<List<PhisicalPerson>>)

    interface Callback<T> {
        fun onSuccess(receivedData: T) {}
        fun onError(e: Exception) {}
    }
}