package ru.tn.shinglass.dto.models

import com.google.gson.annotations.Expose

class CreatedDocumentDetails (
    @Expose
    val docTitle: String = "",
    @Expose
    val docNumber: Number,
    @Expose
    val details: String = ""

)