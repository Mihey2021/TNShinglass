package ru.tn.shinglass.adapters

import ru.tn.shinglass.models.Option

interface OnOptionsInteractionListener {
    fun selectOption(option: Option) {}
}