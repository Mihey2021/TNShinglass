package ru.tn.shinglass.domain.repository

import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import ru.tn.shinglass.models.Option

interface OptionsRepository {
    fun getAll(): LiveData<List<Option>>
    fun setSelectedOption(option: Option)
    fun getSelectedOption(): Option?
}