package ru.tn.shinglass.dto.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.tn.shinglass.domain.repository.PrefsRepository
import ru.tn.shinglass.dto.models.User1C
import java.time.LocalDateTime
import java.time.ZoneOffset

class PrefsRepositoryImpl(context: Context) : PrefsRepository {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("shinglassPrefs", Context.MODE_PRIVATE)
    private val basicPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val type = object : TypeToken<User1C>() {}.type
    private val key = "userPrefs"
    private val _userData = MutableLiveData<User1C?>()
//    val userData: LiveData<User1C?>
//        get() = _userData
//
//    init {
//        prefs.getString(key, null)?.let {
//            _userData.value = gson.fromJson(it, type)
//        }
//    }

    override fun getBasicPreferences(): SharedPreferences = basicPreferences

    override fun setPreferenceLong(key: String, value: Long) {
        val editor = getEditor()
        editor.putLong(key, value)
        editor.apply()
    }

    override fun removePreference(key: String) {
        val editor = getEditor()
        editor.remove(key)
        editor.apply()
    }

    override fun <T> getPreferenceByKey(key: String, default: T, basicPrefs: Boolean): T? {
       if (key.isBlank()) return null

        return when (default) {
            is String -> {
                return (if (basicPrefs) basicPreferences.getString(key, default)  else prefs.getString(key, default)) as T
            }
            is Long -> {
                return (if (basicPrefs) basicPreferences.getLong(key, default) else prefs.getLong(key, default)) as T
            }
            is Boolean -> {
                return (if (basicPrefs) basicPreferences.getBoolean(key, default) else prefs.getBoolean(key, default)) as T
            }
            is Float -> {
                return (if (basicPrefs) basicPreferences.getFloat(key, default) else prefs.getFloat(key, default)) as T
            }
            is Int -> {
                return (if (basicPrefs) basicPreferences.getInt(key, default) else prefs.getInt(key, default)) as T
            }
            else -> {
                null
            }
        }
    }

    private fun getEditor(): SharedPreferences.Editor = prefs.edit()


}