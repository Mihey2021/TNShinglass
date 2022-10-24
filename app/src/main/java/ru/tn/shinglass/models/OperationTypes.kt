package ru.tn.shinglass.models

enum class DocType(val type: OptionType, val title: String, val description: String) {
    REQUIREMENT_INVOICE(OptionType.ACCEPTANCE,SubOptionType.INVOICE_REQUIREMENT.title, SubOptionType.INVOICE_REQUIREMENT.description),
    WORKWEAR_TOOLS(OptionType.ACCEPTANCE,SubOptionType.OVERALLS_TOOLS.title, SubOptionType.OVERALLS_TOOLS.description),
    DISPOSABLE_PPE(OptionType.ACCEPTANCE,SubOptionType.DISPOSABLE_PPE.title, SubOptionType.DISPOSABLE_PPE.description),
    STANDARD_ACCEPTANCE(OptionType.SELECTION, SubOptionType.STANDARD_ACCEPTANCE.title, SubOptionType.STANDARD_ACCEPTANCE.description),
    INVENTORY_IN_CELLS(OptionType.INVENTORY,SubOptionType.INVENTORY_IN_CELLS.title, SubOptionType.INVENTORY_IN_CELLS.description),
}

enum class OptionType(val id: Int, val title: String) {
    SELECTION(0, "ОТБОР"),
    ACCEPTANCE(1, "ПРИЁМКА"),
    INVENTORY(2, "ИНВЕНТАРИЗАЦИЯ"),
}

enum class SubOptionType(val id: Int, val title: String, val description: String) {
    INVOICE_REQUIREMENT(0, "Требование накладная", "Создание документа Требование-накладная"),
    OVERALLS_TOOLS(1, "Спецодежда/Инструменты и прочие ТМЦ", "Создание документа Передача материалов в эксплуатацию"),
    DISPOSABLE_PPE(2, "Одноразовые СИЗ", "Создание документа Перемещение (виртаульная ячейка)"),
    STANDARD_ACCEPTANCE(3, "Стандартная приемка", "Создание документа Приходный ордер на товары"),
    INVENTORY_IN_CELLS(4, "Инвентаризация в ячейках", "Создание документа Инвентаризация товаров на складе"),
}