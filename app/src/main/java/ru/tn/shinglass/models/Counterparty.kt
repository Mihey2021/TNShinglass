package ru.tn.shinglass.models

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class Counterparty(
    @SerializedName("title")
    @ColumnInfo(name = "counterpartyTitle")
    val title: String = "",
    @SerializedName("guid")
    @ColumnInfo(name = "counterpartyGuid")
    val guid: String = "",
    @SerializedName("inn")
    @ColumnInfo(name = "counterpartyInn")
    val inn: Long = 0L,
    @SerializedName("kpp")
    @ColumnInfo(name = "counterpartyKpp")
    val kpp: Long = 0L,
) {}

