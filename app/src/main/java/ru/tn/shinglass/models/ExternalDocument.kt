package ru.tn.shinglass.models

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExternalDocument (
    @SerializedName("title")
    val externalOrderDocumentTitle: String,
    @SerializedName("guid")
    val externalOrderDocumentGuid: String,
    @SerializedName("doc_date")
    val externalOrderDate: Long,
    @SerializedName("doc_number")
    val externalOrderNumber: String,
    @SerializedName("division")
    val externalOrderDivision: Division? = null,
    @SerializedName("items")
    val externalDocumentItems: List<ExternalDocumentItems>? = null,
): Serializable
