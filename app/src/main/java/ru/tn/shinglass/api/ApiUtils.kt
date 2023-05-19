package ru.tn.shinglass.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ru.tn.shinglass.data.api.ApiService


private const val ERR_TAG = "Ошибка при создании Retrofit!"

object ApiUtils  {
    private var retrofitService: ApiService? = null

    fun getApiService(basicPreferences: SharedPreferences? = null, recreate: Boolean = false): ApiService? {
        return try {
            retrofitService = if(recreate) null else retrofitService
            if(retrofitService == null)
                RetrofitClient.getClient(basicPreferences, recreate)?.create(ApiService::class.java)
            else
                retrofitService
        } catch (e: Exception) {
            Log.e(ERR_TAG, e.toString())
            null
        }
    }
}

