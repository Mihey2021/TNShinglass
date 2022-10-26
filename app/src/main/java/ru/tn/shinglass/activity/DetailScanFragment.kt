package ru.tn.shinglass.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.databinding.FragmentDetailScanBinding
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.DetailScanViewModel
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import java.lang.Exception

class DetailScanFragment : Fragment() {

//    companion object {
//        fun newInstance() = DetailScanFragment()
//    }

    private val viewModel: DetailScanViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private var forceOverwrite = false

    private var progressDialog: AlertDialog? = null
    private lateinit var currentRecord: TableScan

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDetailScanBinding.inflate(inflater, container, false)

        val selectedOption = arguments?.getSerializable("selectedOption") as Option
        val user1C = arguments?.getSerializable("userData") as User1C
        var barcode = arguments?.getString("barcode")
        var editRecord = arguments?.getSerializable("editRecord") as TableScan

        forceOverwrite = editRecord.id != 0L

        //BarcodeScannerReceiver.clearData()

        val tempScanRecord = TempScanRecord(editRecord)
        tempScanRecord.OperationId = selectedOption.id
        tempScanRecord.OperationTitle = selectedOption.docType?.title ?: ""

        with(binding) {
            operationTitleTextView.text = selectedOption.docType?.title ?: ""
            //divisionTextView.setText("Подразделение из настроек")

            if (selectedOption.docType == DocType.INVENTORY_IN_CELLS) {
                divisionTextInputLayout.visibility = View.GONE
                purposeOfUseTextInputLayout.visibility = View.GONE
                workwearDisposableCheckBox.visibility = View.GONE
                workwearOrdinaryCheckBox.visibility = View.GONE
            } else {
                divisionTextInputLayout.visibility = View.VISIBLE
                purposeOfUseTextInputLayout.visibility = View.VISIBLE
                workwearDisposableCheckBox.visibility = View.VISIBLE
                workwearOrdinaryCheckBox.visibility = View.VISIBLE
            }
            divisionTextInputLayout.error = "Укажите подразделение!".toString()

            val isNextRecordInSession = editRecord.id == 0L && !editRecord.cellGuid.isNullOrBlank()


            val warehouseGuidByPrefs = viewModel.getPreferenceByKey("warehouse_guid")

            warehouseTextInputLayout.isEnabled = editRecord.id == 0L && editRecord.warehouseGuid.isNullOrBlank()
            //warehouseTextInputLayout.visibility = if (isNextRecordInSession) View.GONE else View.VISIBLE
            warehouseTextInputLayout.visibility = View.GONE

            if (warehouseGuidByPrefs.isNullOrBlank()) {
                warehouseTextInputLayout.error = "Склад не задан в настройках!".toString()
            }

            val warehouseGuid = if (editRecord?.warehouseGuid.isNullOrBlank()) warehouseGuidByPrefs else editRecord?.warehouseGuid
            val warehouse = viewModel.getWarehouseByGuid(warehouseGuid ?: "")

            warehouseTextView.setText(warehouse?.title)
            tempScanRecord.warehouseGuid = warehouse?.guid.toString()
            warehouseTextInputLayout.error = null

            warehouseTextView.setOnClickListener {
                if (warehouseTextView.adapter == null) {
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    getAllWarehousesList(binding)
                }
            }
            warehouseTextView.setOnItemClickListener { adapterView, _, position, _ ->
                val warehouseItem = adapterView.getItemAtPosition(position) as Warehouse
                warehouseTextView.setText(warehouseItem.title)
                warehouseTextInputLayout.error = null
                tempScanRecord.warehouseGuid = warehouseItem.guid
            }
//            warehouseTextInputLayout.setEndIconOnClickListener {
//                if (warehouseTextView.adapter == null) {
//                    progressDialog =
//                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
//                    getAllWarehousesList(binding)
//                }
//            }

            //itemTextView.setText(barcode)
            countEditText.setText(editRecord.Count.toString())
            //countEditText.contentDescription = "шт."
            if (!countEditText.text.isNullOrBlank())
                countTextInputLayout.hint =
                    if (editRecord.ItemMeasureOfUnitTitle.isNullOrBlank()) "<?>" else editRecord.ItemMeasureOfUnitTitle
            countEditText.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        countTextInputLayout.error = null
                        false
                    }
                    else -> false
                }
            }

            //itemMeasureOfUnitTitleTextView.text = "шт."
            workwearDisposableCheckBox.isChecked = editRecord.WorkwearDisposable

            purposeOfUseTextView.setText(editRecord.PurposeOfUse)
            purposeOfUseTextView.setOnClickListener {
                //TODO: Обработка выбора назначения использования
                Toast.makeText(requireContext(), "Клик по ссылке", Toast.LENGTH_SHORT).show()
            }

            //phisicalPersonTextView.setText("Выбранное физическое лицо")

            phisicalPersonTextInputLayout.hint = "МОЛ"
            phisicalPersonTextInputLayout.isEnabled = editRecord.id == 0L && editRecord.PhysicalPersonGUID.isNullOrBlank()
            //phisicalPersonTextInputLayout.visibility = if (isNextRecordInSession) View.GONE else View.VISIBLE
            phisicalPersonTextInputLayout.visibility = View.GONE
            //phisicalPersonTextView.inputType = if (editRecord.PhysicalPersonGUID.isNullOrBlank()) InputType.TYPE_NULL else InputType.TYPE_CLASS_TEXT
            phisicalPersonTextView.setText(editRecord.PhysicalPersonTitle)
            phisicalPersonTextView.setOnClickListener {
                if (phisicalPersonTextView.adapter == null) {
                    getPhysicalPersonList()
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                }
            }
            phisicalPersonTextView.setOnItemClickListener { adapterView, _, position, _ ->
                val physivalPeron = adapterView.getItemAtPosition(position) as PhysicalPerson
                phisicalPersonTextView.setText(physivalPeron.fio)
                phisicalPersonTextInputLayout.error = null
                tempScanRecord.PhysicalPersonGUID = physivalPeron.guid
                tempScanRecord.PhysicalPersonTitle = physivalPeron.fio
            }

            binding.cellTextView.setText(editRecord.cellTitle)
            binding.itemTextView.setText(editRecord.ItemTitle)

            ownerTextView.setText("${getString(R.string.owner_title)} ${user1C.getUser1C()}")
            tempScanRecord.OwnerGuid = user1C.getUserGUID()

            buttonApply.setOnTouchListener{_, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        сheckFillingAndSave(tempScanRecord, user1C, selectedOption, binding)
                        false
                    }
                    else -> false
                }
            }

            buttonApply.setOnClickListener {
                //сheckFillingAndSave(tempScanRecord, user1C, selectedOption, binding)
                binding.buttonApply.clearFocus()
            }
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {

            binding.buttonApply.clearFocus()

            val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcodeFromScaner = dataScanPair?.first ?: ""
            val dataScanBarcode =
                if (dataScanBarcodeFromScaner == "") barcode ?: "" else dataScanBarcodeFromScaner
            barcode = ""

            val dataScanBarcodeType = dataScanPair?.second ?: ""

            if (dataScanBarcode == "") return@observe

            progressDialog?.show()
            if (dataScanBarcodeType == "Code 128") {
                retrofitViewModel.getCellByBarcode(dataScanBarcode)
                binding.cellTextInputLayout.error = null
            } else {
                if (binding.cellTextView.text.isNullOrBlank()) {
                    binding.cellTextInputLayout.error = "Отсканируйте ячейку!"
                    return@observe
                }
                retrofitViewModel.getItemByBarcode(dataScanBarcode)

                var newCount = try {
                    (binding.countEditText.text.toString()).toDouble() + (if(editRecord.coefficient != 0.0) editRecord.coefficient else 1.0)
                } catch(e:Exception) {
                    editRecord.Count + 1
                }
//                var newCount =
//                    if (binding.countEditText.text.isNullOrBlank()) 0.0 else editRecord.Count + 1
                binding.countEditText.setText(newCount.toString())

                binding.itemTextInputLayout.error = null
            }
        }

        retrofitViewModel.cellData.observe(viewLifecycleOwner) {

            if (it == null) return@observe
            if (it.guid.isNullOrBlank()) {
                DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR_SINGLE_BUTTON, "Ячейка не найдена!", "Ok")
                return@observe
            }
            progressDialog?.dismiss()
            binding.cellTextView.setText(it.title)
            tempScanRecord.cellGuid = it.guid
            tempScanRecord.cellTitle = it.title

        }


        retrofitViewModel.itemData.observe(viewLifecycleOwner) {

            if (it == null) return@observe
            if (it.itemGuid.isNullOrBlank()) {
                DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR_SINGLE_BUTTON, "Номенклатура не найдена!", "Ok")
                return@observe
            }
            progressDialog?.dismiss()
            binding.itemTextView.setText(it.itemTitle)
            binding.countTextInputLayout.hint = it.unitOfMeasurementTitle
            binding.qualityEditText.setText(it.qualityTitle)

            tempScanRecord.ItemGUID = it.itemGuid
            tempScanRecord.ItemTitle = it.itemTitle
            tempScanRecord.ItemMeasureOfUnitGUID = it.unitOfMeasurementGuid
            tempScanRecord.ItemMeasureOfUnitTitle = it.unitOfMeasurementTitle
            tempScanRecord.coefficient = it.coefficient
            tempScanRecord.qualityGuid = it.qualityGuid
            tempScanRecord.qualityTitle = it.qualityTitle
        }

        retrofitViewModel.listDataPhysicalPersons.observe(viewLifecycleOwner) {

            if (it.isEmpty()) return@observe

            val dataList = arrayListOf<PhysicalPerson>()
            it.forEach { person -> dataList.add(person) }
            progressDialog?.dismiss()
            val adapter = DynamicListAdapter<PhysicalPerson>(
                requireContext(),
                R.layout.dynamic_prefs_layout,
                dataList
            )
            binding.phisicalPersonTextView.setAdapter(adapter)
            //binding.phisicalPersonTextView.callOnClick()
        }

        retrofitViewModel.listDataWarehouses.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            viewModel.saveWarehouses(it)
            setWarehousesAdapter(binding)
        }

        retrofitViewModel.requestError.observe(viewLifecycleOwner) { error ->

            if (error == null) return@observe

            progressDialog?.dismiss()
            DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_ERROR,
                error.message,
                onDialogsInteractionListener = object : OnDialogsInteractionListener {
                    override fun onPositiveClickButton() {
                        when (error.requestName) {
                            "getPhysicalPersonList" -> {
                                progressDialog?.show()
                                getPhysicalPersonList()
                            }
                            "getAllWarehousesList" -> {
                                progressDialog?.show()
                                getAllWarehousesList(binding)
                            }

                        }
                    }
                })
        }

        return binding.root
    }

    private fun сheckFillingAndSave(
        tempScanRecord: TempScanRecord,
        user1C: User1C,
        selectedOption: Option,
        binding: FragmentDetailScanBinding
    ) {

        var isError = false

        with(binding) {
            if (binding.warehouseTextView.text.isNullOrBlank()) {
                warehouseTextInputLayout.error = "Укажите склад!"
                isError = true
            }
            if (binding.phisicalPersonTextView.text.isNullOrBlank()) {
                phisicalPersonTextInputLayout.error = "Укажите МОЛ!"
                isError = true
            }
            if (binding.cellTextView.text.isNullOrBlank()) {
                cellTextInputLayout.error = "Ячейка не отсканирована!"
                isError = true
            }
            if (binding.itemTextView.text.isNullOrBlank()) {
                itemTextInputLayout.error = "Номенклатура не отсканирована!"
                isError = true
            }
            if (binding.countEditText.text.toString().isBlank())
                tempScanRecord.Count = 0.0
            else
                tempScanRecord.Count = binding.countEditText.text.toString().toDouble()

            if (binding.countEditText.text.isNullOrBlank() || tempScanRecord.Count == 0.0) {
                countTextInputLayout.error = "Количество не может быть нулевым!"
                isError = true
            }
        }

        if (isError) return

        viewModel.saveScanRecord(tempScanRecord.toScanRecord(), forceOverwrite)
        val args = Bundle()
        args.putSerializable("userData", user1C)
        args.putSerializable("selectedOption", selectedOption)
        //findNavController().navigate(R.id.action_detailScanFragment_to_tableScanFragment, args)
        BarcodeScannerReceiver.clearData()
        findNavController().navigateUp()
        //requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
    }

    private fun setWarehousesAdapter(
        binding: FragmentDetailScanBinding
    ) {

//        val dataList = arrayListOf<Warehouse>()
//        val warehousesList = viewModel.getAllWarehousesList()
//        warehousesList.forEach { warehouse -> dataList.add(warehouse) }
//        progressDialog?.dismiss()
//        val adapter = DynamicListAdapter<Warehouse>(
//            requireContext(),
//            R.layout.dynamic_prefs_layout,
//            dataList
//        )
//        binding.warehouseTextView.setAdapter(adapter)

    }

    private fun getAllWarehousesList(binding: FragmentDetailScanBinding) {
//        val dbWarehouses = viewModel.getAllWarehousesList()
//        if (dbWarehouses.isEmpty()) {
//            retrofitViewModel.getAllWarehouses()
//        } else {
//            setWarehousesAdapter(binding)
//        }
    }


    private fun getPhysicalPersonList() {
        //retrofitViewModel.getPhysicalPersonList()
    }

    override fun onStop() {
        BarcodeScannerReceiver.clearData()
        super.onStop()
    }
}

class TempScanRecord {
    var id: Long = 0L
    var OperationId: Long = 0L
    var OperationTitle: String = ""
    var cellTitle: String = ""
    var cellGuid: String = ""
    var ItemTitle: String = ""
    var ItemGUID: String = ""
    var ItemMeasureOfUnitTitle: String = ""
    var ItemMeasureOfUnitGUID: String = ""
    var Count: Double = 0.0
    var coefficient: Double = 0.0
    var qualityGuid: String = ""
    var qualityTitle: String = ""
    var WorkwearOrdinary: Boolean = false
    var WorkwearDisposable: Boolean = false
    var DivisionId: Long = 0L
    var DivisionOrganization: Long = 0L
    var warehouseGuid: String = ""
    var PurposeOfUseTitle: String = ""
    var PurposeOfUse: String = ""
    var PhysicalPersonTitle: String = ""
    var PhysicalPersonGUID: String = ""
    var OwnerGuid: String = ""

    constructor(scanRecord: TableScan) {
        id = scanRecord.id
        OperationId = scanRecord.OperationId
        OperationTitle = scanRecord.OperationTitle
        cellTitle = scanRecord.cellTitle
        cellGuid = scanRecord.cellGuid
        ItemTitle = scanRecord.ItemTitle
        ItemGUID = scanRecord.ItemGUID
        ItemMeasureOfUnitTitle = scanRecord.ItemMeasureOfUnitTitle
        ItemMeasureOfUnitGUID = scanRecord.ItemMeasureOfUnitGUID
        Count = scanRecord.Count
        coefficient = scanRecord.coefficient
        qualityGuid = scanRecord.qualityGuid
        qualityTitle = scanRecord.qualityTitle
        WorkwearOrdinary = scanRecord.WorkwearOrdinary
        WorkwearDisposable = scanRecord.WorkwearDisposable
        DivisionId = scanRecord.DivisionId
        DivisionOrganization = scanRecord.DivisionOrganization
        warehouseGuid = scanRecord.warehouseGuid
        PurposeOfUseTitle = scanRecord.PurposeOfUseTitle
        PurposeOfUse = scanRecord.PurposeOfUse
        PhysicalPersonTitle = scanRecord.PhysicalPersonTitle
        PhysicalPersonGUID = scanRecord.PhysicalPersonGUID
        OwnerGuid = scanRecord.OwnerGuid
    }

    fun toScanRecord(): TableScan {
        return TableScan(
            id = id,
            OperationId = OperationId,
            OperationTitle = OperationTitle,
            cellTitle = cellTitle,
            cellGuid = cellGuid,
            ItemTitle = ItemTitle,
            ItemGUID = ItemGUID,
            ItemMeasureOfUnitTitle = ItemMeasureOfUnitTitle,
            ItemMeasureOfUnitGUID = ItemMeasureOfUnitGUID,
            Count = Count,
            coefficient = coefficient,
            qualityGuid = qualityGuid,
            qualityTitle = qualityTitle,
            WorkwearOrdinary = WorkwearOrdinary,
            WorkwearDisposable = WorkwearDisposable,
            DivisionId = DivisionId,
            DivisionOrganization = DivisionOrganization,
            warehouseGuid = warehouseGuid,
            PurposeOfUseTitle = PurposeOfUseTitle,
            PurposeOfUse = PurposeOfUse,
            PhysicalPersonTitle = PhysicalPersonTitle,
            PhysicalPersonGUID = PhysicalPersonGUID,
            OwnerGuid = OwnerGuid
        )
    }

}


