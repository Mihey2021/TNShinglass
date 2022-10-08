package ru.tn.shinglass.activity.utilites.dialogs

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.tn.shinglass.R

object DialogScreen {
    const val IDD_ERROR = 1
    const val IDD_PROGRESS = 2
    const val IDD_QUESTION = 3

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
            3 -> {
                with(alertDialog) {
                    setTitle(message)
                    setCancelable(false)
                    setIcon(
                        context.resources.getDrawable(
                            R.drawable.ic_baseline_question_outline_24,
                            context.theme
                        )
                    )
                    setNegativeButton(context.resources.getString(R.string.cancel_text)) { dialog, _ ->
                        dialog.cancel()
                    }
                }
            }
        }

        return alertDialog
    }
}