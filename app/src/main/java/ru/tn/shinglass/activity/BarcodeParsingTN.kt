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

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) { dataScanTriple ->

            val dataScanBarcode = dataScanTriple.first
            val dataScanBarcodeType = dataScanTriple.second
            val tnBarcode = dataScanTriple.third

            if (dataScanBarcode == "") return@observe

            BarcodeScannerReceiver.clearData()


            with(binding) {
                val barcodeLength = dataScanBarcode.length
                headerBarcode.text =
                    "${getString(R.string.length_text)}: ${barcodeLength}. ${getString(R.string.type_text)}: $dataScanBarcodeType"

                if (dataScanBarcode.length < 48) {
                    binding.detailGroup.visibility = View.GONE
                    binding.barcodeTextView.text =
                        "$dataScanBarcode\n${getString(R.string.barcode_length_less_than_48_characters)}\n${tnBarcode.error}"
                    return@observe
                } else {
                    binding.detailGroup.visibility = View.VISIBLE
                }
                barcodeTextView.text = dataScanBarcode

                if (tnBarcode.error.isNotBlank()) {
                    binding.errorDetailText.visibility = View.VISIBLE
                    binding.errorDetailText.text =
                        "${getString(R.string.err_barcode_format)}: ${tnBarcode.error}"
                } else {
                    binding.errorDetailText.visibility = View.GONE
                }

                //Serial number barcode
                uniqueNumberTextView.text = tnBarcode.uniqueNumber
                dateOfManufactureTextView.text = tnBarcode.dateOfManufacture
                batchNumberTextView.text = tnBarcode.batchNumber
                inBatchNumberTextView.text = tnBarcode.inBatchNumber
                gsOneIdTextView.text = tnBarcode.gsOneId

                //Shipping unit barcode
                serialNumberTextView.text = tnBarcode.serialNumber
                expiryDateTextView.text = tnBarcode.expiryDate

                val quantity = tnBarcode.quantity
                if (quantity != 0.0f) {
                    quantityTextView.text =
                        "${tnBarcode.quantityInBarcode} (${tnBarcode.quantity})"
                } else {
                    quantityTextView.text = "<?>"
                }

                measureOfUnitTextView.text = tnBarcode.measureOfUnit
                productCodeTextView.text = tnBarcode.productCode


            }
        }
        return binding.root
    }
}