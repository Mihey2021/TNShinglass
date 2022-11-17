package ru.tn.shinglass.dto.repository.initdata

import ru.tn.shinglass.models.DocType
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.OptionType
import ru.tn.shinglass.models.SubOptionType

class OptionsInitData {
    private val optionsList = listOf(
        Option(option = OptionType.SELECTION),
        Option(option = OptionType.ACCEPTANCE),
        Option(option = OptionType.INVENTORY),
        Option(option = OptionType.SELECTION, subOption = DocType.REQUIREMENT_INVOICE.subType, docType = DocType.REQUIREMENT_INVOICE),
        Option(option = OptionType.SELECTION, subOption = DocType.TOIR_REQUIREMENT_INVOICE.subType, docType = DocType.TOIR_REQUIREMENT_INVOICE),
        Option(option = OptionType.SELECTION, subOption = DocType.WORKWEAR_TOOLS.subType, docType = DocType.WORKWEAR_TOOLS),
        Option(option = OptionType.SELECTION, subOption = DocType.DISPOSABLE_PPE.subType, docType = DocType.DISPOSABLE_PPE),
        Option(option = OptionType.ACCEPTANCE, subOption = DocType.STANDARD_ACCEPTANCE.subType, docType = DocType.STANDARD_ACCEPTANCE),
        Option(option = OptionType.INVENTORY, subOption = DocType.INVENTORY_IN_CELLS.subType, docType = DocType.INVENTORY_IN_CELLS),
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