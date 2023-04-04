package ru.tn.shinglass.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.dto.models.User1C

class AppAuth {

    private var authData: User1C = User1C("", "", "")
    private val _authStateFlow: MutableLiveData<AuthState> = MutableLiveData(AuthState(User1C("", "", "")))

    val authStateFlow: LiveData<AuthState>
        get() = _authStateFlow

    fun setAuthData(user1C: User1C) {
        this.authData = user1C
        _authStateFlow.value = AuthState(user1C)
    }

    fun clearAuthData() {
        authData = User1C("","","")
        _authStateFlow.value = AuthState(authData)
    }

    fun getAuthData() = this.authData


    companion object {
        @Volatile
        private var instance: AppAuth? = null

        fun getInstance(): AppAuth = synchronized(this) {
            instance ?: throw IllegalStateException(
                "AppAuth is not initialized, you must call AppAuth.initApp() first."
            )
        }

        fun initApp(): AppAuth = instance ?: synchronized(this) {
            instance ?: buildAuth().also { instance = it }
        }

        private fun buildAuth(): AppAuth = AppAuth()
    }
}

data class AuthState(
    val user1C: User1C,
)