package ru.tn.shinglass.models

import ru.tn.shinglass.dto.models.DetailScanFields
import ru.tn.shinglass.dto.models.HeaderFields

enum class DocType(
    val type: OptionType,
    val subType: SubOptionType,
    val title: String,
    val description: String
) {
    //Оприходование товаров (возвраты)
    RETURNS_REGISTRATION_OF_GOODS (
        OptionType.ACCEPTANCE,
        SubOptionType.RETURNS_REGISTRATION_OF_GOODS,
        SubOptionType.RETURNS_REGISTRATION_OF_GOODS.title,
        SubOptionType.RETURNS_REGISTRATION_OF_GOODS.description
    ),

    //Требование накладная
    REQUIREMENT_INVOICE(
        OptionType.SELECTION,
        SubOptionType.INVOICE_REQUIREMENT,
        SubOptionType.INVOICE_REQUIREMENT.title,
        SubOptionType.INVOICE_REQUIREMENT.description
    ),

    //Требование накладная на основании документа Внутренний заказ (ТОиР)
    TOIR_REQUIREMENT_INVOICE(
        OptionType.SELECTION,
        SubOptionType.TOIR,
        SubOptionType.TOIR.title,
        SubOptionType.TOIR.description
    ),

    //Спецодежда/инструменты и пр. ТМЦ
    WORKWEAR_TOOLS(
        OptionType.SELECTION,
        SubOptionType.OVERALLS_TOOLS,
        SubOptionType.OVERALLS_TOOLS.title,
        SubOptionType.OVERALLS_TOOLS.description
    ),

    //Одноразовые СИЗ
    DISPOSABLE_PPE(
        OptionType.SELECTION,
        SubOptionType.DISPOSABLE_PPE,
        SubOptionType.DISPOSABLE_PPE.title,
        SubOptionType.DISPOSABLE_PPE.description
    ),

    //Стандартная приемка
    STANDARD_ACCEPTANCE(
        OptionType.ACCEPTANCE,
        SubOptionType.STANDARD_ACCEPTANCE,
        SubOptionType.STANDARD_ACCEPTANCE.title,
        SubOptionType.STANDARD_ACCEPTANCE.description
    ),

    //Инвентаризация в ячейках
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
        "Создание документа Передача материалов в эксплуатацию",
        arrayOf(
            HeaderFields.EMPLOYEE,
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
            //HeaderFields.PHYSICAL_PERSON,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
    DISPOSABLE_PPE(
        2, "Одноразовые СИЗ", "Создание документа Перемещение (виртаульная ячейка)",
        arrayOf(
            //HeaderFields.EMPLOYEE,
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
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
    RETURNS_REGISTRATION_OF_GOODS (
        5, "Возвраты", "Создание документа Оприходование товаров",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
}