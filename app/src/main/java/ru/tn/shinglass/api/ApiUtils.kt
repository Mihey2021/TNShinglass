package ru.tn.shinglass.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ru.tn.shinglass.data.api.ApiService


private const val ERR_TAG = "Ошибка при создании Retrofit!"

object ApiUtils  {

    fun getApiService(basicPreferences: SharedPreferences? = null): ApiService? {
        return try {
            RetrofitClient.getClient(basicPreferences)?.create(ApiService::class.java)
        } catch (e: Exception) {
            Log.e(ERR_TAG, e.toString())
            null
        }
    }
}

