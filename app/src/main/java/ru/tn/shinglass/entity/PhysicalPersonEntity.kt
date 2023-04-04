package ru.tn.shinglass.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.PhysicalPerson

@Entity
data class PhysicalPersonEntity(
//    @PrimaryKey(autoGenerate = true)
    //val id: Long,
    val fio: String,
    @PrimaryKey(autoGenerate = false)
    val guid: String,
) {
    fun toDto() =
        PhysicalPerson(physicalPersonFio = fio, physicalPersonGuid = guid)

    companion object {
        fun fromDto(dto: PhysicalPerson) =
            PhysicalPersonEntity(
                fio = dto.physicalPersonFio,
                guid = dto.physicalPersonGuid,
            )
    }
}

fun List<PhysicalPersonEntity>.toDto(): List<PhysicalPerson> = map(PhysicalPersonEntity::toDto)
fun List<PhysicalPerson>.toEntity(): List<PhysicalPersonEntity> = map(PhysicalPersonEntity::fromDto)