package ru.tn.shinglass.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.Employee
import ru.tn.shinglass.models.PhysicalPerson

@Entity
data class EmployeeEntity(
//    @PrimaryKey(autoGenerate = true)
    //val id: Long,
    val fio: String,
    @PrimaryKey(autoGenerate = false)
    val guid: String,
) {
    fun toDto() =
        Employee(employeeFio = fio, employeeGuid = guid)

    companion object {
        fun fromDto(dto: Employee) =
            EmployeeEntity(
                fio = dto.employeeFio,
                guid = dto.employeeGuid,
            )
    }
}

fun List<EmployeeEntity>.toDto(): List<Employee> = map(EmployeeEntity::toDto)
fun List<Employee>.toEntity(): List<EmployeeEntity> = map(EmployeeEntity::fromDto)