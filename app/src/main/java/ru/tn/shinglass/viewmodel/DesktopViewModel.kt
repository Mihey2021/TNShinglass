package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.OptionsRepository
import ru.tn.shinglass.dto.repository.OptionsRepositoryRoomImpl
import ru.tn.shinglass.models.Option

class DesktopViewModel(application: Application): AndroidViewModel(application) {
    private val repositoryOptions: OptionsRepository =
        OptionsRepositoryRoomImpl(AppDb.getInstance(context = application).optionsDao())

    val optionsData = repositoryOptions.getAll()
    fun setSelectedOption(selectedOption: Option) = repositoryOptions.setSelectedOption(selectedOption)
    fun getSelectedOption() = repositoryOptions.getSelectedOption()
}