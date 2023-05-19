package ru.tn.shinglass.activity.utilites

import android.content.Context
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import ru.tn.shinglass.activity.AppActivity
import kotlin.system.exitProcess

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun closeActivity(startActivity: FragmentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity.finishAndRemoveTask()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity.finishAffinity()
        } else {
            startActivity.finish()
        }
        exitProcess(0)
    }

}