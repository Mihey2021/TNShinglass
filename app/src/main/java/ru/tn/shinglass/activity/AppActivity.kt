package ru.tn.shinglass.activity

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.dto.models.BarcodeActions

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        val filter = IntentFilter().apply {
            BarcodeActions.values().forEach { addAction(it.action) }
        }
        this.registerReceiver(BarcodeScannerReceiver, filter)
    }

//    override fun onBackPressed() {
//////        val count = supportFragmentManager.backStackEntryCount
//////        if (count == 0 ) {
//////            DialogScreen.getDialog(
//////                this,
//////                DialogScreen.IDD_QUESTION,
//////                resources.getString(R.string.question_exit_text),
//////                onDialogsInteractionListener = object : OnDialogsInteractionListener {
//////                    override fun onPositiveClickButton() {
//////                        finish()
//////                    }
//////                })
//////        } else {
//////        supportFragmentManager.popBackStack()
//////        //super.onBackPressed()
//////        }
////
////        val navHost = supportFragmentManager.findFragmentById(R.id.authFragment)
////        navHost?.let { navFragment ->
////            navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
//////                if (fragment is LandingFragment) {
//////                    finish()
//////                } else {
//////                    super.onBackPressed()
//////                }
//
//            }

    override fun onDestroy() {
        //requireContext().unregisterReceiver(myDataReceiver)
        this.unregisterReceiver(BarcodeScannerReceiver)
        super.onDestroy()
    }
}