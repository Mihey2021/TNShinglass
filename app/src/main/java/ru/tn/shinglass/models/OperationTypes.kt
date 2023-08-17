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
    RETURNS_REGISTRATION_OF_GOODS(
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

    //Смета (ТОиР)
    TOIR_REPAIR_ESTIMATE(
        OptionType.SELECTION,
        SubOptionType.TOIR_REPAIR_ESTIMATE,
        SubOptionType.TOIR_REPAIR_ESTIMATE.title,
        SubOptionType.TOIR_REPAIR_ESTIMATE.description
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

    //Приемка комплектов
    ACCEPTANCE_OF_KITS(
        OptionType.ACCEPTANCE,
        SubOptionType.ACCEPTANCE_OF_KITS,
        SubOptionType.ACCEPTANCE_OF_KITS.title,
        SubOptionType.ACCEPTANCE_OF_KITS.description
    ),

    //Инвентаризация в ячейках
    INVENTORY_IN_CELLS(
        OptionType.INVENTORY,
        SubOptionType.INVENTORY_IN_CELLS,
        SubOptionType.INVENTORY_IN_CELLS.title,
        SubOptionType.INVENTORY_IN_CELLS.description
    ),

    //Перемещение без указания ячейки откуда в пределах одного склада
    FREE_MOVEMENT(
        OptionType.MOVEMENTS,
        SubOptionType.FREE_MOVEMENT,
        SubOptionType.FREE_MOVEMENT.title,
        SubOptionType.FREE_MOVEMENT.description
    ),

    //Перемещение между ячейками в пределах одного склада
    BETWEEN_CELLS(
        OptionType.MOVEMENTS,
        SubOptionType.BETWEEN_CELLS,
        SubOptionType.BETWEEN_CELLS.title,
        SubOptionType.BETWEEN_CELLS.description
    ),

    //Перемещение между складами
    BETWEEN_WAREHOUSES(
        OptionType.MOVEMENTS,
        SubOptionType.BETWEEN_WAREHOUSES,
        SubOptionType.BETWEEN_WAREHOUSES.title,
        SubOptionType.BETWEEN_WAREHOUSES.description
    ),
}

enum class OptionType(val id: Int, val title: String) {
    SELECTION(0, "ОТБОР"),
    ACCEPTANCE(1, "ПРИЁМКА"),
    INVENTORY(2, "ИНВЕНТАРИЗАЦИЯ"),
    MOVEMENTS(3, "ПЕРЕМЕЩЕНИЯ"),
}

enum class SubOptionType(
    val id: Int,
    val title: String,
    val description: String,
    val headerFields: Array<HeaderFields> = arrayOf(),
    val detailScanFields: Array<DetailScanFields> = arrayOf(),
    val needLogisics: Boolean = false,
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
        1, "Внутренний заказ (ТОиР)", "Создание требования накладной на основании внутреннего заказа",
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

    TOIR_REPAIR_ESTIMATE(
        2, "Смета (ТОиР)", "Заполнение сметы на ремонт",
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
        3,
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
        4, "Одноразовые СИЗ", "Создание документа Перемещение (виртаульная ячейка)",
        arrayOf(
            //HeaderFields.EMPLOYEE,
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        ),
        needLogisics = true,
    ),
    STANDARD_ACCEPTANCE(
        5, "Стандартная приемка", "Создание документа Приходный ордер на товары",
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
            DetailScanFields.SERIAL_NUMBER,
        )
    ),
    ACCEPTANCE_OF_KITS(
        6, "Приемка комплектов", "Создание документа Перемещение в указанную ячейку склада",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        ),
        needLogisics = true,
    ),
    INVENTORY_IN_CELLS(
        7,
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
    RETURNS_REGISTRATION_OF_GOODS(
        8, "Возвраты", "Создание документа Оприходование товаров",
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

    FREE_MOVEMENT(
        9,
        "Свободное перемещение",
        "Размещение из зоны склада без ячеек в ячейку: Создание документа Перемещение товаров (в пределах одного склада)",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        ),
        needLogisics = true,
    ),

    BETWEEN_CELLS(
        10,
        "Между ячейками",
        "Создание документа Перемещение товаров (в пределах одного склада)",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.CELL_RECEIVER,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        ),
        needLogisics = true,
    ),

    BETWEEN_WAREHOUSES(
        11,
        "Между складами",
        "Создание документа Перемещение товаров (между складами)",
        arrayOf(
            HeaderFields.DIVISION,
            HeaderFields.WAREHOUSE,
            HeaderFields.WAREHOUSE_RECEIVER,
        ),
        arrayOf(
            DetailScanFields.CELL,
            DetailScanFields.ITEM,
            DetailScanFields.COUNT,
        )
    ),
}