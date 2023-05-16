package ru.tn.shinglass.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import ru.tn.shinglass.R
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

    private var dialog: AlertDialog? = null
    private var cellReceiverDialogBinding: CellReceiverDialogBinding? = null
    private lateinit var currentRecord: TableScan

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
            if (user1C.getUserGUID() == "") findNavController().navigate(R.id.authFragment)
        }

        var barcode = arguments?.getString("barcode")
        var barcodeType = arguments?.getString("barcodeType")
        var editRecord = arguments?.getSerializable("editRecord") as TableScan


        if (selectedOption.docType == DocType.TOIR_REQUIREMENT_INVOICE) {
            tableFromExternalDocument =
                tableScanViewModel.getAllScanRecordsByOwner(user1C.getUserGUID(), selectedOption.id)
        }

        forceOverwrite = (editRecord.id != 0L) //|| (selectedOption.docType == DocType.TOIR_REPAIR_ESTIMATE && editRecord.ItemGUID.isEmpty())

        //BarcodeScannerReceiver.clearData()

        var tempScanRecord = TempScanRecord(editRecord)
        tempScanRecord.OperationId = selectedOption.id
        tempScanRecord.OperationTitle = selectedOption.docType?.title ?: ""

        if (selectedOption.docType == DocType.BETWEEN_CELLS) {
            binding.cellTextInputLayout.hint = getString(R.string.header_cell)
            if (tempScanRecord.cellReceiverGuid.isBlank())
                binding.buttonApply.setText(R.string.next)
        }

        val detailScanFields = selectedOption.subOption?.detailScanFields

        with(binding) {
            operationTitleTextView.text = selectedOption.docType?.title ?: ""
//            //divisionTextView.setText("Подразделение из настроек")
            //if (detailScanFields?.contains(DetailScanFields.CELL) == true)
            if (detailScanFields != null) {
                val rootView = binding.root
                for (field in detailScanFields) {
                    if (field.fieldType == "TextInputLayout")
                        rootView.findViewById<TextInputLayout>(field.viewId)?.visibility =
                            View.VISIBLE
//                    if (field.viewId == R.id.cellReceiverTextInputLayout)
//                        binding.clearCellReceiverBtn.visibility = View.VISIBLE
                    if (field.fieldType == "CheckBox")
                        rootView.findViewById<CheckBox>(field.viewId)?.visibility = View.VISIBLE
                }
            }

            cellReceiverTextInputLayout.setEndIconOnClickListener {
                cellReceiverTextView.setText("")
                binding.buttonApply.setText(R.string.next)
            }
//            clearCellReceiverBtn.setOnClickListener {
//                cellReceiverTextView.setText("")
//                binding.buttonApply.setText(R.string.next)
//            }

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

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) { dataScanPair ->

            binding.buttonApply.clearFocus()

            //val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcodeFromScanner = dataScanPair.first
            val dataScanBarcode =
                if (dataScanBarcodeFromScanner == "") barcode
                    ?: "" else dataScanBarcodeFromScanner
            barcode = ""

            val dataScanBarcodeTypeFromScanner = dataScanPair.second
            val dataScanBarcodeType = if (dataScanBarcodeTypeFromScanner == "") barcodeType
                ?: "" else dataScanBarcodeTypeFromScanner
            barcodeType = ""


            if (dataScanBarcode == "") return@observe

            if (cellReceiverDialogBinding == null) dialog?.show()

            if (dataScanBarcodeType == "Code 128" || dataScanBarcodeType == "QR Code") {
                retrofitViewModel.getCellByBarcode(
                    dataScanBarcode,
                    editRecord.docHeaders.getWarehouse()?.warehouseGuid ?: ""
                )
                if (cellReceiverDialogBinding != null)
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error = null
                else
                    binding.cellTextInputLayout.error = null
                return@observe
            } else {
                if (binding.cellTextView.text.isNullOrBlank()) {
                    binding.cellTextInputLayout.error = "Отсканируйте ячейку!"
                    return@observe
                }
                if (cellReceiverDialogBinding != null) {
                    if (cellReceiverDialogBinding!!.cellReceiverTextView.text.isNullOrBlank()) {
                        cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                            "Отсканируйте ячейку!"
                        return@observe
                    }
                }
                if (cellReceiverDialogBinding == null)
                    retrofitViewModel.getItemByBarcode(dataScanBarcode)
            }
        }

        retrofitViewModel.cellData.observe(viewLifecycleOwner) {

            if (it == null) return@observe
            if (it.guid.isNullOrBlank()) {
                val warehouse = editRecord.docHeaders.getWarehouse()
                DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                    title = "Ячейка не найдена!",
                    message = if (warehouse == null) "Не задан склад." else "Проверьте, что ячейка принадлежит складу \"${warehouse.warehouseTitle}\" и отсканируйте ШК снова.",
                    positiveButtonTitle = "Ok",
                )
                if (cellReceiverDialogBinding != null) {
                    cellReceiverDialogBinding!!.cellReceiverTextView.setText("")
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                        "Ячейка не задана"
                    tempScanRecord.cellReceiverGuid = ""
                    tempScanRecord.cellReceiverTitle = ""
                    return@observe
                } else {
                    binding.cellTextView.setText("")
                    binding.cellTextInputLayout.error = "Ячейка не задана"
                    tempScanRecord.cellGuid = ""
                    tempScanRecord.cellTitle = ""
                    return@observe
                }
            }
            dialog?.dismiss()
            if (cellReceiverDialogBinding != null) {
                if (it.guid == tempScanRecord.cellGuid) {
                    cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error =
                        "Ячейка Куда не может совпадать с ячейкой Откуда"
                    cellReceiverDialogBinding!!.cellReceiverTextView.setText("")
                    return@observe
                }
                cellReceiverDialogBinding!!.cellReceiverTextView.setText(it.title)
                cellReceiverDialogBinding!!.cellReceiverTextInputLayout.error = null
                tempScanRecord.cellReceiverGuid = it.guid
                tempScanRecord.cellReceiverTitle = it.title
            } else {
                binding.cellTextView.setText(it.title)
                binding.cellTextInputLayout.error = null
                tempScanRecord.cellGuid = it.guid
                tempScanRecord.cellTitle = it.title
            }
        }


        retrofitViewModel.itemData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            dialog?.dismiss()
            if (it.itemGuid.isNullOrBlank()) {
                dialog = DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                    title = getString(R.string.nomenclature_not_found_text),
                    message = getString(R.string.check_label_scan_again_text),
                    positiveButtonTitle = "Ok"
                )
                return@observe
            }

            var newCount = try {
                (binding.countEditText.text.toString()).toDouble() + 1.0
            } catch (e: Exception) {
                1.0
            }
            //Если собираем по внешнему документу
            if (tableFromExternalDocument.isNotEmpty()) {
                //Если отбираем на основании документа из 1С и это редактирование записи, проверяем что отсканировали номенклатуру позиции, которую изменяем - они должны совпадать, иначе попытка подмены - отменяем.
                if (tempScanRecord.ItemGUID != "") {
                    if (it.itemGuid != tempScanRecord.ItemGUID) {
                        dialog = DialogScreen.getDialog(
                            requireContext(),
                            DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                            title = getString(R.string.changing_an_entry_text),
                            message = getString(R.string.nomenclature_substitution_text),
                            positiveButtonTitle = "Ok"
                        )
                        return@observe
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
                    dialog = DialogScreen.getDialog(
                        requireContext(),
                        DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                        title = getString(R.string.nomenclature_not_found_text),
                        message = "${getString(R.string.check_nomenclature_and_measure_of_unit_scan_again_text)}\n(ед. изм. ШК: ${it.unitOfMeasurementTitle})",
                        positiveButtonTitle = "Ok"
                    )
                    return@observe
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

            binding.itemTextInputLayout.error = null

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
            dialog?.dismiss()
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

        retrofitViewModel.requestError.observe(viewLifecycleOwner) { error ->

            if (error == null) return@observe

            dialog?.dismiss()
            DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_ERROR,
                error.message,
                onDialogsInteractionListener = object : OnDialogsInteractionListener {
                    override fun onPositiveClickButton() {
                        when (error.requestName) {
                            "getPhysicalPersonList" -> {
                                dialog?.show()
                                getPhysicalPersonList()
                            }
                            "getAllWarehousesList" -> {
                                dialog?.show()
                                getAllWarehousesList(binding)
                            }
                            "getCellByBarcode" -> {
                                //retrofitViewModel.getCellByBarcode()
                            }

                        }
                    }
                })
        }

        retrofitViewModel.dataState.observe(viewLifecycleOwner) {
            if (it.loading) {
                if (dialog?.isShowing == false || dialog == null)
                    dialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else
                dialog?.dismiss()

            if (it.error) {
                DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getPhysicalPersonList" -> {
                                    dialog?.show()
                                    getPhysicalPersonList()
                                }
                                "getAllWarehousesList" -> {
                                    dialog?.show()
                                    getAllWarehousesList(binding)
                                }

                            }
                        }
                    })
            }
        }

        return binding.root
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

        val detailScanFields = selectedOption.subOption?.detailScanFields
        if (detailScanFields?.isEmpty() == true) isError = true

        detailScanFields?.forEach {
            if (it == DetailScanFields.CELL) {
                fieldValueIsNotCorrect = binding.cellTextView.text.isNullOrBlank()
                isError = (fieldValueIsNotCorrect || isError)
                if (isError) {
                    binding.cellTextInputLayout.error = "Ячейка не отсканирована!"
                    return@forEach
                }
            }

            if (it == DetailScanFields.ITEM) {
                fieldValueIsNotCorrect = binding.itemTextView.text.isNullOrBlank()
                isError = (fieldValueIsNotCorrect || isError)
                if (isError) {
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
                if (isError) {
                    if (fieldValueIsNotCorrect)
                        binding.countTextInputLayout.error = "Количество не может быть нулевым!"
                    return@forEach
                } else {
                    binding.countTextInputLayout.error = null
                }
            }

            if (it == DetailScanFields.CELL_RECEIVER) {
                fieldValueIsNotCorrect = binding.cellReceiverTextView.text.isNullOrBlank()
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
                val cellReceiverDialog = DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_INPUT,
                    isCancelable = false,
                    customView = cellReceiverDialogBinding!!.root,
                    positiveButtonTitle = getString(R.string.save_text),
                )
                cellReceiverDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
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

    override fun onDestroy() {
        dialog?.dismiss()
        super.onDestroy()
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

    //    var DivisionId: Long = 0L
//    var DivisionOrganization: Long = 0L
//    var warehouseGuid: String = ""
    var PurposeOfUseTitle: String = ""
    var PurposeOfUse: String = ""

    //    var PhysicalPersonTitle: String = ""
//    var PhysicalPersonGUID: String = ""
    var docHeaders: DocumentHeaders = DocumentHeaders
    var OwnerGuid: String = ""

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
            OwnerGuid = OwnerGuid
        )
    }
}


