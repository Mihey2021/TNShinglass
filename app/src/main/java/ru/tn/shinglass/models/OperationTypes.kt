package ru.tn.shinglass.models

import ru.tn.shinglass.R
import ru.tn.shinglass.dto.models.DetailScanFields
import ru.tn.shinglass.dto.models.HeaderFields

enum class DocType(
    val type: OptionType,
    val subType: SubOptionType,
    val title: String,
    val description: String
) {
    REQUIREMENT_INVOICE(
        OptionType.SELECTION,
        SubOptionType.INVOICE_REQUIREMENT,
        SubOptionType.INVOICE_REQUIREMENT.title,
        SubOptionType.INVOICE_REQUIREMENT.description
    ),
    TOIR_REQUIREMENT_INVOICE(
        OptionType.SELECTION,
        SubOptionType.TOIR,
        SubOptionType.TOIR.title,
        SubOptionType.TOIR.description
    ),
    WORKWEAR_TOOLS(
        OptionType.SELECTION,
        SubOptionType.OVERALLS_TOOLS,
        SubOptionType.OVERALLS_TOOLS.title,
        SubOptionType.OVERALLS_TOOLS.description
    ),
    DISPOSABLE_PPE(
        OptionType.SELECTION,
        SubOptionType.DISPOSABLE_PPE,
        SubOptionType.DISPOSABLE_PPE.title,
        SubOptionType.DISPOSABLE_PPE.description
    ),
    STANDARD_ACCEPTANCE(
        OptionType.ACCEPTANCE,
        SubOptionType.STANDARD_ACCEPTANCE,
        SubOptionType.STANDARD_ACCEPTANCE.title,
        SubOptionType.STANDARD_ACCEPTANCE.description
    ),
    INVENTORY_IN_CELLS(
        OptionType.INVENTORY,
        SubOptionType.INVENTORY_IN_CELLS,
        SubOptionType.INVENTORY_IN_CELLS.title,
        SubOptionType.INVENTORY_IN_CELLS.description
    ),
}

enum class OptionType(val id: Int, val title: String) {
    SELECTION(0, "ОТБОР"),
    ACCEPTANCE(1, "ПРИЁМКА"),
    INVENTORY(2, "ИНВЕНТАРИЗАЦИЯ"),
}

enum class SubOptionType(
    val id: Int,
    val title: String,
    val description: String,
    val headerFields: Array<HeaderFields> = arrayOf(),
    val detailScanFields: Array<DetailScanFields> = arrayOf(),
) {
    INVOICE_REQUIREMENT(
        0, "Требование накладная", "Создание документа Требование-накладная",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
            HeaderFields.PHYSICAL_PERSON,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
    TOIR(
        1, "ТОиР", "Создание требования накладной на основании внутреннего заказа",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
            HeaderFields.PHYSICAL_PERSON,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
    OVERALLS_TOOLS(
        2,
        "Спецодежда/Инструменты и прочие ТМЦ",
        "Создание документа Передача материалов в эксплуатацию"
    ),
    DISPOSABLE_PPE(2, "Одноразовые СИЗ", "Создание документа Перемещение (виртаульная ячейка)"),
    STANDARD_ACCEPTANCE(
        3, "Стандартная приемка", "Создание документа Приходный ордер на товары",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
            HeaderFields.COUNTERPARTY,
            HeaderFields.INCOMING_DATE,
            HeaderFields.INCOMING_NUMBER,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
    INVENTORY_IN_CELLS(
        4,
        "Инвентаризация в ячейках",
        "Создание документа Инвентаризация товаров на складе",
        arrayOf(
            HeaderFields.WAREHOUSE,
            HeaderFields.PHYSICAL_PERSON,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
}