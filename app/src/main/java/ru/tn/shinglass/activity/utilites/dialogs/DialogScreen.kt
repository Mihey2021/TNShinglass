package ru.tn.shinglass.activity.utilites.dialogs

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.SoundPlayer
import ru.tn.shinglass.activity.utilites.SoundType
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.models.Barcode
import kotlin.Exception

object DialogScreen {
    const val IDD_ERROR = 1
    const val IDD_ERROR_SINGLE_BUTTON = 2
    const val IDD_PROGRESS = 3
    const val IDD_QUESTION = 4
    const val IDD_SUCCESS = 5
    const val IDD_INPUT = 6

    private var dialog: AlertDialog? = null
    private var progressDialog: AlertDialog? = null
    private var inputDialog: AlertDialog? = null

    fun getDialog(dialogType: Int = 0): AlertDialog? {
        return when (dialogType) {
            IDD_PROGRESS -> progressDialog
            IDD_INPUT -> inputDialog
            else -> dialog
        }
    }

    fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(300)
        }
    }

    fun showDialog(
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
        playSound: Boolean = false,
        singleChoiceArray: Array<String>? = null,
        //singleChoiceAdapter: DynamicListAdapter<Barcode>? = null,
        singleChoiceAdapter: ListAdapter? = null,
    ): AlertDialog {
        val alertDialog = MaterialAlertDialogBuilder(context)
        when (ID) {
            1, 2 -> {
                with(alertDialog) {
                    setTitle(if (title == "") context.resources.getString(R.string.err_an_error_has_occured) else title)
                    setMessage(message)
                    //setCancelable(isCancelable)
                    setCancelable(false)
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
                        clearSavingDialogs()
                    }
                    if (ID == 1) {
                        setNegativeButton(
                            negativeButtonTitle ?: context.resources.getString(R.string.cancel_text)
                        ) { dialog, _ ->
                            onDialogsInteractionListener?.onNegativeClickButton()
                            dialog.cancel()
                            clearSavingDialogs()
                        }
                    }
                }
            }
            3 -> {
                with(alertDialog) {
                    getDialog(3)?.dismiss()
                    setView(R.layout.progress_layout)
                    setCancelable(isCancelable)
                }
            }
            4 -> {
                with(alertDialog) {
                    setTitle(if (title == "") message else title)
                    setMessage(message)
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
                        clearSavingDialogs()
                    }

                    setNegativeButton(
                        negativeButtonTitle ?: context.resources.getString(R.string.cancel_text)
                    ) { dialog, _ ->
                        onDialogsInteractionListener?.onNegativeClickButton()
                        dialog.cancel()
                        clearSavingDialogs()
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
                        clearSavingDialogs()
                    }
                }
            }
            6 -> {
                with(alertDialog) {
                    if (customView != null)
                        setView(customView)
                    setTitle(message)
                    setCancelable(isCancelable)
                    if (singleChoiceAdapter != null) {
                        setSingleChoiceItems(singleChoiceAdapter, 0) { dialog, index ->
                            onDialogsInteractionListener?.onClick(index)
                            clearSavingDialogs()
                            dialog.dismiss()
                        }
                    } else {
                        setPositiveButton(
                            positiveButtonTitle ?: context.resources.getString(R.string.ok_text)
                        )
                        { _, _ ->
                            onDialogsInteractionListener?.onPositiveClickButton()
                            //dialog.dismiss()
                            clearSavingDialogs()
                        }
                    }
                }
            }
        }

        var generatedDialog = alertDialog.show()
        when (ID) {
            IDD_PROGRESS -> progressDialog = generatedDialog
            IDD_INPUT -> inputDialog = generatedDialog
            else -> dialog = generatedDialog
        }

        //if (playSound || ID in 1..2) {
        if (ID in 1..2) {
            vibrate(context)
            playSound(context, SoundType.ERROR)
        }
        if (ID == IDD_QUESTION) playSound(context, SoundType.ATTENTION)

        //}

//        if(ID == IDD_ERROR_SINGLE_BUTTON) Log.d("TTT", "[DialogScreen] Create and show ErrorDialog: $generatedDialog")
//        if(ID == IDD_PROGRESS) Log.d("TTT", "[DialogScreen] Create and show ProgressDialog: $generatedDialog")
        return generatedDialog!!
    }

    private fun clearSavingDialogs() {
        //progressDialog = null
        inputDialog = null
        dialog = null
    }

    private fun playSound(context: Context, soundType: SoundType) {
        SoundPlayer(context, soundType).playSound()
        //appPause(context = context)
    }

    private fun appPause(timeout: Long = 500L, context: Context) {
        try {
            Thread.sleep(timeout)
        } catch (e: Exception) {
            showDialog(
                context,
                IDD_ERROR,
                title = context.resources.getString(R.string.err_an_error_has_occured),
                message = e.stackTraceToString()
            )
        }
    }
}