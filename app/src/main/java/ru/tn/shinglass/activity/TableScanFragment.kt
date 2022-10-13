package ru.tn.shinglass.activity

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.databinding.FragmentTableScanBinding
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.OnTableScanItemInteractionListener
import ru.tn.shinglass.adapters.TableScanAdapter
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel

class TableScanFragment : Fragment() {

    private val viewModel: TableScanFragmentViewModel by viewModels()

    private lateinit var selectedOption: Option
    private lateinit var user1C: User1C

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

        selectedOption = arguments?.getSerializable("selectedOption") as Option
        setFragmentResult("requestSelectedOption", bundleOf("selectedOption" to selectedOption))
        user1C = arguments?.getSerializable("userData") as User1C
        setFragmentResult("requestUserData", bundleOf("userData" to user1C))

        val adapter = TableScanAdapter(object : OnTableScanItemInteractionListener {
            override fun selectItem(item: TableScan) {
                //super.selectItem(item)
                //TODO: Для отладки
                val args = Bundle()
                args.putSerializable("userData", user1C)
                args.putSerializable("selectedOption", selectedOption)
                args.putString("itemBarCode", "")
                args.putSerializable("scanRecord", item)
                findNavController().navigate(R.id.action_tableScanFragment_to_detailScanFragment, args)
            }
        })

        binding.list.adapter = adapter

        viewModel.refreshTableScan(user1C.getUserGUID(), selectedOption.id)

        viewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }


        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {

            val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcode = dataScanPair?.first ?: ""
            val dataScanBarcodeType = dataScanPair?.second ?: ""

            if (dataScanBarcode == "") return@observe

            val args = Bundle()
            args.putSerializable("userData", user1C)
            args.putSerializable("selectedOption", selectedOption)
            args.putString("barcode", dataScanBarcode)
            args.putSerializable("scanRecord", TableScan(OwnerGuid = user1C.getUserGUID()))
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

    override fun onResume() {
        viewModel.refreshTableScan(user1C.getUserGUID(), selectedOption.id)
        super.onResume()
    }
}