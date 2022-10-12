package ru.tn.shinglass.dto.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.tn.shinglass.domain.repository.PrefsRepository
import ru.tn.shinglass.dto.models.User1C

class PrefsRepositoryImpl(context: Context) : PrefsRepository {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("shinglassPrefs", Context.MODE_PRIVATE)
    private val basicPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val type = object : TypeToken<User1C>() {}.type
    private val key = "userPrefs"
    private val _userData = MutableLiveData<User1C?>()
    val userData: LiveData<User1C?>
        get() = _userData

    init {
        prefs.getString(key, null)?.let {
            _userData.value = gson.fromJson(it, type)
        }
    }

    override fun getBasicPreferences(): SharedPreferences = basicPreferences

    override fun getPreferenceByKey(key: String): String? {
        if (key.isBlank()) return null
        return basicPreferences.getString(key, "")
    }


}