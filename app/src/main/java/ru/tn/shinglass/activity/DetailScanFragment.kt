package ru.tn.shinglass.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.SoundPlayer
import ru.tn.shinglass.activity.utilites.SoundType
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.databinding.CellReceiverDialogBinding
import ru.tn.shinglass.databinding.FragmentDetailScanBinding
import ru.tn.shinglass.dto.models.DetailScanFields
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.DetailScanViewModel
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel
import java.lang.Exception

class DetailScanFragment : Fragment() {

//    companion object {
//        fun newInstance() = DetailScanFragment()
//    }

    private val viewModel: DetailScanViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private val tableScanViewModel: TableScanFragmentViewModel by viewModels()
    private var forceOverwrite = false
    private var tableFromExternalDocument: List<TableScan> = listOf()
    private var fragmentBinding: FragmentDetailScanBinding? = null

    //private var dialog: AlertDialog? = null
    private var scannerIsBlocked: Boolean = false
    private var cellReceiverDialogBinding: CellReceiverDialogBinding? = null
    private var usedLogistics: Boolean = false
    private lateinit var currentRecord: TableScan
    lateinit var tempScanRecord: TempScanRecord
    private var warehouseGuid: String? = null

    lateinit var user1C: User1C

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDetailScanBinding.inflate(inflater, container, false)
        fragmentBinding = binding

        val selectedOption = arguments?.getSerializable("selectedOption") as Option
        //val user1C = arguments?.getSerializable("userData") as User1C
        user1C = AppAuth.getInstance().getAuthData()

        AppAuth.getInstance().authStateFlow.observe(viewLifecycleOwner) { authState ->
            user1C = authState.user1C
            if (user1C.getUserGUID().isEmpty()) findNavController().navigate(R.id.authFragment)
        }

        var barcode = arguments?.getString("barcode")
        var barcodeType = arguments?.getString("barcodeType")
        var editRecord = arguments?.getSerializable("editRecord") as TableScan
        warehouseGuid = arguments?.getString("warehouseGuid")


        if (selectedOption.docType == DocType.TOIR_REQUIREMENT_INVOICE) {
            tableFromExternalDocument =
                tableScanViewModel.getAllScanRecordsByOwner(user1C.getUserGUID(), selectedOption.id)
        }

        forceOverwrite =
            (editRecord.id != 0L) //|| (selectedOption.docType == DocType.TOIR_REPAIR_ESTIMATE && editRecord.ItemGUID.isEmpty())

        //BarcodeScannerReceiver.clearData()

        tempScanRecord = TempScanRecord(editRecord)
        tempScanRecord.OperationId = selectedOption.id
        tempScanRecord.OperationTitle = selectedOption.docType?.title ?: ""
        tempScanRecord.warehouseGuid =
            DocumentHeaders.getWarehouse()?.warehouseGuid ?: ""//warehouseGuid ?: ""

        if (selectedOption.docType == DocType.BETWEEN_CELLS) {
            binding.cellTextInputLayout.hint = getString(R.string.header_cell)
            if (tempScanRecord.cellReceiverGuid.isBlank())
                binding.buttonApply.setText(R.string.next)
        }

        if (tempScanRecord.warehouseGuid.isBlank()) {
            val currentWarehouseGuid: String = DocumentHeaders.getWarehouse()?.warehouseGuid
                ?: settingsViewModel.getPreferenceByKey("warehouse_guid", "") ?: ""
            if (currentWarehouseGuid.isBlank()) {
                BarcodeScannerReceiver.setEnabled(false)
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    title = getString(R.string.warehouse_not_specified),
                    message = getString(R.string.warehouse_is_not_set_open_settings),
                    positiveButtonTitle = getString(R.string.text_yes),
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            BarcodeScannerReceiver.setEnabled(true)
                            findNavController().navigate(R.id.action_global_settingsFragment)
                        }
                    }
                )
            } else {
                retrofitViewModel.getWarehousesListByGuid(currentWarehouseGuid)
            }
        } else {
            usedLogistics = DocumentHeaders.getWarehouse()?.usesLogistics ?: false
        }

        //usedLogistics = settingsViewModel.getPreferenceByKey("usedLogistics", true) ?: false

        initViewsInScreen(binding, editRecord, selectedOption)

        retrofitViewModel.listDataWarehouses.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) return@observe
            usedLogistics = it.firstOrNull()?.usesLogistics ?: false
            initViewsInScreen(binding, editRecord, selectedOption)
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) { dataScanTriple ->

            if (!BarcodeScannerReceiver.isEnabled()) {
                SoundPlayer(requireContext(), SoundType.SMALL_ERROR).playSound()
                return@observe
            }

            binding.buttonApply.clearFocus()

            //val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcodeFromScanner = dataScanTriple.first
            val dataScanBarcode =
                if (dataScanBarcodeFromScanner == "") barcode
                    ?: "" else dataScanBarcodeFromScanner
            barcode = ""

            val dataScanBarcodeTypeFromScanner = dataScanTriple.second
            val dataScanBarcodeType = if (dataScanBarcodeTypeFromScanner == "") barcodeType
                ?: "" else dataScanBarcodeTypeFromScanner
            barcodeType = ""

            val tnBarcode = dataScanTriple.third

            if (dataScanBarcode == "") return@observe

//            if (cellReceiverDialogBinding == null)
//                DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.show() //dialog?.show()

            if (dataScanBarcodeType == "Code 128" || dataScanBarcodeType == "QR Code") {
                if (dataScanBarcode.length == 48) {
                    binding.serialNumberEditText.setText(tnBarcode.serialNumber)
                    return@observe
                }
                retrofitViewModel.getCellByBarcode(
                    dataScanBarcode,
                    editRecord.docHeaders.getWarehouse()?.warehouseGuid ?: ""
                )
                if (cellReceiverDialogBinding != null) {
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error = null
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.isErrorEnabled = false
                } else {
                    binding.cellTextInputLayout.error = null
                    binding.cellTextInputLayout.isErrorEnabled = false
                }
                return@observe
            } else if (dataScanBarcode.length == 13) {
                retrofitViewModel.getItemByBarcode(dataScanBarcode)
            } else {
                if (binding.cellTextView.text.isNullOrBlank()) {
                    binding.cellTextInputLayout.error = getString(R.string.scan_the_cell)
                    return@observe
                }
                if (cellReceiverDialogBinding != null) {
                    //if (cellReceiverDialogBinding!!.cellReceiverTextView.text.isNullOrBlank()) {
                    if (tempScanRecord.cellReceiverGuid.isBlank()) {
                        cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                            getString(R.string.scan_the_cell)
                        return@observe
                    }
                }
                if (cellReceiverDialogBinding == null) {
                    retrofitViewModel.getItemByBarcode(dataScanBarcode)
                }
            }
        }

        retrofitViewModel.cellData.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            if (it.guid.isBlank()) {
                val warehouse = editRecord.docHeaders.getWarehouse()
                if (cellReceiverDialogBinding != null) {
                    cellReceiverDialogBinding!!.cellReceiverTextView.setText("")
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                        "Ячейка не задана"
                    tempScanRecord.cellReceiverGuid = ""
                    tempScanRecord.cellReceiverTitle = ""
                } else {
                    binding.cellTextView.setText("")
                    binding.cellTextInputLayout.error = "Ячейка не задана"
                    tempScanRecord.cellGuid = ""
                    tempScanRecord.cellTitle = ""
                }
                BarcodeScannerReceiver.setEnabled(false)
                Log.d("TTT", "Close Dialog: ${DialogScreen.getDialog() ?: "[null]"}")
                DialogScreen.getDialog()?.dismiss()
                //Thread.sleep(1500L)
                //closeAllDialogs()
                val ddd = DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                    title = "Ячейка не найдена!",
                    message = if (warehouse == null) "Не задан склад." else "Проверьте, что ячейка принадлежит складу \"${warehouse.warehouseTitle}\" и отсканируйте ШК снова.",
                    positiveButtonTitle = "Ok",
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            BarcodeScannerReceiver.setEnabled()
                        }
                    }
                )
                if (DialogScreen.getDialog()?.isShowing == false) ddd.show()
                Log.d("TTT", "Show ErrorDialog: $ddd")
                //DialogScreen.showDialog(requireContext(),DialogScreen.IDD_SUCCESS, message = "KSdaklfjds fjds posdj fansfk ajpofj sd!..")
                SoundPlayer(requireContext(), SoundType.CELL_NOT_FOUND).playSound()
                return@observe
            }
            checkCellReceiver(it)
            if (cellReceiverDialogBinding == null) {
                binding.cellTextView.setText(it.title)
                binding.cellTextInputLayout.error = null
                binding.cellTextInputLayout.isErrorEnabled = false
//                println("tableFromExternalDocument.isNotEmpty() = ${tableFromExternalDocument.isNotEmpty()}")
//                println("tableFromExternalDocument.isEmpty() = ${tableFromExternalDocument.isEmpty()}")
                tempScanRecord.replacement =
                    (tempScanRecord.id != 0L && it.guid != tempScanRecord.cellGuid && tempScanRecord.cellGuid.isNotEmpty())
                if (tempScanRecord.replacement) SoundPlayer(
                    requireContext(),
                    SoundType.CELL_CHANGED
                ).playSound()
                tempScanRecord.cellGuid = it.guid
                tempScanRecord.cellTitle = it.title
            }
        }


        retrofitViewModel.itemData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            //dialog?.dismiss()
            DialogScreen.getDialog()?.dismiss()
            if (it.itemGuid.isBlank()) {
                itemNotFound(it)
                return@observe
            }

            if (tempScanRecord.ItemGUID.isNotEmpty() && (tempScanRecord.ItemGUID != it.itemGuid)) {
                scannerIsBlocked = true
                DialogScreen.getDialog()?.dismiss()
                DialogScreen.showDialog(requireContext(), DialogScreen.IDD_QUESTION,
                    title = resources.getString(R.string.other_nomenclature_text),
                    message = resources.getString(R.string.scanned_another_nomenclature_text),
                    positiveButtonTitle = resources.getString(R.string.text_yes),
                    negativeButtonTitle = resources.getString(R.string.text_no),
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            processReceivedItemData(it, binding, replacementNomenclature = true)
                            scannerIsBlocked = false
                        }

                        override fun onNegativeClickButton() {
                            scannerIsBlocked = false
                        }
                    })
                SoundPlayer(requireContext(), SoundType.ANOTHER_ITEM_SCAN).playSound()
                return@observe
            }

            processReceivedItemData(it, binding)

        }

        retrofitViewModel.listDataPhysicalPersons.observe(viewLifecycleOwner) {

            if (it.isEmpty()) return@observe

            val dataList = arrayListOf<PhysicalPerson>()
            it.forEach { person -> dataList.add(person) }
            DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()//dialog?.dismiss()
            val adapter = DynamicListAdapter<PhysicalPerson>(
                requireContext(),
                R.layout.dynamic_prefs_layout,
                dataList
            )
            binding.physicalPersonTextView.setAdapter(adapter)
            //binding.physicalPersonTextView.callOnClick()
        }

        retrofitViewModel.listDataWarehouses.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            viewModel.saveWarehouses(it)
            setWarehousesAdapter(binding)
        }

        retrofitViewModel.cellListData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            val cellArrayList: ArrayList<Cell> = arrayListOf()
            cellArrayList.clear()
            cellArrayList.add(Cell(getString(R.string.not_chosen_text), ""))

            it.forEach { cell ->
                cellArrayList.add(cell)
            }

            val cellAdapter = DynamicListAdapter<Cell>(
                requireContext(),
                R.layout.dynamic_prefs_layout,
                cellArrayList
            )

            if (cellReceiverDialogBinding != null) {
                cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error = null
                cellReceiverDialogBinding!!.cellReceiverTextInputLayout.isErrorEnabled = false
                (cellReceiverDialogBinding!!.cellReceiverTextView as? AutoCompleteTextView)?.setAdapter(
                    cellAdapter
                )
                cellReceiverDialogBinding!!.cellReceiverTextView.showDropDown()
            } else {
                (binding.cellTextView as? AutoCompleteTextView)?.setAdapter(cellAdapter)
                binding.cellTextView.showDropDown()
            }
        }

        retrofitViewModel.dataState.observe(viewLifecycleOwner) {
//            if (it.loading) {
//                if (dialog?.isShowing == false || dialog == null)
//                    dialog =
//                        DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
//            } else
//                dialog?.dismiss()
            Log.d("TTT", "DataState DetailFragment")
            if (it.loading) {
                if (DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.isShowing == false || DialogScreen.getDialog(
                        DialogScreen.IDD_PROGRESS
                    ) == null
                )
                    DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else {
                Log.d(
                    "TTT",
                    "Close ProgressDialog: ${DialogScreen.getDialog(DialogScreen.IDD_PROGRESS) ?: "[null]"}"
                )
                DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
                //dialog?.dismiss()
            }

            if (it.error) {
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getPhysicalPersonList" -> {
                                    DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)
                                        ?.show()//dialog?.show()
                                    getPhysicalPersonList()
                                }
                                "getAllWarehousesList" -> {
                                    DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)
                                        ?.show()//dialog?.show()
                                    getAllWarehousesList(binding)
                                }
                                "getCellByBarcode" -> {
                                    DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.show()
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "barcode")
                                        retrofitViewModel.getCellByBarcode(
                                            barcode = requestParam.propertyValue,
                                            warehouseGuid = editRecord.docHeaders.getWarehouse()?.warehouseGuid
                                                ?: ""
                                        )
                                }
                                "getItemByBarcode" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "barcode")
                                        retrofitViewModel.getItemByBarcode(barcode = requestParam.propertyValue)
                                }
                            }
                        }
                    })
            }
        }

        return binding.root
    }

    private fun checkCellReceiver(cellData: Cell): Boolean {
        DialogScreen.getDialog()?.dismiss()
        var isError = false
        if (cellReceiverDialogBinding != null) {
            if (cellData.guid == tempScanRecord.cellGuid) {
                cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                    getString(R.string.to_cell_equal_from_cell)//"Ячейка Куда не может совпадать с ячейкой Откуда"
                cellReceiverDialogBinding!!.cellReceiverTextView.setText("")
                tempScanRecord.cellReceiverGuid = ""
                tempScanRecord.cellReceiverTitle = ""
                isError = true
                return isError
            }
            cellReceiverDialogBinding!!.cellReceiverTextView.setText(cellData.title)
            cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error = null
            cellReceiverDialogBinding!!.cellReceiverTextInputLayout.isErrorEnabled = false
            tempScanRecord.cellReceiverGuid = cellData.guid
            tempScanRecord.cellReceiverTitle = cellData.title
            isError = false
        } else {
            if (cellData.guid == tempScanRecord.cellReceiverGuid) {
                //binding.cellTextInputLayout.error = getString(R.string.to_cell_equal_from_cell)
//                scannerIsBlocked = true
//
//                DialogScreen.showDialog(requireContext(), DialogScreen.IDD_ERROR_SINGLE_BUTTON,
//                    title = getString(R.string.cell_text),
//                    message = getString(R.string.to_cell_equal_from_cell),
//                    isCancelable = false,
//                    positiveButtonTitle = getString(R.string.ok_text),
//                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
//                        override fun onPositiveClickButton() {
//                            scannerIsBlocked = false
//                        }
//                    }
//                )
                isError = true
                return isError
            }

//            val binding = FragmentDetailScanBinding.inflate(LayoutInflater.from(requireContext()))
//            binding.cellTextView.setText(cellData.title)
//            binding.cellTextInputLayout.error = null
//            binding.cellTextInputLayout.isErrorEnabled = false
////                println("tableFromExternalDocument.isNotEmpty() = ${tableFromExternalDocument.isNotEmpty()}")
////                println("tableFromExternalDocument.isEmpty() = ${tableFromExternalDocument.isEmpty()}")
//            tempScanRecord.replacement =
//                (tempScanRecord.id != 0L && cellData.guid != tempScanRecord.cellGuid && tempScanRecord.cellGuid.isNotEmpty())
//            if (tempScanRecord.replacement) SoundPlayer(
//                requireContext(),
//                SoundType.CELL_CHANGED
//            ).playSound()
//            tempScanRecord.cellGuid = cellData.guid
//            tempScanRecord.cellTitle = cellData.title
        }

        return isError
    }

    private fun initViewsInScreen(
        binding: FragmentDetailScanBinding,
        editRecord: TableScan,
        selectedOption: Option
    ) {
        val detailScanFields = selectedOption.subOption?.detailScanFields
        with(binding) {
            operationTitleTextView.text = selectedOption.docType?.title ?: ""
            if (detailScanFields != null) {
                val rootView = binding.root
                for (field in detailScanFields) {
                    if (field.fieldType == "TextInputLayout") {
                        rootView.findViewById<TextInputLayout>(field.viewId)?.visibility =
                            View.VISIBLE
                        if (!usedLogistics && field.usedLogistics)
                            rootView.findViewById<TextInputLayout>(field.viewId)?.visibility =
                                View.GONE
                    }
                    if (field.fieldType == "CheckBox") {
                        rootView.findViewById<CheckBox>(field.viewId)?.visibility = View.VISIBLE
                        if (!usedLogistics && field.usedLogistics)
                            rootView.findViewById<CheckBox>(field.viewId)?.visibility = View.GONE
                    }
                }
            }

            cellReceiverTextInputLayout.setEndIconOnClickListener {
                cellReceiverTextView.setText("")
                tempScanRecord.cellReceiverGuid = ""
                tempScanRecord.cellReceiverTitle = ""
                binding.buttonApply.setText(R.string.next)
            }

            val isNextRecordInSession =
                editRecord.id == 0L && !editRecord.cellGuid.isNullOrBlank()

            countEditText.setText(editRecord.Count.toString())
            countEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (tableFromExternalDocument.isEmpty()) return

                    val changedCount = try {
                        (text.toString()).toDouble()
                    } catch (e: Exception) {
                        0.0
                    }
                    setCountTextColor(
                        changedCount,
                        tempScanRecord.docCount,
                        tempScanRecord.totalCount,
                        countEditText
                    )
                }

                override fun afterTextChanged(editText: Editable?) {
                }
            })

            if (tableFromExternalDocument.isNotEmpty()) {
                val currentCount = try {
                    (countEditText.text.toString()).toDouble()
                } catch (e: Exception) {
                    0.0
                }
                setCountTextColor(
                    currentCount,
                    tempScanRecord.docCount,
                    tempScanRecord.totalCount,
                    countEditText
                )
            }

            //countEditText.contentDescription = "шт."
            var hint = "<?>"
            if (editRecord.ItemMeasureOfUnitTitle.isNotBlank()) {
                hint = if (tempScanRecord.docGuid != "")
                    "Потребность: ${tempScanRecord.docCount - tempScanRecord.totalCount} ${editRecord.ItemMeasureOfUnitTitle}"
                else
                    editRecord.ItemMeasureOfUnitTitle
            }


            if (!countEditText.text.isNullOrBlank())
                countTextInputLayout.hint = hint

            countEditText.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        countTextInputLayout.error = null
                        countTextInputLayout.isErrorEnabled = false
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


            binding.cellTextView.setText(editRecord.cellTitle)

            setTextInputElementProperties(
                element = cellTextView,
                elementLayout = cellTextInputLayout
            )

            if (!cellTextView.text.isNullOrBlank()) {
                cellTextView.inputType = android.text.InputType.TYPE_NULL
                cellTextInputLayout.endIconDrawable =
                    androidx.appcompat.content.res.AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_clear_24
                    )
                cellTextInputLayout.endIconContentDescription =
                    getString(R.string.clear_text)
                cellTextInputLayout.error = null
                ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(binding.root)
            }
            binding.cellReceiverTextView.setText(editRecord.cellReceiverTitle)
            binding.itemTextView.setText(editRecord.ItemTitle)

            ownerTextView.setText("${getString(R.string.owner_title)} ${user1C.getUser1C()}")
            tempScanRecord.OwnerGuid = user1C.getUserGUID()

//            buttonApply.setOnTouchListener { _, motionEvent ->
//                when (motionEvent.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        сheckFillingAndSave(tempScanRecord, user1C, selectedOption, binding)
//                        false
//                    }
//                    else -> false
//                }
//            }
//
//            buttonApply.setOnClickListener {
//                //сheckFillingAndSave(tempScanRecord, user1C, selectedOption, binding)
//                binding.buttonApply.clearFocus()
//            }

            buttonApply.setOnClickListener {
                checkFillingAndSave(tempScanRecord, user1C, selectedOption, binding)
            }
        }
    }

    private fun setTextInputElementProperties(
        element: AutoCompleteTextView,
        elementLayout: TextInputLayout,
        receivedCell: Boolean = false
    ) {
        element.setOnClickListener {
            if (elementLayout.error == null) return@setOnClickListener
            elementLayout.error = null
            element.text = null
            element.inputType = android.text.InputType.TYPE_CLASS_TEXT
            elementLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            elementLayout.endIconDrawable =
                androidx.appcompat.content.res.AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_search_24
                )
            elementLayout.endIconContentDescription =
                getString(R.string.find_text)
        }

        elementLayout.setEndIconOnClickListener {
            ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(element)
            if (element.inputType != android.text.InputType.TYPE_NULL) {
                retrofitViewModel.getCellsList(
                    warehouseGuid = tempScanRecord.warehouseGuid ?: "",
                    partNameCode = element.text.toString()
                )
            } else {
                DocumentHeaders.setEmployee(null)
                element.text = null
                elementLayout.helperText = null
                if (!receivedCell) {
                    tempScanRecord.cellGuid = ""
                    tempScanRecord.cellTitle = ""
                } else {
                    tempScanRecord.cellReceiverGuid = ""
                    tempScanRecord.cellReceiverTitle = ""
                }
                element.inputType = android.text.InputType.TYPE_CLASS_TEXT
                elementLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                elementLayout.endIconDrawable =
                    androidx.appcompat.content.res.AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_search_24
                    )
                elementLayout.endIconContentDescription =
                    getString(R.string.find_text)
            }
        }

        element.setOnItemClickListener { adapterView, _, position, _ ->
            val cell = adapterView.getItemAtPosition(position) as Cell
            tempScanRecord.replacement =
                (tempScanRecord.id != 0L && cell.guid != tempScanRecord.cellGuid && tempScanRecord.cellGuid.isNotEmpty())
            if (tempScanRecord.replacement) SoundPlayer(
                requireContext(),
                SoundType.CELL_CHANGED
            ).playSound()
            if (!receivedCell) {
                tempScanRecord.cellGuid = cell.guid
                tempScanRecord.cellTitle = cell.title
            } else {
                tempScanRecord.cellReceiverGuid = cell.guid
                tempScanRecord.cellReceiverTitle = cell.title
            }
            element.setText(cell.title)
            element.inputType = android.text.InputType.TYPE_NULL
            elementLayout.endIconDrawable =
                androidx.appcompat.content.res.AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_clear_24
                )
            elementLayout.endIconContentDescription =
                getString(R.string.clear_text)
            elementLayout.error = null
            val binding = FragmentDetailScanBinding.inflate(LayoutInflater.from(requireContext()))
            ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(binding.root)
        }
    }

    private fun itemNotFound(item: Nomenclature, isExternalDocument: Boolean = false) {
        val context = requireContext()
        scannerIsBlocked = true
        Log.d(
            "TTT",
            "Close Dialog [itemNotFound]: ${DialogScreen.getDialog(DialogScreen.IDD_PROGRESS) ?: "[null]"}"
        )
        DialogScreen.getDialog()?.dismiss()
        DialogScreen.showDialog(
            context,
            DialogScreen.IDD_ERROR_SINGLE_BUTTON,
            title = getString(R.string.nomenclature_not_found_text),
            message = if (isExternalDocument) "${getString(R.string.check_nomenclature_and_measure_of_unit_scan_again_text)}\n(ед. изм. ШК: ${item.unitOfMeasurementTitle})" else getString(
                R.string.check_label_scan_again_text
            ),
            positiveButtonTitle = "Ok",
            onDialogsInteractionListener = object : OnDialogsInteractionListener {
                override fun onPositiveClickButton() {
                    scannerIsBlocked = false
                }
            })
        SoundPlayer(context, SoundType.ITEM_NOT_FOUND).playSound()
    }

    private fun processReceivedItemData(
        it: Nomenclature,
        binding: FragmentDetailScanBinding,
        replacementNomenclature: Boolean = false
    ) {
        var newCount = try {
            if (replacementNomenclature) 1.0 else (binding.countEditText.text.toString()).toDouble() + 1.0
        } catch (e: Exception) {
            1.0
        }

        tempScanRecord.replacement = replacementNomenclature

        //Если собираем по внешнему документу
        if (tableFromExternalDocument.isNotEmpty()) {
            //Если отбираем на основании документа из 1С и это редактирование записи, проверяем что отсканировали номенклатуру позиции, которую изменяем - они должны совпадать, иначе попытка подмены - отменяем.
            if (tempScanRecord.ItemGUID != "") {
                if (it.itemGuid != tempScanRecord.ItemGUID) {
                    scannerIsBlocked = true
                    Log.d(
                        "TTT",
                        "Close Dialog [itemNotFound]: ${DialogScreen.getDialog(DialogScreen.IDD_PROGRESS) ?: "[null]"}"
                    )
                    DialogScreen.getDialog()?.dismiss()
                    DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                        title = getString(R.string.changing_an_entry_text),
                        message = getString(R.string.nomenclature_substitution_text),
                        positiveButtonTitle = "Ok",
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                scannerIsBlocked = false
                            }
                        }
                    )
                    SoundPlayer(requireContext(), SoundType.ANOTHER_ITEM_SCAN).playSound()
                    return
                }
            }

            val filteredItems = tableFromExternalDocument
                .filter { tableItem -> tableItem.ItemGUID == it.itemGuid }
                .filter { filteredItem -> filteredItem.ItemMeasureOfUnitGUID == it.unitOfMeasurementGuid }
            //Номенклатура есть в документе
            if (filteredItems.isNotEmpty()) {
                //Нашлась 1 позиция
                //if (filteredItems.size == 1) {
                if (filteredItems.size > 0) {
                    var selectedItem = filteredItems[0]
                    //Отфильтруем по ячейке в документе = отсканированной ячейке
                    val filteredCellItems =
                        filteredItems.filter { filteredItem -> filteredItem.cellGuid == tempScanRecord.cellGuid }
                    if (filteredCellItems.isNotEmpty()) {
                        //Позиция в данной ячейке есть в документе (должна быть всегда 1, т.к. фильтры по Номенклатуре, Ед. изм. и Ячейке)
                        selectedItem = filteredCellItems[0]
//                            // Если ячейка уже задана в таблице документа, проверим что она совпадает с отсканированной
//                        val selectedItem = filteredItems[0]
//                        if (selectedItem.cellGuid != "") {
//                            if (selectedItem.cellGuid == tempScanRecord.cellGuid) {
                        tempScanRecord =
                            TempScanRecord(selectedItem) //Ставим текущую рабочую запись равной найденной в таблице документа
                        newCount =
                            if (newCount == 1.0) selectedItem.Count + 1.0 else newCount
                    } else {
                        if (selectedItem.cellGuid != "") {
                            //Позиция есть в документе, но отсканирована другая ячейка - это нормально, нужно добавить новую строку с отсканированной ячейкой
                            val oldScanRecord = tempScanRecord
                            tempScanRecord =
                                TempScanRecord(
                                    selectedItem.copy(
                                        id = 0L,
                                        Count = newCount,
                                        cellGuid = oldScanRecord.cellGuid,
                                        cellTitle = oldScanRecord.cellTitle
                                    )
                                )
                        } else {
                            //Ячейка еще не указана в таблице документа, прописываем отсканированную.
                            val cellGuid = tempScanRecord.cellGuid
                            val cellTitle = tempScanRecord.cellTitle
                            tempScanRecord = TempScanRecord(
                                selectedItem.copy(
                                    cellGuid = cellGuid,
                                    cellTitle = cellTitle,
                                )
                            )
                        }
                        //Номенклатура есть, но ячейка в документе не совпадает с отсканированной
//                                dialog = DialogScreen.getDialog(
//                                    requireContext(),
//                                    DialogScreen.IDD_ERROR_SINGLE_BUTTON,
//                                    title = getString(R.string.cell_text),
//                                    message = getString(R.string.not_correct_cell),
//                                    positiveButtonTitle = "Ok"
//                                )
//                                return@observe
                    }
                    //binding.countEditText.setText(selectedItem.Count.toString())

                    setCountTextColor(
                        newCount,
                        selectedItem.docCount,
                        selectedItem.totalCount,
                        binding.countEditText
                    )

                }
                forceOverwrite = tempScanRecord.id != 0L
            } else {
                //Номенклатура не найдена или не совпадает единица измерения в отсканированном ШК и таблице документа
                itemNotFound(it, true)
                return
            }
        }
        binding.itemTextView.setText(it.itemTitle)
        if (tempScanRecord.docCount != 0.0)
            binding.countTextInputLayout.hint =
                "Потребность: ${tempScanRecord.docCount - tempScanRecord.totalCount} ${it.unitOfMeasurementTitle}"
        else
            binding.countTextInputLayout.hint = it.unitOfMeasurementTitle

        binding.qualityEditText.setText(it.qualityTitle)

        binding.countEditText.setText(newCount.toString())
        binding.countTextInputLayout.error = null
        binding.countTextInputLayout.isErrorEnabled = false

        binding.itemTextInputLayout.error = null
        binding.itemTextInputLayout.isErrorEnabled = false

        tempScanRecord.ItemGUID = it.itemGuid
        tempScanRecord.ItemTitle = it.itemTitle
        tempScanRecord.ItemMeasureOfUnitGUID = it.unitOfMeasurementGuid
        tempScanRecord.ItemMeasureOfUnitTitle = it.unitOfMeasurementTitle
        tempScanRecord.coefficient = it.coefficient
        tempScanRecord.qualityGuid = it.qualityGuid
        tempScanRecord.qualityTitle = it.qualityTitle
    }

    private fun getCellReceiverDialogBinding(): CellReceiverDialogBinding =
        CellReceiverDialogBinding.inflate(LayoutInflater.from(requireContext()))

    fun keyDownPressed(keyCode: Int, event: KeyEvent?) {
        //Toast.makeText(requireContext(), "Нажата ${keyCode}. Event: ${event?.displayLabel}",Toast.LENGTH_LONG).show()
        var textCount: Editable =
            fragmentBinding?.countEditText?.text ?: SpannableStringBuilder("0")
        if (event != null) {
            if (event.keyCode == FunctionKeyCodes.M3_M1.keyCode || event.keyCode == FunctionKeyCodes.CIPHERLAB_P1.keyCode) {
                findNavController().navigateUp()
            }
            if (event.keyCode == FunctionKeyCodes.M3_M2.keyCode || event.keyCode == FunctionKeyCodes.CIPHERLAB_P2.keyCode) {
                fragmentBinding?.buttonApply?.callOnClick()
            }
            if (event.keyCode == KeyEvent.KEYCODE_DEL) fragmentBinding?.countEditText?.text?.clear()
            if (event.keyCode == KeyEvent.KEYCODE_PERIOD) {
                if (!textCount.contains(".")) {
                    if ((fragmentBinding?.countEditText?.text?.length ?: -1) > 0) {
                        fragmentBinding?.countEditText?.text =
                            textCount.append(event.displayLabel.toString())
                    }
                }
            }
            val digitKey = event.number.digitToIntOrNull() ?: -1
            if (digitKey in 0..9) {
                //if (textCount.contains(".")) textCount.clear()
                fragmentBinding?.countEditText?.text =
                    textCount.append(event.displayLabel.toString())
            }
        }
    }

    enum class FunctionKeyCodes(val keyCode: Int) {
        M3_M1(314),
        M3_M2(315),
        CIPHERLAB_P1(539),
        CIPHERLAB_P2(540),
    }

    private fun setCountTextColor(
        newCount: Double,
        docCount: Double,
        totalCount: Double,
        countEditText: EditText
    ) {

        val need = docCount - totalCount

        if (newCount > need) {
            countEditText.setTextColor(Color.parseColor("#FF0000"))
        }

        if (newCount < need) {
            countEditText.setTextColor(Color.parseColor("#FF8C00"))
        }

        if (newCount == need) {
            countEditText.setTextColor(Color.parseColor("#006400"))
        }

    }


    private fun checkFillingAndSave(
        tempScanRecord: TempScanRecord,
        user1C: User1C,
        selectedOption: Option,
        binding: FragmentDetailScanBinding
    ) {

        var isError = false
        var fieldValueIsNotCorrect = false
        var needShowCellReceiverDialog = false

        val tempDetailScanFields = selectedOption.subOption?.detailScanFields
        var detailScanFields: List<DetailScanFields>? = null
        if (!usedLogistics) {
            detailScanFields =
                tempDetailScanFields?.filter { dsf -> dsf.usedLogistics == usedLogistics }
        } else {
            detailScanFields = tempDetailScanFields?.toList()
        }

        //val detailScanFields = selectedOption.subOption?.detailScanFields
        if (detailScanFields?.isEmpty() == true) isError = true

        detailScanFields?.forEach {
            if (it == DetailScanFields.CELL) {
//                fieldValueIsNotCorrect = binding.cellTextView.text.isNullOrBlank()
                fieldValueIsNotCorrect = tempScanRecord.cellGuid.isBlank()
                isError = (fieldValueIsNotCorrect || isError)
                if (isError && fieldValueIsNotCorrect) {
                    binding.cellTextInputLayout.error = "Ячейка не отсканирована!"
                    return@forEach
                }
            }

            if (it == DetailScanFields.ITEM) {
                fieldValueIsNotCorrect = binding.itemTextView.text.isNullOrBlank()
                isError = (fieldValueIsNotCorrect || isError)
                if (isError && fieldValueIsNotCorrect) {
                    binding.itemTextInputLayout.error = "Номенклатура не отсканирована!"
                    return@forEach
                }
            }

            if (it == DetailScanFields.COUNT) {
                if (binding.countEditText.text.toString().isBlank()) {
                    tempScanRecord.Count = 0.0
                } else {
                    if ((binding.countEditText.text?.length ?: -1) > 0)
                        tempScanRecord.Count = binding.countEditText.text.toString().toDouble()
                }
                fieldValueIsNotCorrect =
                    binding.countEditText.text.isNullOrBlank() || tempScanRecord.Count == 0.0
                isError = (fieldValueIsNotCorrect || isError)
                if (isError && fieldValueIsNotCorrect) {
                    if (fieldValueIsNotCorrect)
                        binding.countTextInputLayout.error = "Количество не может быть нулевым!"
                    return@forEach
                } else {
                    binding.countTextInputLayout.error = null
                    binding.countTextInputLayout.isErrorEnabled = false
                }
            }

            if (it == DetailScanFields.CELL_RECEIVER) {
                //fieldValueIsNotCorrect = binding.cellReceiverTextView.text.isNullOrBlank()
                fieldValueIsNotCorrect =
                    tempScanRecord.cellReceiverGuid.isBlank() || checkCellReceiver(
                        Cell(
                            title = tempScanRecord.cellTitle,
                            guid = tempScanRecord.cellGuid
                        )
                    )
                needShowCellReceiverDialog =
                    (fieldValueIsNotCorrect || isError || needShowCellReceiverDialog)
                if (needShowCellReceiverDialog) {
                    //binding.cellReceiverTextInputLayout.error = "Ячейка не отсканирована!"
                    return@forEach
                }
            }
        }

        if (needShowCellReceiverDialog && !isError) {
            if (cellReceiverDialogBinding == null) {
                binding.buttonApply.setText(R.string.next)
                cellReceiverDialogBinding = getCellReceiverDialogBinding()
                setTextInputElementProperties(
                    cellReceiverDialogBinding!!.cellReceiverTextView,
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout,
                    receivedCell = true
                )
                if (tempScanRecord.cellReceiverGuid == tempScanRecord.cellGuid)
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                        getString(R.string.to_cell_equal_from_cell)
                if (tempScanRecord.cellReceiverGuid.isBlank())
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                        getString((R.string.scan_the_cell))
//                val needOpenCellReceiverDialog = checkCellReceiver(
//                    Cell(
//                        title = tempScanRecord.cellTitle,
//                        guid = tempScanRecord.cellGuid
//                    )
//                )
//                if (needOpenCellReceiverDialog) {
                val cellReceiverDialog = DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_INPUT,
                    isCancelable = false,
                    customView = cellReceiverDialogBinding!!.root,
                    positiveButtonTitle = getString(R.string.save_text),
                )
                cellReceiverDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    ?.setOnClickListener {
                        binding.cellReceiverTextView.setText(
                            cellReceiverDialogBinding?.cellReceiverTextView?.text ?: ""
                        )
                        if (binding.cellReceiverTextView.text.isNullOrBlank())
                            binding.buttonApply.setText(R.string.next)
                        else
                            binding.buttonApply.setText(R.string.apply_text)

                        cellReceiverDialogBinding = null
                        cellReceiverDialog.dismiss()
                    }
            }
            //}
            return
        }

        if (isError) return

        viewModel.saveScanRecord(tempScanRecord.toScanRecord(), forceOverwrite)
        val args = Bundle()
        //args.putSerializable("userData", user1C)
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

    override fun onDestroyView() {
        //dialog?.dismiss()
        BarcodeScannerReceiver.setEnabled()
        closeAllDialogs()
        super.onDestroyView()
    }

    private fun closeAllDialogs() {
        DialogScreen.getDialog()?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.dismiss()
    }

}

class TempScanRecord {
    var id: Long = 0L
    var OperationId: Long = 0L
    var OperationTitle: String = ""
    var cellTitle: String = ""
    var cellGuid: String = ""
    var cellReceiverTitle: String = ""
    var cellReceiverGuid: String = ""
    var ItemTitle: String = ""
    var ItemGUID: String = ""
    var ItemMeasureOfUnitTitle: String = ""
    var ItemMeasureOfUnitGUID: String = ""
    var Count: Double = 0.0
    var totalCount: Double = 0.0
    var docCount: Double = 0.0
    var docTitle: String = ""
    var docGuid: String = ""
    var coefficient: Double = 0.0
    var qualityGuid: String = ""
    var qualityTitle: String = ""
    var WorkwearOrdinary: Boolean = false
    var WorkwearDisposable: Boolean = false
    var replacement: Boolean = false

    //    var DivisionId: Long = 0L
//    var DivisionOrganization: Long = 0L
//    var warehouseGuid: String = ""
    var PurposeOfUseTitle: String = ""
    var PurposeOfUse: String = ""

    //    var PhysicalPersonTitle: String = ""
//    var PhysicalPersonGUID: String = ""
    var docHeaders: DocumentHeaders = DocumentHeaders
    var OwnerGuid: String = ""
    var lastModified: Long = System.currentTimeMillis()
    var warehouseGuid: String = ""

    constructor(scanRecord: TableScan) {
        id = scanRecord.id
        OperationId = scanRecord.OperationId
        OperationTitle = scanRecord.OperationTitle
        cellTitle = scanRecord.cellTitle
        cellGuid = scanRecord.cellGuid
        cellReceiverTitle = scanRecord.cellReceiverTitle
        cellReceiverGuid = scanRecord.cellReceiverGuid
        ItemTitle = scanRecord.ItemTitle
        ItemGUID = scanRecord.ItemGUID
        ItemMeasureOfUnitTitle = scanRecord.ItemMeasureOfUnitTitle
        ItemMeasureOfUnitGUID = scanRecord.ItemMeasureOfUnitGUID
        Count = scanRecord.Count
        totalCount = scanRecord.totalCount
        docCount = scanRecord.docCount
        docTitle = scanRecord.docTitle
        docGuid = scanRecord.docGuid
        coefficient = scanRecord.coefficient
        qualityGuid = scanRecord.qualityGuid
        qualityTitle = scanRecord.qualityTitle
        WorkwearOrdinary = scanRecord.WorkwearOrdinary
        WorkwearDisposable = scanRecord.WorkwearDisposable
//        DivisionId = scanRecord.DivisionId
//        DivisionOrganization = scanRecord.DivisionOrganization
//        warehouseGuid = scanRecord.warehouseGuid
        PurposeOfUseTitle = scanRecord.PurposeOfUseTitle
        PurposeOfUse = scanRecord.PurposeOfUse
//        PhysicalPersonTitle = scanRecord.PhysicalPersonTitle
//        PhysicalPersonGUID = scanRecord.PhysicalPersonGUID
        docHeaders = scanRecord.docHeaders
        OwnerGuid = scanRecord.OwnerGuid
        replacement = scanRecord.replacement
        lastModified = scanRecord.lastModified
        warehouseGuid = scanRecord.warehouseGuid
    }

    fun toScanRecord(): TableScan {
        return TableScan(
            id = id,
            OperationId = OperationId,
            OperationTitle = OperationTitle,
            cellTitle = cellTitle,
            cellGuid = cellGuid,
            cellReceiverTitle = cellReceiverTitle,
            cellReceiverGuid = cellReceiverGuid,
            ItemTitle = ItemTitle,
            ItemGUID = ItemGUID,
            ItemMeasureOfUnitTitle = ItemMeasureOfUnitTitle,
            ItemMeasureOfUnitGUID = ItemMeasureOfUnitGUID,
            Count = Count,
            totalCount = totalCount,
            docCount = docCount,
            docTitle = docTitle,
            docGuid = docGuid,
            coefficient = coefficient,
            qualityGuid = qualityGuid,
            qualityTitle = qualityTitle,
            WorkwearOrdinary = WorkwearOrdinary,
            WorkwearDisposable = WorkwearDisposable,
//            DivisionId = DivisionId,
//            DivisionOrganization = DivisionOrganization,
//            warehouseGuid = warehouseGuid,
            PurposeOfUseTitle = PurposeOfUseTitle,
            PurposeOfUse = PurposeOfUse,
//            PhysicalPersonTitle = PhysicalPersonTitle,
//            PhysicalPersonGUID = PhysicalPersonGUID,
            docHeaders = DocumentHeaders,
            OwnerGuid = OwnerGuid,
            replacement = replacement,
            lastModified = lastModified,
            warehouseGuid = warehouseGuid,
        )
    }
}


