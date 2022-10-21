package ru.tn.shinglass.models

enum class DocType(val type: OptionType, val title: String, val description: String) {
    REQUIREMENT_INVOICE(OptionType.ACCEPTANCE,"Требование накладная", "Создание документа Требование-накладная"),
    WORKWEAR_TOOLS(OptionType.ACCEPTANCE,"Спецодежда/Инструменты и прочие ТМЦ", "Создание документа Передача материалов в эксплуатацию"),
    DISPOSABLE_PPE(OptionType.ACCEPTANCE,"Одноразовые СИЗ", "Создание документа Перемещение (виртаульная ячейка)"),
    STANDARD_ACCEPTANCE(OptionType.SELECTION, "Стандартная приемка", "Создание документа Приходный ордер на товары"),
    INVENTORY_IN_CELLS(OptionType.INVENTORY,"Инвентаризация в ячейках", "Создание документа Инвентаризация товаров на складе"),
}

enum class OptionType(val id: Long, val title: String) {
    SELECTION(0L, "ОТБОР"),
    ACCEPTANCE(1L, "ПРИЁМКА"),
    INVENTORY(2L, "ИНВЕНТАРИЗАЦИЯ"),
}