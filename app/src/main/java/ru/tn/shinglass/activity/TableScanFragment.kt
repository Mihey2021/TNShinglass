package ru.tn.shinglass.activity

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.databinding.FragmentTableScanBinding
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.OnTableScanItemInteractionListener
import ru.tn.shinglass.adapters.TableScanAdapter
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.CurrentScanData

class TableScanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentTableScanBinding.inflate(inflater, container, false)
        with(binding) {
            headerTitleTextView.setTextColor(Color.BLACK)
            headerCountTextView.setTextColor(Color.BLACK)
            headerUnitOfMeasureTextView.setTextColor(Color.BLACK)
            headerCellTextView.setTextColor(Color.BLACK)
        }

        val selectedOption = arguments?.getSerializable("selectedOption") as Option
        setFragmentResult("requestSelectedOption", bundleOf("selectedOption" to selectedOption))
        val user1C = arguments?.getSerializable("userData") as User1C
        setFragmentResult("requestUserData", bundleOf("userData" to user1C))

        var demoData = listOf(
            CurrentScanData
                (
                id = 0,
                guid = "dfasdf-sdfsdf-sdf-sdf",
                title = "Test data Test data Test data Test data Test data Test data Test data Test data Test data Test data Test data Test data",
                count = 16.0,
                unitOfMeasureGuid = "dsfsdfsdf-sdfgfgfd-fdgfdgfg-fhgdgdfgf",
                unitOfMeasureTitle = "шт.",
                barcode = "4562132486"
            ),
            CurrentScanData
                (
                id = 1,
                guid = "dfasdf-sdfsdf-sdf-sdf",
                title = "Test data 2 bla-bla-bla",
                count = 1.6,
                unitOfMeasureGuid = "dsfsdfsdf-sdfgfgfd-fdgfdgfg-fhgdgdfgf",
                unitOfMeasureTitle = "кг.",
                barcode = "4562132486"
            )
        )
        val adapter = TableScanAdapter(object : OnTableScanItemInteractionListener {
            override fun selectItem(item: CurrentScanData) {
                //super.selectItem(item)
                //TODO: Для отладки
                val args = Bundle()
                args.putSerializable("userData", user1C)
                args.putSerializable("selectedOption", selectedOption)
                args.putString("itemBarCode", "")
                findNavController().navigate(R.id.action_tableScanFragment_to_detailScanFragment, args)
            }
        })

        binding.list.adapter = adapter





        adapter.submitList(demoData)

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {

            val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcode = dataScanPair?.first ?: ""
            val dataScanBarcodeType = dataScanPair?.second ?: ""

            if (dataScanBarcode == "") return@observe

            demoData += listOf(
                CurrentScanData
                    (
                    id = 2,
                    guid = "dfasdf-sdfsdf-sdf-sdf",
                    title = "Test data 3",
                    count = 21.0,
                    unitOfMeasureGuid = "dsfsdfsdf-sdfgfgfd-fdgfdgfg-fhgdgdfgf",
                    unitOfMeasureTitle = "упак.",
                    barcode = "4562132486"
                )
            )

            adapter.submitList(demoData)

            val args = Bundle()
            args.putSerializable("userData", user1C)
            args.putSerializable("selectedOption", selectedOption)
            args.putString("itemBarCode", dataScanBarcode)
            findNavController().navigate(R.id.action_tableScanFragment_to_detailScanFragment, args)

//            val scanDialog = AlertDialog.Builder(requireContext())
//            scanDialog.setTitle("Сканирование")
//            //scanDialog.setMessage("Сообщение диалога")
//
//            val titleCellTextView = TextView(requireContext())
//            titleCellTextView.text = "Ячейка"
//            scanDialog.setView(titleCellTextView)
//
//            val titleCellEditText = EditText(requireContext())
//            titleCellEditText.setText(dataScanBarcode)
//            scanDialog.setView(titleCellEditText)
//
//            scanDialog.show()
        }

        return binding.root
    }

}