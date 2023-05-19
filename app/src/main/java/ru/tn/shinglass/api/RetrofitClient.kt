package ru.tn.shinglass.api

import android.content.SharedPreferences
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.tn.shinglass.api.httpsettings.createSocketFactory
import ru.tn.shinglass.dto.models.Settings

object RetrofitClient {
    private var retrofit: Retrofit? = null

    fun getClient(
        basicPreferences: SharedPreferences? = null,
        recreate: Boolean = false
    ): Retrofit? {

        retrofit = if (recreate) null else retrofit

        if (retrofit == null && basicPreferences != null) {
            initializeBasicPrefs(basicPreferences)
            var mUrl = basicPreferences.getString(
                Settings.URL_SETTINGS.value,
                "https://10.16.62.7/zil-test/hs/wos/"
            ).toString()
            if (!mUrl[mUrl.length - 1].equals('/', true)) mUrl += "/"
            val serviceUserName =
                basicPreferences.getString(Settings.USER_NAME_SETTINGS.value, "obmen").toString()
            val serviceUserPassword =
                basicPreferences.getString(Settings.USER_PASSWORD_SETTINGS.value, "123").toString()

            val okHttpClient = createSocketFactory(serviceUserName, serviceUserPassword)

            retrofit = Retrofit.Builder()
                .baseUrl(mUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        }
        return retrofit
    }

    private fun initializeBasicPrefs(sharedPrefs: SharedPreferences) {
        var editingSharedPrefs = false
        with(sharedPrefs) {
            val editor = edit()
            if (!contains(Settings.URL_SETTINGS.value)) {
                editingSharedPrefs = true
                editor.putString(
                    Settings.URL_SETTINGS.value,
                    "https://10.16.62.7/zil-test/hs/wos/"
                )
            }
            if (!contains(Settings.USER_NAME_SETTINGS.value)) {
                editingSharedPrefs = true
                editor.putString(Settings.USER_NAME_SETTINGS.value, "obmen")
            }
            if (!contains(Settings.USER_PASSWORD_SETTINGS.value)) {
                editingSharedPrefs = true
                editor.putString(Settings.USER_PASSWORD_SETTINGS.value, "123")
            }
            if (editingSharedPrefs) editor.apply()
        }
    }
}