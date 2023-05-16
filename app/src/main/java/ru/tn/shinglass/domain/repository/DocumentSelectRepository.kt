package ru.tn.shinglass.domain.repository

import ru.tn.shinglass.models.ExternalDocument

interface DocumentSelectRepository {

    suspend fun getInternalOrderList(): List<ExternalDocument>
    suspend fun getRepairEstimate(): List<ExternalDocument>

}