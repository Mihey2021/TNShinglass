package ru.tn.shinglass.activity.utilites.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.SoundPlayer
import ru.tn.shinglass.activity.utilites.SoundType
import ru.tn.shinglass.dto.models.BarcodeActions

class BarcodeScannerReceiver {
    class TNBarcode(
        var uniqueNumber: String = "",
        var dateOfManufacture: String = "",
        var batchNumber: String = "",
        var inBatchNumber: String = "",
        var gsOneId: String = "",
        var serialNumber: String = "",
        var expiryDate: String = "",
        var quantity: Float = 0.0f,
        var quantityInBarcode: String = "0000000",
        var measureOfUnit: String = "",
        var productCode: String = "",
        var error: String = "",
    )

    companion object : BroadcastReceiver() {
        private val _dataScan = MutableLiveData(Triple("", "", TNBarcode()))
        private var enabled: Boolean = true

        val dataScan: LiveData<Triple<String, String, TNBarcode>>
            get() = _dataScan

        fun setEnabled(isEnabled: Boolean = true) {
            enabled = isEnabled
        }

        fun isEnabled(): Boolean = enabled

        fun clearData() {
            _dataScan.value = Triple("", "", TNBarcode())
        }

        private fun parsingTNBarcode(dataScanBarcode: String): TNBarcode {

            val tnBarcode = TNBarcode()
            //if (dataScanBarcode.length != 48) return tnBarcode
            try {
                //Serial number barcode
                tnBarcode.uniqueNumber = dataScanBarcode.substring(0, 3)
                tnBarcode.dateOfManufacture = dataScanBarcode.substring(3, 9)
                tnBarcode.batchNumber = dataScanBarcode.substring(9, 14)
                tnBarcode.inBatchNumber = dataScanBarcode.substring(14, 17)
                tnBarcode.gsOneId = dataScanBarcode.substring(17, 26)

                //Shipping unit barcode
                tnBarcode.serialNumber = dataScanBarcode.substring(0, 26)
                tnBarcode.expiryDate = dataScanBarcode.substring(26, 32)
                val quantityInBarcode = dataScanBarcode.substring(32, 39)
                tnBarcode.quantityInBarcode = quantityInBarcode
                try {
                    val numberOfDigits = quantityInBarcode.substring(0, 1).toInt()
                    var divider = 1
                    for (i in 1..numberOfDigits) {
                        divider *= 10
                    }
                    val quantityInBarcodeAsInt =
                        quantityInBarcode.substring(1, quantityInBarcode.length).toFloat()
                    tnBarcode.quantity = quantityInBarcodeAsInt / divider
                } catch (e: Exception) {
                    tnBarcode.quantity = 0.0f
                    tnBarcode.error = e.message.toString()
                }

                tnBarcode.measureOfUnit = dataScanBarcode.substring(39, 42)
                tnBarcode.productCode = dataScanBarcode.substring(42, dataScanBarcode.length)
            } catch (e: Exception) {
                tnBarcode.error = e.message.toString()
            }
            return tnBarcode
        }

        override fun onReceive(context: Context, intent: Intent) {
            var tmpType = ""

            if (!enabled) {
                SoundPlayer(context, SoundType.SMALL_ERROR).playSound()
                return
            }

            var barcodeData = ""
            when (intent.action) {
                //if (intent.action == BarcodeActions.CIPHERLAB_BARCODE.action) {
                BarcodeActions.CIPHERLAB_BARCODE.action -> {
                    tmpType = intent.getStringExtra("Decoder_CodeType_String").toString()
                    barcodeData = intent.getStringExtra("Decoder_Data").toString()
                    _dataScan.value = Triple(barcodeData, tmpType, parsingTNBarcode(barcodeData))
                }
                BarcodeActions.ZEBRA_BARCODE.action -> {
                    tmpType = intent.getStringExtra("com.symbol.datawedge.label_type").toString()
                    barcodeData =
                        intent.getStringExtra("com.symbol.datawedge.data_string").toString()
                    _dataScan.value = Triple(barcodeData, tmpType, parsingTNBarcode(barcodeData))
                }
                BarcodeActions.HONEYWELL_BARCODE.action -> {
                    tmpType = intent.getStringExtra("codeId").toString()
                    barcodeData = intent.getStringExtra("data").toString()
                    _dataScan.value = Triple(barcodeData, tmpType, parsingTNBarcode(barcodeData))
                }
                BarcodeActions.UROVO_BARCODE.action -> {
                    val barcode = intent.getByteArrayExtra("barocode")
                    val barocodelen = intent.getIntExtra("length", 0)
                    val temp = intent.getByteExtra("barcodeType", 0.toByte()).toInt()
                    if (temp == 8) {
                        tmpType = "Code 128";
                    }
                    if (temp == 11) //EAN13
                    {
                        tmpType = "LABEL-TYPE-EAN13";
                    }
                    if (temp == 28) //QR
                    {
                        tmpType = "LABEL-TYPE-QRCODE";
                    }
                    if (temp == 31) {
                        tmpType = "QR Code";
                    }

                    barcodeData = String(barcode!!, 0, barocodelen)
                    _dataScan.value = Triple(barcodeData, tmpType, parsingTNBarcode(barcodeData))
                }

                BarcodeActions.DS2_BARCODE.action -> {
                    tmpType = intent.getStringExtra("EXTRA_BARCODE_DECODED_SYMBOLE").toString()
                    barcodeData = intent.getStringExtra("EXTRA_BARCODE_DECODED_DATA").toString()
                    _dataScan.value = Triple(barcodeData, tmpType, parsingTNBarcode(barcodeData))
                }

                BarcodeActions.M3.action -> {
                    tmpType = intent.getStringExtra("m3scanner_code_type").toString()
                    barcodeData = intent.getStringExtra("m3scannerdata").toString()
                    _dataScan.value = Triple(barcodeData, tmpType, parsingTNBarcode(barcodeData))
                }

            }
        }
    }
}