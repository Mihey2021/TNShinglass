package ru.tn.shinglass.dto.models

enum class BarcodeActions(val action: String) {
    CIPHERLAB_BARCODE("com.cipherlab.barcodebaseapi.PASS_DATA_2_APP"),
    ZEBRA_BARCODE("com.dw.ACTION"),
    HONEYWELL_BARCODE("hsm.RECVRBI"),
    UROVO_BARCODE("urovo.rcv.message"),
    DS2_BARCODE("app.dsic.barcodetray.BARCODE_BR_DECODING_DATA"),
    M3("com.android.server.scannerservice.broadcast"),
}