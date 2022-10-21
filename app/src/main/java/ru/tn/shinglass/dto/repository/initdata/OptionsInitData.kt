package ru.tn.shinglass.dto.repository.initdata

import ru.tn.shinglass.models.DocType
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.OptionType

class OptionsInitData {
    private val optionsList = listOf(
        Option(type = OptionType.SELECTION.title, title = OptionType.SELECTION.title),
        Option(type = OptionType.ACCEPTANCE.title, title = OptionType.ACCEPTANCE.title),
        Option(type = OptionType.INVENTORY.title, title = OptionType.INVENTORY.title),
        Option(type = OptionType.SELECTION.title, subOptionId = 1, title = DocType.REQUIREMENT_INVOICE.title,  description = DocType.REQUIREMENT_INVOICE.description),
        Option(type = OptionType.SELECTION.title, subOptionId = 1, title = DocType.WORKWEAR_TOOLS.title,  description = DocType.WORKWEAR_TOOLS.description),
        Option(type = OptionType.SELECTION.title, subOptionId = 1, title = DocType.DISPOSABLE_PPE.title, description = DocType.DISPOSABLE_PPE.description),
        Option(type = OptionType.ACCEPTANCE.title, subOptionId = 2, title = DocType.STANDARD_ACCEPTANCE.title, description = DocType.STANDARD_ACCEPTANCE.description),
        Option(type = OptionType.INVENTORY.title, subOptionId = 3, title = DocType.INVENTORY_IN_CELLS.title, description = DocType.INVENTORY_IN_CELLS.description),
    )

    fun getOptionsInitData() = optionsList
}

//private val optionsList = listOf(
//    Option(type = "ОТБОР", title = "ОТБОР"),
//    Option(type = "ПРИЕМКА", title = "ПРИЕМКА"),
//    Option(type = "ИНВЕНТАРИЗАЦИЯ", title = "ИНВЕНТАРИЗАЦИЯ"),
//    Option(type = "ОТБОР", subOptionId = 1, title = "Требование накладная",  description = "Создание документа Требование-накладная"),
//    Option(type = "ОТБОР", subOptionId = 1, title = "Спецодежда/Инструменты и прочие ТМЦ",  description = "Создание документа Передача материалов в эксплуатацию"),
//    Option(type = "ОТБОР", subOptionId = 1, title = "Одноразовые СИЗ", description = "Создание документа Перемещение (виртаульная ячейка)"),
//    Option(type = "ПРИЕМКА", subOptionId = 2, title = "Стандартная приемка", description = "Создание документа Приходный ордер на товары"),
//    Option(type = "ИНВЕНТАРИЗАЦИЯ", subOptionId = 3, title = "Инвентаризация в ячейках", description = "Создание документа Инвентаризация товаров на складе"),
//)