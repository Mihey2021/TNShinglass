package ru.tn.shinglass.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.databinding.FragmentBarcodeParsingTnBinding

class BarcodeParsingTN : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentBarcodeParsingTnBinding.inflate(inflater, container, false)

        binding.barcodeTextView.text = getString(R.string.scan_tn_barcode_text)

        binding.backBarcodeParseButton.setOnClickListener {
            findNavController().navigateUp()
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) { dataScanPair ->

            val dataScanBarcode = dataScanPair.first
            val dataScanBarcodeType = dataScanPair.second

            if (dataScanBarcode == "") return@observe

            BarcodeScannerReceiver.clearData()

            try {
                with(binding) {
                    val barcodeLength = dataScanBarcode.length
                    headerBarcode.text =
                        "${getString(R.string.length_text)}: ${barcodeLength}. ${getString(R.string.type_text)}: $dataScanBarcodeType"

                    if (dataScanBarcode.length < 48) {
                        binding.detailGroup.visibility = View.GONE
                        binding.barcodeTextView.text =
                            "$dataScanBarcode\n${getString(R.string.barcode_length_less_than_48_characters)}"
                        return@observe
                    } else {
                        binding.detailGroup.visibility = View.VISIBLE
                    }

                    barcodeTextView.text = dataScanBarcode

                    //Serial number barcode
                    uniqueNumberTextView.text = dataScanBarcode.substring(0, 3)
                    dateOfManufactureTextView.text = dataScanBarcode.substring(3, 9)
                    batchNumberTextView.text = dataScanBarcode.substring(9, 14)
                    inBatchNumberTextView.text = dataScanBarcode.substring(14, 17)
                    gsOneIdTextView.text = dataScanBarcode.substring(17, 26)

                    //Shipping unit barcode
                    serialNumberTextView.text = dataScanBarcode.substring(0, 26)
                    expiryDateTextView.text = dataScanBarcode.substring(26, 32)
                    val quantityInBarcode = dataScanBarcode.substring(32, 39)
                    try {
                        val numberOfDigits = quantityInBarcode.substring(0, 1).toInt()
                        var divider = 1
                        for (i in 1..numberOfDigits) {
                            divider *= 10
                        }
                        val quantityInBarcodeAsInt = quantityInBarcode.substring(1,quantityInBarcode.length).toFloat()
                        quantityTextView.text = "$quantityInBarcode (${(quantityInBarcodeAsInt / divider)})"
                    } catch (e:Exception) {
                        quantityTextView.text = "<?>"
                    }

                    measureOfUnitTextView.text = dataScanBarcode.substring(39, 42)
                    productCodeTextView.text = dataScanBarcode.substring(42, dataScanBarcode.length)

                }
            } catch (e: Exception) {
                binding.barcodeTextView.text =
                    "${getString(R.string.err_barcode_format)}: ${e.message}"
            }
        }
        return binding.root
    }
}