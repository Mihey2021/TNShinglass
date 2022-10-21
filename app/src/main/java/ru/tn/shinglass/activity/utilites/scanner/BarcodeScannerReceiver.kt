package ru.tn.shinglass.activity.utilites.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.dto.models.BarcodeActions

class BarcodeScannerReceiver {
    companion object : BroadcastReceiver() {
        private val _dataScan = MutableLiveData(Pair("", ""))
        val dataScan: LiveData<Pair<String, String>>
            get() = _dataScan

        fun clearData() {
            _dataScan.value = "" to ""
        }

        override fun onReceive(context: Context?, intent: Intent) {
            var tmpType = ""

            when (intent.action) {
                //if (intent.action == BarcodeActions.CIPHERLAB_BARCODE.action) {
                BarcodeActions.CIPHERLAB_BARCODE.action -> {
                    tmpType = intent.getStringExtra("Decoder_CodeType_String").toString()
                    _dataScan.value = intent.getStringExtra("Decoder_Data").toString() to tmpType
                }
                BarcodeActions.ZEBRA_BARCODE.action -> {
                    tmpType = intent.getStringExtra("com.symbol.datawedge.label_type").toString()
                    _dataScan.value = intent.getStringExtra("com.symbol.datawedge.data_string")
                        .toString() to tmpType
                }
                BarcodeActions.HONEYWELL_BARCODE.action -> {
                    tmpType = intent.getStringExtra("codeId").toString()
                    _dataScan.value = intent.getStringExtra("data").toString() to tmpType
                }
                BarcodeActions.UROVO_BARCODE.action -> {
                    val barcode = intent.getByteArrayExtra("barocode")
                    val barocodelen = intent.getIntExtra("length", 0)
                    val temp = intent.getByteExtra("barcodeType", 0.toByte()).toInt()
                    if (temp == 11) //EAN13
                    {
                        tmpType = "LABEL-TYPE-EAN13";
                    }
                    if (temp == 28) //QR
                    {
                        tmpType = "LABEL-TYPE-QRCODE";
                    }
                    _dataScan.value = String(barcode!!, 0, barocodelen) to tmpType
                }

                BarcodeActions.DS2_BARCODE.action -> {
                    tmpType = intent.getStringExtra("EXTRA_BARCODE_DECODED_SYMBOLE").toString()
                    _dataScan.value =
                        intent.getStringExtra("EXTRA_BARCODE_DECODED_DATA").toString() to tmpType
                }

                BarcodeActions.M3.action -> {
                    tmpType = intent.getStringExtra("m3scanner_code_type").toString()
                    _dataScan.value =
                        intent.getStringExtra("m3scannerdata").toString() to tmpType
                }

            }
        }
    }
}