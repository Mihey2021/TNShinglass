package ru.tn.shinglass.dto.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class User1C(
    @SerializedName("User1C")
    @Expose
    private var user1C: String,

    @SerializedName("User_GUID")
    @Expose
    private var userGUID: String,

    @SerializedName("default_division_GUID")
    @Expose
    private var defaultDivisionGUID: String
) : Serializable {

    fun getUser1C(): String {
        return user1C
    }

    fun setUser1C(user1C: String) {
        this.user1C = user1C
    }

    fun getUserGUID(): String {
        return userGUID
    }

    fun setUserGUID(userGUID: String) {
        this.userGUID = userGUID
    }

    fun getDefaultDivisionGUID(): String {
        return defaultDivisionGUID
    }

    fun setDefaultDivisionGUID(defaultDivisionGUID: String) {
        this.defaultDivisionGUID = defaultDivisionGUID
    }
}

