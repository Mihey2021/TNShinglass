package ru.tn.shinglass.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.AndroidUtils
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.databinding.FragmentTableScanBinding
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.OnTableScanItemInteractionListener
import ru.tn.shinglass.adapters.TableScanAdapter
import ru.tn.shinglass.databinding.DocumentsHeadersInitDialogBinding
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.HeaderFields
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel
import java.text.SimpleDateFormat
import java.util.*

class TableScanFragment : Fragment() {

    private val viewModel: TableScanFragmentViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val dataListWarehouses: ArrayList<Warehouse> = arrayListOf()
    private val dataListPhysicalPersons: ArrayList<PhysicalPerson> = arrayListOf()
    private val dataListDivisions: ArrayList<Division> = arrayListOf()
    private val dataListCounterparties: ArrayList<Counterparty> = arrayListOf()

    private val requiredField = arrayListOf<HeaderFields>()

    private lateinit var selectedOption: Option
    private lateinit var user1C: User1C

    //private lateinit var itemList: List<TableScan>
    private lateinit var dlgBinding: DocumentsHeadersInitDialogBinding
    private var dlgHeaders: AlertDialog? = null

    private var itemList: List<TableScan> = listOf()

    private var progressDialog: AlertDialog? = null


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

        val layoutInflater =
            LayoutInflater.from(requireContext())//.inflate(R.layout.inventory_init_dialog, null)
        dlgBinding = DocumentsHeadersInitDialogBinding.inflate(layoutInflater)

        selectedOption = arguments?.getSerializable("selectedOption") as Option
        setFragmentResult("requestSelectedOption", bundleOf("selectedOption" to selectedOption))
        user1C = arguments?.getSerializable("userData") as User1C
        setFragmentResult("requestUserData", bundleOf("userData" to user1C))

        binding.infoTextView.text = getString(R.string.info_table_scan_start_text)
//        binding.infoTextView.setOnClickListener {
//            dlgHeaders = getDocumentHeadersDialog(forceOpen = true, tableIsEmpty = itemList.isEmpty())
//        }
        binding.documentDetailsImageButton.setOnClickListener {
            dlgHeaders =
                getDocumentHeadersDialog(forceOpen = true, tableIsEmpty = itemList.isEmpty())
        }

        val adapter = TableScanAdapter(object : OnTableScanItemInteractionListener {
            override fun selectItem(item: TableScan) {

                DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_QUESTION,
                    "Что Вы хотите сделать?",
                    positiveButtonTitle = getString(R.string.edit_record_text),
                    negativeButtonTitle = getString(R.string.delete_text),
                    isCancelable = true,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            val args = Bundle()
                            args.putSerializable("userData", user1C)
                            args.putSerializable("selectedOption", selectedOption)
                            args.putString("itemBarCode", "")
                            args.putSerializable("editRecord", item)
                            findNavController().navigate(
                                R.id.action_tableScanFragment_to_detailScanFragment,
                                args
                            )
                        }

                        override fun onNegativeClickButton() {
                            viewModel.deleteRecordById(item)
                        }
                    })
            }
        })

        binding.list.adapter = adapter

        //getAllWarehousesList()
        //getPhysicalPersonList()

        if (DocumentHeaders.getWarehouse() == null)
            viewModel.getAllWarehousesList()
        if (DocumentHeaders.getPhysicalPerson() == null)
            viewModel.getAllPhysicalPerson()

        binding.completeAndSendBtn.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (selectedOption.option == OptionType.INVENTORY || selectedOption.option == OptionType.ACCEPTANCE) {
                        createDocumentIn1C()
//                        retrofitViewModel.createInventoryOfGoods(itemList) //primary = Первичная инвент. (поиск в ТЧ 1С идет без учета ячейки, т.к. в ячейках там еще ничего нет)
//                        progressDialog =
//                            DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    }
                    false
                }
                else -> false
            }
        }


        viewModel.refreshTableScan(user1C.getUserGUID(), selectedOption.id)

        viewModel.docCreated.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            //progressDialog?.dismiss()
            //Toast.makeText(requireContext(),"Документ отправлен!", Toast.LENGTH_LONG).show()
            DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_SUCCESS,
                "Документ в 1С успешно создан.\nНомер: ${it.docNumber}\nДетали:${if (it.details.isNotEmpty()) "\n${it.details}" else "[нет]"}",
                it.docTitle
            )
            viewModel.resetTheDocumentCreatedFlag()
            //viewModel.deleteRecordsByOwnerAndOperationId(user1C.getUserGUID(), selectedOption.id)
            //viewModel.updateRecordUpload(user1C.getUserGUID(), selectedOption.id)
            viewModel.refreshTableScan(user1C.getUserGUID(), selectedOption.id)
        }

        viewModel.data.observe(viewLifecycleOwner) {
            binding.completeAndSendBtn.isEnabled = it.isNotEmpty()
            adapter.submitList(it)
            itemList = it

            if (itemList.isNotEmpty()) {
                binding.infoTextView.text =
                    "${getString(R.string.info_table_scan_continue_text)} ${itemList[0].cellTitle}"
            } else {
                binding.infoTextView.text = getString(R.string.info_table_scan_start_text)
            }
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
                            "createInventoryOfGoods" -> {
//                                progressDialog?.show()
//                                getPhysicalPersonList()
                            }
                        }
                    }
                })
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) { dataScanPair ->

            //val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcode = dataScanPair.first
            val dataScanBarcodeType = dataScanPair.second

            if (dataScanBarcode == "") return@observe

            val args = Bundle()
            args.putSerializable("userData", user1C)
            args.putSerializable("selectedOption", selectedOption)
            args.putString("barcode", dataScanBarcode)
            args.putString("barcodeType", dataScanBarcodeType)

            BarcodeScannerReceiver.clearData()

            if (itemList.isEmpty()) {
                //if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null) {
                if (checkHeadersDataFail()) {
                    var showingHeadersDialog = true
                    if (dlgHeaders != null)
                        showingHeadersDialog = !(dlgHeaders?.isShowing ?: false)
                    if (showingHeadersDialog) {
                        dlgHeaders = getDocumentHeadersDialog(args)
                    }
                } else {
                    openDetailScanFragment(args)
                }
            } else {
                //val lastIndex = itemList.count() - 1
                //binding.infoTextView.text = "Чтобы начать, отсканируйте ячейку или номенклатуру.\nТекущая ячейка: ${itemList[lastIndex].cellTitle}"
                args.putSerializable(
                    "editRecord",
                    TableScan(
                        cellGuid = itemList[0].cellGuid,
                        cellTitle = itemList[0].cellTitle,
                        docHeaders = itemList[0].docHeaders,
//                        warehouseGuid = itemList[0].warehouseGuid,
//                        PhysicalPersonGUID = itemList[0].PhysicalPersonGUID,
//                        PhysicalPersonTitle = itemList[0].PhysicalPersonTitle,
                        OwnerGuid = user1C.getUserGUID()
                    )
                )
                findNavController().navigate(
                    R.id.action_tableScanFragment_to_detailScanFragment,
                    args
                )
            }
        }

//        retrofitViewModel.listDataWarehouses.observe(viewLifecycleOwner) {
//            if (it.isEmpty()) return@observe
//
//            viewModel.saveWarehouses(it)
//            setWarehousesAdapter(dlgBinding)
//        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            if (it.loading) {
                if (progressDialog?.isShowing == false || progressDialog == null)
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else
                progressDialog?.dismiss()

            if (it.error) {
                //DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR, title = it.errorMessage)
                progressDialog?.dismiss()
                DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getAllWarehousesList" -> {
                                    viewModel.getAllWarehousesList()
                                }
                                "getAllPhysicalPerson" -> {
                                    viewModel.getAllPhysicalPerson()
                                }
                                "createDocumentIn1C" ->
                                    createDocumentIn1C()
                                "getCounterpartiesList" -> viewModel.getCounterpartiesList(
                                    dlgBinding.counterpartyTextEdit.text.toString()
                                )
                            }
                        }
                    })
            }
        }

        viewModel.divisionsList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            it.forEach { division ->
                dataListDivisions.add(division)
            }
        }

        viewModel.warehousesList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            it.forEach { warehouse ->
                dataListWarehouses.add(warehouse)
            }
        }

        viewModel.physicalPersons.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            it.forEach { person ->
                dataListPhysicalPersons.add(person)
            }
        }

        viewModel.counterpartiesList.observe(viewLifecycleOwner) {
            dataListCounterparties.clear()
            it.forEach { counterparty ->
                dataListCounterparties.add(counterparty)
            }

            val adapter = DynamicListAdapter<Counterparty>(
                requireContext(),
                R.layout.counterparty_item_layout,
                dataListCounterparties
            )
            (dlgBinding.counterpartyTextEdit as? AutoCompleteTextView)?.setAdapter(adapter)
            if ((dlgHeaders?.isShowing == true) && dataListCounterparties.isEmpty())
                DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_SUCCESS,
                    getString(R.string.specify_name_or_inn_text),
                    getString(R.string.nothing_found_text),
                    titleIcon = R.drawable.ic_baseline_search_off_24
                )
            else
                dlgBinding.counterpartyTextEdit.showDropDown()
        }

        return binding.root
    }

    private fun createDocumentIn1C() {
        viewModel.createDocumentIn1C(itemList, selectedOption.docType!!)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDocumentHeadersDialog(
        args: Bundle? = null,
        cancellable: Boolean = true,
        forceOpen: Boolean = false,
        tableIsEmpty: Boolean = false,
    ): AlertDialog? {
        val layoutInflater =
            LayoutInflater.from(requireContext())//.inflate(R.layout.inventory_init_dialog, null)
        dlgBinding = DocumentsHeadersInitDialogBinding.inflate(layoutInflater)


        val docHeadersFields = selectedOption.subOption?.headerFields
        if (docHeadersFields?.isEmpty() == true) return null

        with(dlgBinding) {

            if (docHeadersFields?.contains(HeaderFields.DIVISION) == true) {
                val divisionGuidByPrefs =
                    settingsViewModel.getPreferenceByKey("division_guid")
                if (divisionGuidByPrefs.isNullOrBlank())
                    divisionTextInputLayout.error = "Подразделение не задано в настройках!"

                val divisionAdapter = DynamicListAdapter<Division>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListDivisions
                )

                divisionTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) divisionTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                divisionTextView.setAdapter(divisionAdapter)

                val division = settingsViewModel.getDivisionByGuid(divisionGuidByPrefs ?: "")
                if (DocumentHeaders.getDivision() == null)
                    DocumentHeaders.setDivision(division)
                divisionTextView.setText(DocumentHeaders.getDivision()?.divisionTitle)
                divisionTextInputLayout.error = null

                divisionTextView.setOnClickListener {
                    if (divisionTextView.adapter == null) {
                        viewModel.getAllDivisions()
                    }
                }

                divisionTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val divisionItem = adapterView.getItemAtPosition(position) as Division
                    DocumentHeaders.setDivision(divisionItem)
//                    fillResponsible(
//                        DocumentHeaders.getDivision()?.responsibleGuid ?: "",
//                        dlgBinding.physicalPersonTextView
//                    )
                    divisionTextView.setText(divisionItem?.divisionTitle)
                    divisionTextInputLayout.error = null
                    AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                divisionTextInputLayout.isVisible = false
            }

            if (docHeadersFields?.contains(HeaderFields.WAREHOUSE) == true) {
                val warehouseGuidByPrefs =
                    settingsViewModel.getPreferenceByKey("warehouse_guid")
                if (warehouseGuidByPrefs.isNullOrBlank())
                    warehouseTextInputLayout.error = "Склад не задан в настройках!".toString()

                val warehousesAdapter = DynamicListAdapter<Warehouse>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListWarehouses
                )

                warehouseTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) warehouseTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                warehouseTextView.setAdapter(warehousesAdapter)

                val warehouse = settingsViewModel.getWarehouseByGuid(warehouseGuidByPrefs ?: "")
                if (DocumentHeaders.getWarehouse() == null)
                    DocumentHeaders.setWarehouse(warehouse)
                warehouseTextView.setText(DocumentHeaders.getWarehouse()?.warehouseTitle)
                warehouseTextInputLayout.error = null

                warehouseTextView.setOnClickListener {
                    if (warehouseTextView.adapter == null) {
                        viewModel.getAllWarehousesList()
                    }
                }

                warehouseTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val warehouseItem = adapterView.getItemAtPosition(position) as Warehouse
                    DocumentHeaders.setWarehouse(warehouseItem)
                    //if (physicalPersonTextView.text.isNullOrBlank()) {
                    fillResponsible(
                        DocumentHeaders.getWarehouse()?.warehouseResponsibleGuid ?: "",
                        dlgBinding.physicalPersonTextView
                    )
                    //}
                    warehouseTextView.setText(warehouseItem?.warehouseTitle)
                    warehouseTextInputLayout.error = null
                    AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                warehouseTextInputLayout.isVisible = false
            }

            if (docHeadersFields?.contains(HeaderFields.PHYSICAL_PERSON) == true) {
                val physicalPersonAdapter = DynamicListAdapter<PhysicalPerson>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListPhysicalPersons
                )

                physicalPersonTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) physicalPersonTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                physicalPersonTextView.setAdapter(physicalPersonAdapter)

                physicalPersonTextInputLayout.hint = "МОЛ"
                if (DocumentHeaders.getPhysicalPerson() != null)
                    physicalPersonTextView.setText(DocumentHeaders.getPhysicalPerson()?.physicalPersonFio)
                physicalPersonTextView.setOnClickListener {
                    if (physicalPersonTextView.adapter == null) {
                        //getPhysicalPersonList()
                        viewModel.getAllPhysicalPerson()
//                    progressDialog =
//                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    }
                }
                physicalPersonTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val physicalPerson = adapterView.getItemAtPosition(position) as PhysicalPerson
                    DocumentHeaders.setPhysicalPerson(physicalPerson)
                    physicalPersonTextView.setText(physicalPerson?.physicalPersonFio)
                    physicalPersonTextInputLayout.error = null
                    AndroidUtils.hideKeyboard(dlgBinding.root)
                }

                fillResponsible(
                    DocumentHeaders.getWarehouse()?.warehouseResponsibleGuid ?: "",
                    dlgBinding.physicalPersonTextView
                )
            } else {
                physicalPersonTextInputLayout.isVisible = false
            }

            if (docHeadersFields?.contains(HeaderFields.COUNTERPARTY) == false) {
                counterpartyTextInputLayout.isVisible = false
            } else {

                counterpartyTextInputLayout.isEnabled = itemList.isEmpty()
                val counterparty =
                    if (itemList.isNotEmpty()) itemList[0].docHeaders.getCounterparty() else null
                if (DocumentHeaders.getCounterparty() == null)
                    DocumentHeaders.setCounterparty(counterparty)
                counterpartyTextEdit.setText(DocumentHeaders.getCounterparty()?.title)
                counterpartyTextInputLayout.helperText =
                    getCounterpartyHelperText(DocumentHeaders.getCounterparty())

                counterpartyTextEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        counterpartyTextInputLayout.error = null
                    }

                    override fun afterTextChanged(p0: Editable?) {
                    }

                })

                counterpartyTextInputLayout.setEndIconOnClickListener {
                    AndroidUtils.hideKeyboard(counterpartyTextEdit)
                    if (counterpartyTextEdit.inputType != InputType.TYPE_NULL) {
                        viewModel.getCounterpartiesList(counterpartyTextEdit.text.toString())
                    } else {
                        DocumentHeaders.setCounterparty(null)
                        counterpartyTextEdit.text = null
                        counterpartyTextInputLayout.helperText = null
                        counterpartyTextEdit.inputType = InputType.TYPE_CLASS_TEXT
                        counterpartyTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        counterpartyTextInputLayout.endIconDrawable =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_search_24
                            )
                        counterpartyTextInputLayout.endIconContentDescription =
                            getString(R.string.find_text)
                    }
                }

//                counterpartyTextEdit.setOnKeyListener { _, keyCode, _ ->
//                    when (keyCode) {
//                        KeyEvent.KEYCODE_DEL -> {
//                            dataListCounterparties.clear()
//                            counterpartyTextInputLayout.helperText = null
//                        }
//                    }
//                    return@setOnKeyListener false
//                }
                counterpartyTextEdit.setOnItemClickListener { adapterView, _, position, _ ->
                    AndroidUtils.hideKeyboard(counterpartyTextEdit)
                    val counterparty = adapterView.getItemAtPosition(position) as Counterparty
                    DocumentHeaders.setCounterparty(counterparty)
                    counterpartyTextEdit.setText(counterparty?.title)
                    counterpartyTextEdit.inputType = InputType.TYPE_NULL
                    counterpartyTextInputLayout.endIconDrawable = AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_clear_24
                    )
                    counterpartyTextInputLayout.endIconContentDescription =
                        getString(R.string.clear_text)
                    counterpartyTextInputLayout.error = null
                    counterpartyTextInputLayout.helperText = getCounterpartyHelperText(counterparty)

                }
            }

            if (docHeadersFields?.contains(HeaderFields.INCOMING_DATE) == false) {
                incomingDateTextInputLayout.isVisible = false
            } else {
                incomingDateTextInputLayout.isEnabled = itemList.isEmpty()
                val incomingDate =
                    if (itemList.isNotEmpty()) itemList[0].docHeaders.getIncomingDate() else null
                if (DocumentHeaders.getIncomingDate() == null)
                    DocumentHeaders.setIncomingDate(incomingDate)
                val userDateAlready = DocumentHeaders.getIncomingDate() != null
                val userDate =
                    if (userDateAlready) DocumentHeaders.getIncomingDate() else MaterialDatePicker.todayInUtcMilliseconds()
                val sdf = SimpleDateFormat("dd.MM.yyyy")
                incomingDateEditText.setText(if (userDateAlready) sdf.format(Date(userDate!!)) else null)
                incomingDateTextInputLayout.setEndIconOnClickListener {
                    if (incomingDateTextInputLayout.endIconContentDescription == getString(R.string.clear_text)) {
                        DocumentHeaders.setIncomingDate(null)
                        incomingDateEditText.text = null
                        incomingDateTextInputLayout.endIconDrawable =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_calendar_24
                            )
                        incomingDateTextInputLayout.endIconContentDescription =
                            getString(R.string.incoming_date_text)
                        return@setEndIconOnClickListener
                    }
                    val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText(R.string.incoming_date_text)
                        .setSelection(userDate)
                        .build()
                    datePicker.show(requireActivity().supportFragmentManager, "datePicker")
                    datePicker.addOnPositiveButtonClickListener {
                        DocumentHeaders.setIncomingDate(it)
                        incomingDateEditText.setText(sdf.format(Date(it)))
                        incomingDateTextInputLayout.endIconDrawable =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_clear_24
                            )
                        incomingDateTextInputLayout.endIconContentDescription =
                            getString(R.string.clear_text)
                    }
                }
            }

            if (docHeadersFields?.contains(HeaderFields.INCOMING_NUMBER) == false) {
                incomingNumberTextInputLayout.isVisible = false
            } else {
                incomingNumberTextInputLayout.isEnabled = itemList.isEmpty()
                val incomingNumber =
                    if (itemList.isNotEmpty()) itemList[0].docHeaders.getIncomingNumber() else ""
                if (DocumentHeaders.getIncomingNumber() == "")
                    DocumentHeaders.setIncomingNumber(incomingNumber)
                incomingNumberEditText.setText(DocumentHeaders.getIncomingNumber())
                incomingNumberEditText.addTextChangedListener(
                    object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            DocumentHeaders.setIncomingNumber(s.toString())
                        }

                        override fun afterTextChanged(s: Editable?) {
//                            if (s != null && s.isNotEmpty()) {
//
//                            }
                        }
                    }
                )
            }

        }


        var dialogHeadersNeedOpen = checkHeadersDataFail()

        //if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null || forceOpen) {
        if (dialogHeadersNeedOpen || forceOpen) {
            dlgHeaders = DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_INPUT,
                isCancelable = cancellable,
                customView = dlgBinding.root,
                positiveButtonTitle = getString(R.string.save_text),
//                onDialogsInteractionListener = object : OnDialogsInteractionListener {
//                    override fun onPositiveClickButton() {
////                        if (DocumentHeaders.getWarehouse() == null)
////                            dlgBinding.warehouseTextInputLayout.error = "Склад не заполнен!"
////                        if (DocumentHeaders.getPhysicalPerson() == null)
////                            dlgBinding.physicalPersonTextInputLayout.error = "МОЛ не указан!"
////                        if (DocumentHeaders.getWarehouse() != null && DocumentHeaders.getPhysicalPerson() != null)
//                        if (!checkHeadersDataFail()) {
//                            if (args != null)
//                                openDetailScanFragment(args)
//                        }
//                    }
//                }

            )

//            if (dlg != null) {
//                //requireContext().display.getCurrentSizeRange(w, Point())
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    val bounds = requireActivity().windowManager.currentWindowMetrics.bounds
//                    dlg?.window?.setLayout(bounds.width(), bounds.height())
//                } else {
//                    val width = requireActivity().windowManager.defaultDisplay.width
//                    val height = requireActivity().windowManager.defaultDisplay.height
//                    dlg?.window?.setLayout(width, height)
//                }
//            }
        }

        dlgHeaders?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            if (!checkHeadersDataFail()) {
                if (args != null) {
                    openDetailScanFragment(args)
                } else {
                    dlgHeaders?.dismiss()
                }
            }
        }

        return dlgHeaders
    }

    private fun getCounterpartyHelperText(counterparty: Counterparty?): CharSequence? {
        return if (counterparty == null) null else "ИНН: ${counterparty.inn}. КПП: ${counterparty.kpp}"
    }

    //true  - не все заполнено
    //false - все заполнено
    private fun checkHeadersDataFail(): Boolean {
        var isNotCorrect = false
        val docHeadersFields = selectedOption.subOption?.headerFields
        if (docHeadersFields?.isEmpty() == true)
            return true

        docHeadersFields?.forEach {
            if (it == HeaderFields.DIVISION) {
                isNotCorrect = DocumentHeaders.getDivision() == null
                val divisionTextInputLayout =
                    dlgHeaders?.findViewById<TextInputLayout>(HeaderFields.DIVISION.viewId)
                if (isNotCorrect) {
                    divisionTextInputLayout?.error = getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                divisionTextInputLayout?.error = null
            }
            if (it == HeaderFields.WAREHOUSE) {
                isNotCorrect = DocumentHeaders.getWarehouse() == null
                val warehouseTextInputLayout =
                    dlgHeaders?.findViewById<TextInputLayout>(HeaderFields.WAREHOUSE.viewId)
                if (isNotCorrect) {
                    warehouseTextInputLayout?.error = getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                warehouseTextInputLayout?.error = null
            }
            if (it == HeaderFields.PHYSICAL_PERSON) {
                isNotCorrect = DocumentHeaders.getPhysicalPerson() == null
                val physicalPersonTextInputLayout =
                    dlgHeaders?.findViewById<TextInputLayout>(HeaderFields.PHYSICAL_PERSON.viewId)
                if (isNotCorrect) {
                    physicalPersonTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                physicalPersonTextInputLayout?.error = null
            }
            if (it == HeaderFields.COUNTERPARTY) {
                isNotCorrect = DocumentHeaders.getCounterparty() == null
                val counterpartyTextInputLayout =
                    dlgHeaders?.findViewById<TextInputLayout>(HeaderFields.COUNTERPARTY.viewId)
                if (isNotCorrect) {
                    counterpartyTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                counterpartyTextInputLayout?.error = null
            }
        }
        return isNotCorrect
    }

    private fun fillResponsible(
        responsibleGuid: String,
        physicalPersonTextView: AutoCompleteTextView
    ) {
        val physicalPerson = viewModel.getPhysicalPersonByGuid(responsibleGuid)
        DocumentHeaders.setPhysicalPerson(physicalPerson)
        physicalPersonTextView.setText(physicalPerson?.physicalPersonFio ?: "")
    }


    private fun openDetailScanFragment(args: Bundle) {
        //if (DocumentHeaders.getWarehouse() != null && DocumentHeaders.getPhysicalPerson() != null) {
        if (!checkHeadersDataFail()) {
//            val warehouseGuid = DocumentHeaders.getWarehouse()?.warehouseGuid.toString()
//            val physicalPersonGuid =
//                DocumentHeaders.getPhysicalPerson()?.physicalPersonGuid.toString()
//            val physicalPersonTitle =
//                DocumentHeaders.getPhysicalPerson()?.physicalPersonFio.toString()
            args.putSerializable(
                "editRecord",
                TableScan(
                    OwnerGuid = user1C.getUserGUID(),
//                    warehouseGuid = warehouseGuid,
//                    PhysicalPersonGUID = physicalPersonGuid,
//                    PhysicalPersonTitle = physicalPersonTitle
                    docHeaders = DocumentHeaders
                )
            )
            dlgHeaders?.dismiss()
            findNavController().navigate(
                R.id.action_tableScanFragment_to_detailScanFragment,
                args
            )
        }
    }

    override fun onResume() {
        viewModel.refreshTableScan(user1C.getUserGUID(), selectedOption.id)
        if (itemList.isEmpty()) {
//            if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null)
//                dlgHeaders = getDocumentHeadersDialog()
            if (checkHeadersDataFail() && (dlgHeaders?.isShowing == false))
                dlgHeaders = getDocumentHeadersDialog()
        }
        super.onResume()
    }

    override fun onDestroy() {
        if (itemList.isEmpty()) {
            DocumentHeaders.setWarehouse(null)
            DocumentHeaders.setPhysicalPerson(null)
            DocumentHeaders.setCounterparty(null)
            DocumentHeaders.setIncomingDate(null)
            DocumentHeaders.setIncomingNumber("")
        }
        super.onDestroy()
    }
}