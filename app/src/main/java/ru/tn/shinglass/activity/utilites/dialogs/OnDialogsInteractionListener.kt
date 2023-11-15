package ru.tn.shinglass.activity.utilites.dialogs

interface OnDialogsInteractionListener {
    fun onNegativeClickButton() {}
    fun onPositiveClickButton() {}
    fun onClick(index: Int) {}
}