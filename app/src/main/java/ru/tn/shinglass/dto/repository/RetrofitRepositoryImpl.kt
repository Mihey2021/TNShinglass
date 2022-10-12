package ru.tn.shinglass.dto.repository

import android.content.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.domain.repository.RetrofitRepository
import ru.tn.shinglass.dto.models.RequestLogin
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.PhisicalPerson

class RetrofitRepositoryImpl : RetrofitRepository {

    private val apiService = ApiUtils.getApiService()
    override fun authorization(user: RequestLogin, callback: RetrofitRepository.Callback<User1C>) {
        TODO("Not yet implemented")
    }

    override fun getPhysicalPersonList(callback: RetrofitRepository.Callback<List<PhisicalPerson>>) {
        apiService?.getPhisicalPersonList()?.enqueue(getCallbackHandler(callback))
    }

    private fun <T> getCallbackHandler(callback: RetrofitRepository.Callback<T>): Callback<T> {
        return object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException("Operation execution error. The server returned the code: ${response.code()}"))
                    return
                }

                val body = response.body() ?: run {
                    callback.onError(RuntimeException("Operation execution error: The server did not return a response"))
                    return
                }

                callback.onSuccess(body)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onError(RuntimeException("Operation execution error:\n$t"))
            }
        }
    }


}