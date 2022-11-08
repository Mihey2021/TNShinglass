package ru.tn.shinglass.activity.utilites.dialogs

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.tn.shinglass.R

object DialogScreen {
    const val IDD_ERROR = 1
    const val IDD_ERROR_SINGLE_BUTTON = 2
    const val IDD_PROGRESS = 3
    const val IDD_QUESTION = 4
    const val IDD_SUCCESS = 5
    const val IDD_INPUT = 6

    fun getDialog(
        context: Context,
        ID: Int,
        message: String = "",
        title: String = "",
        positiveButtonTitle: String? = null,
        negativeButtonTitle: String? = null,
        onDialogsInteractionListener: OnDialogsInteractionListener? = null,
        customView: View? = null,
        isCancelable: Boolean = false,
        titleIcon: Int = R.drawable.ic_baseline_check_24,
    ): AlertDialog {
        val alertDialog = MaterialAlertDialogBuilder(context)
        when (ID) {
            1, 2 -> {
                with(alertDialog) {
                    setTitle(context.resources.getString(R.string.err_an_error_has_occured))
                    setMessage(message)
                    setCancelable(isCancelable)
                    setIcon(
                        context.resources.getDrawable(
                            R.drawable.ic_baseline_error_24,
                            context.theme
                        )
                    )

                    setPositiveButton(
                        positiveButtonTitle
                            ?: context.resources.getString(
                                R.string.retry_loading
                            )
                    ) { dialog, _ ->
                        onDialogsInteractionListener?.onPositiveClickButton()
                        dialog.dismiss()
                    }
                    if (ID == 1) {
                        setNegativeButton(
                            negativeButtonTitle ?: context.resources.getString(R.string.cancel_text)
                        ) { dialog, _ ->
                            onDialogsInteractionListener?.onNegativeClickButton()
                            dialog.cancel()
                        }
                    }
                }
            }
            3 -> {
                with(alertDialog) {
                    setView(R.layout.progress_layout)
                    setCancelable(isCancelable)
                }
            }
            4 -> {
                with(alertDialog) {
                    setTitle(message)
                    setCancelable(isCancelable)
                    setIcon(
                        context.resources.getDrawable(
                            R.drawable.ic_baseline_question_outline_24,
                            context.theme
                        )
                    )

                    setPositiveButton(
                        positiveButtonTitle
                            ?: context.resources.getString(R.string.ok_text)
                    ) { dialog, _ ->
                        onDialogsInteractionListener?.onPositiveClickButton()
                        dialog.dismiss()
                    }

                    setNegativeButton(
                        negativeButtonTitle ?: context.resources.getString(R.string.cancel_text)
                    ) { dialog, _ ->
                        onDialogsInteractionListener?.onNegativeClickButton()
                        dialog.cancel()
                    }
                }
            }
            5 -> {
                with(alertDialog) {
                    setTitle(title)
                    setMessage(message)
                    setCancelable(isCancelable)
                    setIcon(
                        context.resources.getDrawable(
                            titleIcon,
                            context.theme
                        )
                    )

                    setPositiveButton(
                        positiveButtonTitle
                            ?: context.resources.getString(R.string.ok_text)
                    ) { dialog, _ ->
                        onDialogsInteractionListener?.onPositiveClickButton()
                        dialog.dismiss()
                    }
                }
            }
            6 -> {
                with(alertDialog) {
                    if (customView != null)
                        setView(customView)
                    setTitle(message)
                    setCancelable(isCancelable)
                    setPositiveButton(positiveButtonTitle ?: context.resources.getString(R.string.ok_text)
                    )
                    { _, _ ->
                        onDialogsInteractionListener?.onPositiveClickButton()
                        //dialog.dismiss()
                    }
                }
            }
        }

        return alertDialog.show()
    }
}