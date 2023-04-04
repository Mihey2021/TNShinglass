package ru.tn.shinglass.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Employee(
    @SerializedName("fio")
    val employeeFio: String = "",
    @SerializedName("guid")
    val employeeGuid: String = "",
) : Serializable
