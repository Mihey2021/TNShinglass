package ru.tn.shinglass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.tn.shinglass.db.room.AppDb
import ru.tn.shinglass.domain.repository.OptionsRepository
import ru.tn.shinglass.dto.repository.OptionsRepositoryRoomImpl

class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryOptions: OptionsRepository =
        OptionsRepositoryRoomImpl(AppDb.getInstance(context = application).optionsDao())


}