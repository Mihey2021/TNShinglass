package ru.tn.shinglass.activity

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.tn.shinglass.R
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

    override fun onDestroy() {
        //requireContext().unregisterReceiver(myDataReceiver)
        this.unregisterReceiver(BarcodeScannerReceiver)
        super.onDestroy()
    }
}