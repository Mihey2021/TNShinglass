package ru.tn.shinglass.activity.utilites.dialogs

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.tn.shinglass.R

object DialogScreen {
    const val IDD_ERROR = 1
    const val IDD_PROGRESS = 2

    fun getDialogBuilder(
        context: Context,
        ID: Int,
        message: String = ""
    ): MaterialAlertDialogBuilder {
        val alertDialog = MaterialAlertDialogBuilder(context)
        when (ID) {
            1 -> {
                with(alertDialog) {
                    setTitle(context.resources.getString(R.string.err_an_error_has_occured))
                    setMessage(message)
                    setCancelable(false)
                    setIcon(
                        context.resources.getDrawable(
                            R.drawable.ic_baseline_error_24,
                            context.theme
                        )
                    )
                }
            }
            2 -> {
                with(alertDialog) {
                    setView(R.layout.progress_layout)
                    setCancelable(false)
                }
            }
        }

        return alertDialog
    }
}