package ru.tn.shinglass.activity

import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.dto.models.BarcodeActions
import ru.tn.shinglass.dto.models.PreferenceKeys
import ru.tn.shinglass.viewmodel.SettingsViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

const val SESSION_LIFETIME_MIN: Long = 10L

class AppActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        val filter = IntentFilter().apply {
            BarcodeActions.values().forEach { addAction(it.action) }
        }
        this.registerReceiver(BarcodeScannerReceiver, filter)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //println("AAAAA. ${keyCode}")
        val currentFragment = supportFragmentManager.fragments[0].childFragmentManager.fragments[0]
        if(currentFragment is DetailScanFragment) {
            currentFragment.keyDownPressed(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }

    //Заверешение сеанса по истечении времени жизни сеанса (SESSION_LIFETIME_MIN) при бездействии пользователя
    private val idleHandler = Handler(Looper.getMainLooper())
    @RequiresApi(Build.VERSION_CODES.O)
    private val runnable = Runnable {
        run{
            val currentDateTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            val startDateTime = settingsViewModel.getPreferenceByKey<Long>(PreferenceKeys.SESSION_START.key, 0L, false) ?: 0L
            //val sessionLifeTimeInSec = SESSION_LIFETIME_MIN * 1000 * 60
            if ((currentDateTime.minus(startDateTime).toFloat() /60F) > SESSION_LIFETIME_MIN.toFloat()) {
                settingsViewModel.removePreference(PreferenceKeys.SESSION_START.key)
                AppAuth.getInstance().clearAuthData()
            } else {
                settingsViewModel.setPreferenceLong(PreferenceKeys.SESSION_START.key, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserInteraction() {
        super.onUserInteraction()
        idleHandler.removeCallbacks(runnable)
        idleHandler.postDelayed(runnable, (SESSION_LIFETIME_MIN * 1000 * 60))
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