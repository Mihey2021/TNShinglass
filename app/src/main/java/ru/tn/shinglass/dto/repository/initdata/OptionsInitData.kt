package ru.tn.shinglass.dto.repository.initdata

import ru.tn.shinglass.models.Option

class OptionsInitData {
    private val optionsList = listOf(
        Option(type = "ОТБОР", title = "ОТБОР"),
        Option(type = "ПРИЕМКА", title = "ПРИЕМКА"),
        Option(type = "ИНВЕНТАРИЗАЦИЯ", title = "ИНВЕНТАРИЗАЦИЯ"),
        Option(type = "ОТБОР", subOptionId = 1, title = "Требование накладная",  description = "Создание документа Требование-накладная"),
        Option(type = "ОТБОР", subOptionId = 1, title = "Спецодежда/Инструменты и прочие ТМЦ",  description = "Создание документа Передача материалов в эксплуатацию"),
        Option(type = "ОТБОР", subOptionId = 1, title = "Одноразовые СИЗ", description = "Создание документа Перемещение (виртаульная ячейка)"),
        Option(type = "ПРИЕМКА", subOptionId = 2, title = "Стандартная приемка", description = "Создание документа Приходный ордер на товары"),
        Option(type = "ИНВЕНТАРИЗАЦИЯ", subOptionId = 3, title = "Инвентаризация в ячейках", description = "Создание документа Инвентаризация товаров на складе"),
    )

    fun getOptionsInitData() = optionsList
}