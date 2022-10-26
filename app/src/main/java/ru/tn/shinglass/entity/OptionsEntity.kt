package ru.tn.shinglass.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.DocType
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.OptionType
import ru.tn.shinglass.models.SubOptionType

@Entity
data class OptionsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val subOption: SubOptionType? = null,
    val option: OptionType,
    val docType: DocType? = null
) {

    fun toDto() =
        Option(id = id, subOption = subOption, option = option, docType = docType)

    companion object {
        fun fromDto(dto: Option) =
            OptionsEntity(
                id = dto.id,
                subOption = dto.subOption,
                option = dto.option,
                docType = dto.docType
            )
    }
}