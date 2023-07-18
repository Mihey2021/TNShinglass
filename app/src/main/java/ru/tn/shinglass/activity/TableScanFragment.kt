package ru.tn.shinglass.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.AndroidUtils
import ru.tn.shinglass.activity.utilites.SoundPlayer
import ru.tn.shinglass.activity.utilites.SoundType
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.HeadersDialogFragment
import ru.tn.shinglass.activity.utilites.dialogs.HeadersDialogFragmentDirections
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.databinding.FragmentTableScanBinding
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.OnTableScanItemInteractionListener
import ru.tn.shinglass.adapters.TableScanAdapter
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.databinding.DocumentsHeadersInitDialogBinding
import ru.tn.shinglass.dto.models.DocHeaders
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.HeaderFields
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel
import java.text.SimpleDateFormat
import java.util.*

const val IDM_OPEN = 1
const val IDM_DELETE = 2

class TableScanFragment : Fragment() {

    private val viewModel: TableScanFragmentViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var selectedOption: Option
    private lateinit var user1C: User1C


    private lateinit var dlgBinding: DocumentsHeadersInitDialogBinding
    private var dlgHeadersAndOther: AlertDialog? = null

    private var itemList: List<TableScan> = listOf()

    private var currentDialog: AlertDialog? = null

    private var isExternalDocument: Boolean = false

    private var refreshing: Boolean = false

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentTableScanBinding.inflate(inflater, container, false)

        val layoutInflater =
            LayoutInflater.from(requireContext())//.inflate(R.layout.inventory_init_dialog, null)
        dlgBinding = DocumentsHeadersInitDialogBinding.inflate(layoutInflater)

        selectedOption = arguments?.getSerializable("selectedOption") as Option
        setFragmentResult("requestSelectedOption", bundleOf("selectedOption" to selectedOption))
//        user1C = arguments?.getSerializable("userData") as User1C
//        setFragmentResult("requestUserData", bundleOf("userData" to user1C))

        user1C = AppAuth.getInstance().getAuthData()
        AppAuth.getInstance().authStateFlow.observe(viewLifecycleOwner) { authState ->
            user1C = authState.user1C
            if (user1C.getUserGUID().isEmpty()) findNavController().navigate(R.id.authFragment)
        }

        viewModel.reloadTableScan(user1C.getUserGUID(), selectedOption.id)

        isExternalDocument =
            (selectedOption.docType == DocType.TOIR_REQUIREMENT_INVOICE) || (selectedOption.docType == DocType.TOIR_REPAIR_ESTIMATE)

//        with(binding) {
//            headerTitleTextView.setTextColor(Color.BLACK)
//            headerTotalCount.setTextColor(Color.BLACK)
//            //headerTotalCount.text = if (isExternalDocument) "${getString(R.string.total_text)}\n${getString(R.string.selected_and_by_document_text)}" else "${getString(R.string.total_text)}"
//            headerTotalCount.text = getString(R.string.total_text)
//            headerCountTextView.setTextColor(Color.BLACK)
//            headerUnitOfMeasureTextView.setTextColor(Color.BLACK)
//            headerCellTextView.setTextColor(Color.BLACK)
//        }

        if (selectedOption.docType == DocType.BETWEEN_CELLS) {
            binding.tableScanHeaders.headerCellTextView.setText(getString(R.string.header_cell_receiver))
        }

        if (!isExternalDocument) {
            if (selectedOption.subOption?.headerFields?.contains(HeaderFields.WAREHOUSE) == true) {
                if (DocumentHeaders.getWarehouse() == null)
                    viewModel.getAllWarehousesList()
            }
            if (selectedOption.subOption?.headerFields?.contains(HeaderFields.WAREHOUSE_RECEIVER) == true) {
                if (DocumentHeaders.getWarehouseReceiver() == null)
                    viewModel.getAllWarehousesList(receiver = true)
            }
            if (selectedOption.subOption?.headerFields?.contains(HeaderFields.PHYSICAL_PERSON) == true) {
                if (DocumentHeaders.getPhysicalPerson() == null)
                    viewModel.getAllPhysicalPerson()
            }
            if (selectedOption.subOption?.headerFields?.contains(HeaderFields.EMPLOYEE) == true) {
                if (DocumentHeaders.getEmployee() == null) {
                    viewModel.getAllEmployees()
                }
            }
        }

        //if (selectedOption.docType == DocType.TOIR_REQUIREMENT_INVOICE) {
        if (isExternalDocument) {
            if (viewModel.getAllScanRecordsByOwner(user1C.getUserGUID(), selectedOption.id)
                    .isEmpty() && !DocumentHeaders.getExternalDocumentSelected()
            ) {
                openExternalDocumentFragment()
            } else {
                binding.externalDocumentTextView.visibility = View.VISIBLE

            }
        }

        binding.externalDocumentTextView.setOnClickListener {
            openExternalDocumentFragment(itemList.isEmpty())
        }

        registerForContextMenu(binding.externalDocumentTextView)

        binding.infoTextView.text = getString(R.string.info_table_scan_start_text)
//        binding.infoTextView.setOnClickListener {
//            dlgHeaders = getDocumentHeadersDialog(forceOpen = true, tableIsEmpty = itemList.isEmpty())
//        }
        binding.documentDetailsButton.setOnClickListener {
            getDocumentHeadersDialog(forceOpen = true)
        }

        val adapter = TableScanAdapter(
            object : OnTableScanItemInteractionListener {
                override fun selectItem(item: TableScan) {
                    //if (!isExternalDocument) {
                    if (item.isGroup) return
                    dlgHeadersAndOther = DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_QUESTION,
                        "Что Вы хотите сделать?",
                        positiveButtonTitle = getString(R.string.edit_record_text),
                        negativeButtonTitle = getString(R.string.delete_text),
                        isCancelable = true,
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                refreshing = false
                                openDetailScanForChangeItem(item)
                            }

                            override fun onNegativeClickButton() {
                                refreshing = true
                                if (itemList.filter { filterRecord -> filterRecord.ItemGUID == item.ItemGUID && filterRecord.ItemMeasureOfUnitGUID == item.ItemMeasureOfUnitGUID }.size == 1 && isExternalDocument) {
                                    val isToirRepairEstimate =
                                        selectedOption.docType == DocType.TOIR_REPAIR_ESTIMATE //Это смета
                                    viewModel.saveRecord(
                                        item.copy(
                                            ItemGUID = if (isToirRepairEstimate) "" else item.ItemGUID,
                                            ItemTitle = if (isToirRepairEstimate) "" else item.ItemTitle,
                                            ItemMeasureOfUnitGUID = if (isToirRepairEstimate) "" else item.ItemMeasureOfUnitGUID,
                                            ItemMeasureOfUnitTitle = if (isToirRepairEstimate) "" else item.ItemMeasureOfUnitTitle,
                                            cellGuid = "",
                                            cellTitle = "",
                                            Count = 0.0,
                                            totalCount = 0.0
                                        ), true
                                    )
                                    //binding.list.adapter?.notifyDataSetChanged()
                                    viewModel.reloadTableScan(
                                        user1C.getUserGUID(),
                                        selectedOption.id
                                    )
                                } else {
                                    val isGroup = item.isGroup
                                    viewModel.deleteRecordById(item)
//                                if (isGroup)
//                                    binding.list.adapter?.notifyDataSetChanged()
                                    viewModel.reloadTableScan(
                                        user1C.getUserGUID(),
                                        selectedOption.id
                                    )
                                }
//                            val tmpAdapter = binding.list.adapter
//                            val tmpLayoutManager = binding.list.layoutManager
//                            binding.list.adapter = null
//                            binding.list.layoutManager = null
//                            binding.list.adapter = tmpAdapter
//                            binding.list.layoutManager = tmpLayoutManager
//                            binding.list.adapter?.notifyDataSetChanged()
                            }
                        })
//                } else {
//                    openDetailScanForChangeItem(item)
//                }
                }
            },
            isExternalDocument = isExternalDocument,
            isExternalDocumentDetail = false,
            emptyCellText = getString(R.string.not_scanned_yet)
        )

        binding.list.adapter = adapter

        //getAllWarehousesList()
        //getPhysicalPersonList()

//        else {
//            if(itemList.isNotEmpty()) {
//                val docHeaders = itemList[0].docHeaders
//                DocumentHeaders.setDivision(docHeaders.getDivision())
//
//            }
//        }

        binding.completeAndSendBtn.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (selectedOption.docType == DocType.DISPOSABLE_PPE) {
                        if (settingsViewModel.getPreferenceByKey<String>(
                                "virtual_cell_guid",
                                ""
                            ) == ""
                        ) {
                            currentDialog?.dismiss()
                            currentDialog = DialogScreen.showDialog(
                                requireContext(),
                                DialogScreen.IDD_QUESTION,
                                message = "В настройках не установлена виртуальная ячейка. Выгрузка документа невозможна",
                                title = getString(R.string.header_virtual_cell),
                                positiveButtonTitle = getString(R.string.settings_header),
                                onDialogsInteractionListener = object :
                                    OnDialogsInteractionListener {
                                    override fun onPositiveClickButton() {
                                        val intent =
                                            Intent(requireContext(), SettingsActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                            )
                            return@setOnTouchListener false
                        }
                    }



                    if (selectedOption.option == OptionType.INVENTORY || selectedOption.option == OptionType.ACCEPTANCE
                        || selectedOption.option == OptionType.SELECTION || selectedOption.option == OptionType.MOVEMENTS
                    ) {
                        currentDialog?.dismiss()
                        BarcodeScannerReceiver.setEnabled(false)
                        currentDialog = DialogScreen.showDialog(
                            requireContext(),
                            DialogScreen.IDD_QUESTION,
                            message = "Отправить данные в 1С?",
                            title = getString(R.string.document_1c_text),
                            positiveButtonTitle = getString(R.string.text_yes),
                            onDialogsInteractionListener = object :
                                OnDialogsInteractionListener {
                                override fun onPositiveClickButton() {
                                    BarcodeScannerReceiver.setEnabled()
                                    createDocumentIn1C()
                                }

                                override fun onNegativeClickButton() {
                                    BarcodeScannerReceiver.setEnabled()
                                    super.onNegativeClickButton()
                                }
                            }
                        )
//                        retrofitViewModel.createInventoryOfGoods(itemList) //primary = Первичная инвент. (поиск в ТЧ 1С идет без учета ячейки, т.к. в ячейках там еще ничего нет)
                        //progressDialog =
                        //DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    }
                    false
                }
                else -> false
            }
        }

        viewModel.docCreated.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            //progressDialog?.dismiss()
            //Toast.makeText(requireContext(),"Документ отправлен!", Toast.LENGTH_LONG).show()
            currentDialog?.dismiss()
            //currentDialog =
            BarcodeScannerReceiver.setEnabled(false)
            DialogScreen.showDialog(
                requireContext(),
                DialogScreen.IDD_SUCCESS,
                "Документ в 1С успешно создан.\nНомер: ${it.docNumber}\nДетали:${if (it.details.isNotEmpty()) "\n${it.details}" else "[нет]"}",
                it.docTitle,
                onDialogsInteractionListener = object : OnDialogsInteractionListener {
                    override fun onPositiveClickButton() {
                        BarcodeScannerReceiver.setEnabled()
                        super.onPositiveClickButton()
                    }
                }
            )
            viewModel.resetTheDocumentCreatedFlag()
            //viewModel.deleteRecordsByOwnerAndOperationId(user1C.getUserGUID(), selectedOption.id)
            //viewModel.updateRecordUpload(user1C.getUserGUID(), selectedOption.id)
            viewModel.reloadTableScan(user1C.getUserGUID(), selectedOption.id)
        }

        viewModel.data.observe(viewLifecycleOwner) {
            //if (selectedOption.docType == DocType.TOIR_REQUIREMENT_INVOICE) {
            if (isExternalDocument && (selectedOption.docType != DocType.TOIR_REPAIR_ESTIMATE)) {
                binding.completeAndSendBtn.isEnabled =
                    checkScanTableForExternalDocument(it) && itemList.isNotEmpty()
            } else {
                binding.completeAndSendBtn.isEnabled = it.isNotEmpty()
                if (it.isEmpty()) {
                    if (checkHeadersDataFail() && selectedOption.docType == DocType.DISPOSABLE_PPE) {
                        DocumentHeaders.setDivision(null)
                        //getDocumentHeadersDialog()
                        return@observe
                    }
                }
            }


//            val list = it.map { record ->
//                record.copy(
//                    totalCount =
//                    viewModel.getTotalCount(
//                        ownerGuid = record.OwnerGuid,
//                        operationId = record.OperationId,
//                        itemGUID = record.ItemGUID,
//                        itemMeasureOfUnitGUID = record.ItemMeasureOfUnitGUID
//                    )
//                )
//            }

            val list = mutableListOf<TableScan>()
            val groups = it.filter { groupRecord -> groupRecord.isGroup }

            groups.forEach { groupRecord ->
                list.add(groupRecord.copy(refreshing = refreshing))
                list.add(groupRecord.copy(isGroup = false, refreshing = refreshing))
                list.addAll(it.filter { filterRecord ->
                    filterRecord.OperationId == groupRecord.OperationId
                            && filterRecord.OwnerGuid == groupRecord.OwnerGuid
                            && filterRecord.uploaded == groupRecord.uploaded
                            && filterRecord.docHeaders == groupRecord.docHeaders
                            && filterRecord.ItemGUID == groupRecord.ItemGUID
                            && filterRecord.ItemMeasureOfUnitGUID == groupRecord.ItemMeasureOfUnitGUID
                            && filterRecord.docGuid == groupRecord.docGuid
                            && !filterRecord.isGroup
                }.map { listRecord ->
                    listRecord.copy(
                        totalCount = groupRecord.totalCount,
                        refreshing = refreshing
                    )
                }
                )
            }

            //adapter.submitList(it)
            if (isExternalDocument) {
                if (list.isNotEmpty()) {
                    if (list[0].ItemGUID.isNotEmpty()) {
                        adapter.submitList(list)
                    } else {
                        adapter.submitList(mutableListOf<TableScan>())
                    }
                } else {
                    adapter.submitList(list)
                }
            } else {
                adapter.submitList(list)
            }
            //refreshing = false

            itemList = it

            if (itemList.isNotEmpty()) {
                binding.infoTextView.text =
                    "${getString(R.string.info_table_scan_continue_text)} ${if (itemList[0].cellReceiverGuid.isNullOrBlank()) itemList[0].cellTitle else itemList[0].cellReceiverTitle}"

                val externalDocumentTitle = itemList[0].docTitle
                if (externalDocumentTitle != "")
                    binding.externalDocumentTextView.text = externalDocumentTitle


            } else {
                binding.infoTextView.text = getString(R.string.info_table_scan_start_text)
                binding.externalDocumentTextView.text = getString(R.string.select_document)
            }
        }



        retrofitViewModel.requestError.observe(viewLifecycleOwner) { error ->

            if (error == null) return@observe

            currentDialog?.dismiss()
            currentDialog = DialogScreen.showDialog(
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
            if (!BarcodeScannerReceiver.isEnabled() || dataScanPair == Pair("", "")) return@observe
//            val showingDialog = (dlgHeadersAndOther?.isShowing ?: false)
//            //if (dlgHeadersAndOther != null)
//
//            if (showingDialog) {
//                SoundPlayer(requireContext(), SoundType.SMALL_ERROR).playSound()
//                return@observe
//            }

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

            //if (selectedOption.docType == DocType.TOIR_REQUIREMENT_INVOICE) {
            if (isExternalDocument) {
                if (itemList.isNotEmpty()) {
                    if (itemList[0].docTitle == "") {
                        openExternalDocumentFragment()
                        return@observe
                    }
                } else {
                    openExternalDocumentFragment()
                    return@observe
                }
            }

            if (itemList.isEmpty()) {
                //if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null) {
                if (checkHeadersDataFail()) {

                    //if (!showingDialog) {
                    getDocumentHeadersDialog(args)
                    return@observe
                    //}
                } else {
                    openDetailScanFragment(args)
                    return@observe
                }
            } else {
                //val lastIndex = itemList.count() - 1
                //binding.infoTextView.text = "Чтобы начать, отсканируйте ячейку или номенклатуру.\nТекущая ячейка: ${itemList[lastIndex].cellTitle}"
                if (thisOnePositionAndEmpty(itemList)) {
                    if (checkHeadersDataFail()) {
                        //if (!showingDialog) {
                        getDocumentHeadersDialog(args)
                        return@observe
                        //}
                    }
                    //openDetailScanFragment(args)
                    args.putSerializable(
                        "editRecord",
                        TableScan(
                            id = itemList[0].id,
                            docTitle = itemList[0].docTitle,
                            docGuid = itemList[0].docGuid,
                            cellGuid = itemList[0].cellGuid,
                            cellTitle = itemList[0].cellTitle,
                            docHeaders = itemList[0].docHeaders,
                            OwnerGuid = user1C.getUserGUID()
                        )
                    )
                    findNavController().navigate(
                        R.id.action_tableScanFragment_to_detailScanFragment,
                        args
                    )
                    return@observe
                } else {
                    if (isExternalDocument) {
                        openDetailScanFragment(args, itemList)
                        return@observe
                    }
                }
            }

            if (selectedOption.docType == DocType.DISPOSABLE_PPE && itemList.isEmpty()) {
                getDocumentHeadersDialog()
                return@observe
            }

            args.putSerializable(
                "editRecord",
                TableScan(
                    docTitle = itemList[0].docTitle,
                    docGuid = itemList[0].docGuid,
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

        viewModel.dataState.observe(viewLifecycleOwner)
        {
            if (it.loading) {
                if (DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.isShowing == false || DialogScreen.getDialog(
                        DialogScreen.IDD_PROGRESS
                    ) == null
                )
                    DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else {
                DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
            }

            if (it.error) {
                //DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR, title = it.errorMessage)
                DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "createDocumentIn1C" ->
                                    createDocumentIn1C()
                            }
                        }
                    })
            }
        }

        return binding.root
    }

    private fun thisOnePositionAndEmpty(itemList: List<TableScan>): Boolean {
        var isOnePositionAndEmpty = false
        if (itemList.isNotEmpty()) {
            if (itemList.count() == 1) {
                if (itemList[0].ItemGUID.isEmpty()) {
                    isOnePositionAndEmpty = true
                }
            }
        }
        return isOnePositionAndEmpty
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(Menu.NONE, IDM_OPEN, Menu.NONE, getString(R.string.show_selected_text))
        menu.add(Menu.NONE, IDM_DELETE, Menu.NONE, getString(R.string.choose_another_text))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            IDM_OPEN -> {
                openExternalDocumentFragment(itemList.isEmpty())
            }
            IDM_DELETE -> {
                if (itemList.isNotEmpty()) {
                    currentDialog?.dismiss()
                    currentDialog =
                        DialogScreen.showDialog(requireContext(), DialogScreen.IDD_QUESTION,
                            title = getString(R.string.choose_another_text),
                            message = "Текущие данные будут очищены.\nПродолжить?",
                            onDialogsInteractionListener = object : OnDialogsInteractionListener {
                                override fun onPositiveClickButton() {
                                    deleteCurrentExternalDocumentData()
                                }
                            }
                        )
                }
            }
            else -> return super.onContextItemSelected(item)
        }
        return true
    }

    private fun deleteCurrentExternalDocumentData() {
        viewModel.deleteRecordsByOwnerAndOperationId(
            user1C.getUserGUID(),
            selectedOption.id
        )
        openExternalDocumentFragment(itemList.isEmpty())
    }

    private fun openDetailScanForChangeItem(item: TableScan) {
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

    private fun checkScanTableForExternalDocument(items: List<TableScan>): Boolean {
        var isCorrect = true
        items.forEach { item ->
            val record = viewModel.getScanRecordById(item.id)
            val count = viewModel.getExistingRecordCountSum(record)
            if (record == null) {
                isCorrect = false
            } else {
                //if (record.Count != item.docCount)
                if (count != item.docCount)
                    isCorrect = false
            }
        }
        return isCorrect
    }

    private fun openExternalDocumentFragment(isNew: Boolean = true) {

        val args = Bundle()
        args.putSerializable("userData", user1C)
        args.putSerializable("selectedOption", selectedOption)
        args.putBoolean("isNew", isNew)

        findNavController().navigate(
            R.id.action_tableScanFragment_to_documentSelectFragment,
            args
        )

    }

    private fun createDocumentIn1C() {
        currentDialog?.dismiss()
        currentDialog = DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
        viewModel.createDocumentIn1C(
            itemList,
            selectedOption.docType!!,
            settingsViewModel.getPreferenceByKey<String>("virtual_cell_guid", "") ?: ""
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDocumentHeadersDialog(
        args: Bundle? = null,
        cancellable: Boolean = true,
        forceOpen: Boolean = false,
    ): AlertDialog? {
        val layoutInflater =
            LayoutInflater.from(requireContext())//.inflate(R.layout.inventory_init_dialog, null)
        dlgBinding = DocumentsHeadersInitDialogBinding.inflate(layoutInflater)

        val tableIsEmpty = itemList.isEmpty()

        val isExternalDocument = selectedOption.docType == DocType.TOIR_REQUIREMENT_INVOICE

        val docHeadersFields = selectedOption.subOption?.headerFields
        if (docHeadersFields?.isEmpty() == true) return null

        var dialogHeadersNeedOpen = checkHeadersDataFail()

        dlgHeadersAndOther = null
        val direction = HeadersDialogFragmentDirections.actionGlobalHeadersDialogFragment(
            user1C = user1C,
            tableIsEmpty = tableIsEmpty,
            isExternalDocument = isExternalDocument,
            selectedOption = selectedOption,
            itemList = itemList.toTypedArray()
        ) //, headerFields = docHeadersFields!!, )
        findNavController().navigate(direction)
        return dlgHeadersAndOther
    }

    //true  - не все заполнено
    //false - все заполнено
    private fun checkHeadersDataFail(): Boolean {
        var isNotCorrect = false
        var fieldValueIsNotCorrect = false
        val docHeadersFields = selectedOption.subOption?.headerFields
        if (docHeadersFields?.isEmpty() == true)
            return true

        docHeadersFields?.forEach {
            if (it == HeaderFields.DIVISION) {
                fieldValueIsNotCorrect = DocumentHeaders.getDivision() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
            }
            if (it == HeaderFields.WAREHOUSE) {
                fieldValueIsNotCorrect = DocumentHeaders.getWarehouse() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
            }
            if (it == HeaderFields.WAREHOUSE_RECEIVER) {
                fieldValueIsNotCorrect = DocumentHeaders.getWarehouseReceiver() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
            }
            if (it == HeaderFields.PHYSICAL_PERSON) {
                fieldValueIsNotCorrect = DocumentHeaders.getPhysicalPerson() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
            }
            if (it == HeaderFields.EMPLOYEE) {
                fieldValueIsNotCorrect = DocumentHeaders.getEmployee() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
            }
            if (it == HeaderFields.COUNTERPARTY) {
                fieldValueIsNotCorrect = DocumentHeaders.getCounterparty() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
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


    private fun openDetailScanFragment(args: Bundle, itemList: List<TableScan>? = null) {
        //if (DocumentHeaders.getWarehouse() != null && DocumentHeaders.getPhysicalPerson() != null) {
        if (!checkHeadersDataFail()) {
//            val warehouseGuid = DocumentHeaders.getWarehouse()?.warehouseGuid.toString()
//            val physicalPersonGuid =
//                DocumentHeaders.getPhysicalPerson()?.physicalPersonGuid.toString()
//            val physicalPersonTitle =
//                DocumentHeaders.getPhysicalPerson()?.physicalPersonFio.toString()
            if (itemList != null) {
                args.putSerializable(
                    "editRecord",
                    TableScan(
                        //id = itemList[0].id,
                        docTitle = itemList[0].docTitle,
                        docGuid = itemList[0].docGuid,
                        OwnerGuid = user1C.getUserGUID(),
                        docHeaders = DocumentHeaders
                    )
                )
            } else {
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
            }
            //dlgHeadersAndOther?.dismiss()
            DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
            DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.dismiss()
            DialogScreen.getDialog()?.dismiss()
            findNavController().navigate(
                R.id.action_tableScanFragment_to_detailScanFragment,
                args
            )
        }
    }

    override fun onResume() {
        viewModel.reloadTableScan(user1C.getUserGUID(), selectedOption.id)
        if (itemList.isEmpty()) {
//            if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null)
//                dlgHeaders = getDocumentHeadersDialog()
            if (checkHeadersDataFail() && (dlgHeadersAndOther?.isShowing == false))
                getDocumentHeadersDialog()
        }
        super.onResume()
    }

    override fun onDestroy() {
        if (itemList.isEmpty()) {
            DocumentHeaders.setWarehouse(null)
            DocumentHeaders.setPhysicalPerson(null)
            DocumentHeaders.setEmployee(null)
            DocumentHeaders.setCounterparty(null)
            DocumentHeaders.setIncomingDate(null)
            DocumentHeaders.setIncomingNumber("")
        }

        closeAllDialogs()
        //dlgHeadersAndOther?.dismiss()
        //dialog?.dismiss()

        super.onDestroy()
    }

    private fun closeAllDialogs() {
        DialogScreen.getDialog()?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.dismiss()
        BarcodeScannerReceiver.setEnabled()
    }

    override fun onDestroyView() {
        currentDialog?.dismiss()
        dlgHeadersAndOther?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.dismiss()
        DialogScreen.getDialog()?.dismiss()
        super.onDestroyView()
    }

}