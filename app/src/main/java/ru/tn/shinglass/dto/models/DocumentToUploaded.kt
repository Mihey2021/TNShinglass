package ru.tn.shinglass.dto.models

import ru.tn.shinglass.models.DocType
import ru.tn.shinglass.models.TableScan

class DocumentToUploaded (
    val docType: DocType,
    val records: List<TableScan>,
)