package ru.tn.shinglass.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PhysicalPerson(
    @SerializedName("fio")
    val physicalPersonFio: String,
    @SerializedName("guid")
    val physicalPersonGuid: String,
) : Serializable
