package ru.tn.shinglass.domain.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import ru.tn.shinglass.dto.models.User1C

interface PrefsRepository {
//    fun setAuthData(user1C: User1C?)
//    fun getAuthData(): User1C?
    fun getBasicPreferences(): SharedPreferences
    fun <T> getPreferenceByKey(key: String, default: T, basicPrefs: Boolean): T?
    fun setPreferenceLong(key: String, value: Long)
    fun removePreference(key: String)
}