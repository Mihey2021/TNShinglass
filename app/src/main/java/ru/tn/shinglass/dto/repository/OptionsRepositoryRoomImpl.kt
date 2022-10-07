package ru.tn.shinglass.dto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import ru.tn.shinglass.dao.room.OptionsDao
import ru.tn.shinglass.domain.repository.OptionsRepository
import ru.tn.shinglass.dto.repository.initdata.OptionsInitData
import ru.tn.shinglass.entity.OptionsEntity
import ru.tn.shinglass.models.Option

class OptionsRepositoryRoomImpl(private val dao: OptionsDao) : OptionsRepository {

    private val _selectedOption = MutableLiveData<Option?>()
    val selectedOption: LiveData<Option?>
        get() = _selectedOption

    init {
        val optionsCount = dao.getCountRecords()
        if (optionsCount == 0L) {
            val initOptionsData = OptionsInitData().getOptionsInitData()
            initOptionsData.forEach { option -> dao.save(OptionsEntity.fromDto(option)) }
            getAll()
        }
    }

    override fun getAll() =
        Transformations.map(dao.getAllOptions()) { listOptionsEntity ->
            listOptionsEntity.map { it.toDto() }
        }

    override fun setSelectedOption(option: Option) {
        _selectedOption.value = option
    }

    override fun getSelectedOption(): Option? = selectedOption.value

}