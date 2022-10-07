package ru.tn.shinglass.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.Option

@Entity
data class OptionsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val subOptionId: Long = 0L,
    val type: String,
    val title: String,
    val description: String,
) {

    fun toDto() =
        Option(id = id, subOptionId = subOptionId, type = type, title = title, description = description)

    companion object {
        fun fromDto(dto: Option) =
            OptionsEntity(
                id = dto.id,
                subOptionId = dto.subOptionId,
                type = dto.type,
                title = dto.title,
                description = dto.description,
            )
    }
}