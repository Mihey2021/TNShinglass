package ru.tn.shinglass.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AutoCompleteTextView
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
import ru.tn.shinglass.auth.AppAuth
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

const val IDM_OPEN = 1
const val IDM_DELETE = 2

class TableScanFragment : Fragment() {

    private val viewModel: TableScanFragmentViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val dataListWarehouses: ArrayList<Warehouse> = arrayListOf()
    private val dataListWarehousesReceiver: ArrayList<WarehouseReceiver> = arrayListOf()
    private val dataListPhysicalPersons: ArrayList<PhysicalPerson> = arrayListOf()
    private val dataListEmployees: ArrayList<Employee> = arrayListOf()
    private val dataListDivisions: ArrayList<Division> = arrayListOf()
    private val dataListCounterparties: ArrayList<Counterparty> = arrayListOf()

    private lateinit var selectedOption: Option
    private lateinit var user1C: User1C
    //private var user1C: User1C? = null


    //private lateinit var itemList: List<TableScan>
    private lateinit var dlgBinding: DocumentsHeadersInitDialogBinding
    private var dlgHeadersAndOther: AlertDialog? = null

    private var itemList: List<TableScan> = listOf()

    private var dialog: AlertDialog? = null

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
        binding.documentDetailsImageButton.setOnClickListener {
            dlgHeadersAndOther =
                getDocumentHeadersDialog(forceOpen = true)
        }

        val adapter = TableScanAdapter(
            object : OnTableScanItemInteractionListener {
                override fun selectItem(item: TableScan) {
                    //if (!isExternalDocument) {
                    if (item.isGroup) return
                    dlgHeadersAndOther = DialogScreen.getDialog(
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
                                    viewModel.saveRecord(
                                        item.copy(
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
                            dialog?.dismiss()
                            dialog = DialogScreen.getDialog(
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
                        dialog?.dismiss()
                        dialog = DialogScreen.getDialog(
                            requireContext(),
                            DialogScreen.IDD_QUESTION,
                            message = "Отправить данные в 1С?",
                            title = getString(R.string.document_1c_text),
                            positiveButtonTitle = getString(R.string.text_yes),
                            onDialogsInteractionListener = object :
                                OnDialogsInteractionListener {
                                override fun onPositiveClickButton() {
                                    createDocumentIn1C()
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
            dialog?.dismiss()
//            dialog =
            DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_SUCCESS,
                "Документ в 1С успешно создан.\nНомер: ${it.docNumber}\nДетали:${if (it.details.isNotEmpty()) "\n${it.details}" else "[нет]"}",
                it.docTitle
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

            dialog?.dismiss()
            dialog = DialogScreen.getDialog(
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


            val showingDialog = (dlgHeadersAndOther?.isShowing ?: false)
            //if (dlgHeadersAndOther != null)

            if (showingDialog) return@observe

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

                    if (!showingDialog) {
                        dlgHeadersAndOther = getDocumentHeadersDialog(args)
                        return@observe
                    }
                } else {
                    openDetailScanFragment(args)
                    return@observe
                }
            } else {
                //val lastIndex = itemList.count() - 1
                //binding.infoTextView.text = "Чтобы начать, отсканируйте ячейку или номенклатуру.\nТекущая ячейка: ${itemList[lastIndex].cellTitle}"
                if (thisOnePositionAndEmpty(itemList)) {
                    if (checkHeadersDataFail()) {
                        if (!showingDialog) {
                            dlgHeadersAndOther = getDocumentHeadersDialog(args)
                            return@observe
                        }
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
                    openDetailScanFragment(args, itemList)
                    return@observe
                }
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


//        retrofitViewModel.listDataWarehouses.observe(viewLifecycleOwner) {
//            if (it.isEmpty()) return@observe
//
//            viewModel.saveWarehouses(it)
//            setWarehousesAdapter(dlgBinding)
//        }

        viewModel.dataState.observe(viewLifecycleOwner)
        {
            if (it.loading) {
                if (dialog?.isShowing == false || dialog == null)
                    dialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else
                dialog?.dismiss()

            if (it.error) {
                //DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR, title = it.errorMessage)
                dialog?.dismiss()
                dialog = DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getAllWarehousesList" -> {
                                    viewModel.getAllWarehousesList()
                                }
                                "getAllWarehousesReceiverList" -> {
                                    viewModel.getAllWarehousesList(receiver = true)
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

        viewModel.divisionsList.observe(viewLifecycleOwner)
        {
            if (it.isEmpty()) return@observe

            dataListDivisions.clear()
            dataListDivisions.add(Division(getString(R.string.not_chosen_text), "", ""))

            it.forEach { division ->
                dataListDivisions.add(division)
            }
        }

        viewModel.warehousesList.observe(viewLifecycleOwner)
        {
            if (it.isEmpty()) return@observe

            dataListWarehouses.clear()
            dataListWarehouses.add(Warehouse(getString(R.string.not_chosen_text), "", "", ""))

            it.forEach { warehouse ->
                dataListWarehouses.add(warehouse)
            }
        }

        viewModel.warehousesReceiverList.observe(viewLifecycleOwner)
        {
            if (it.isEmpty()) return@observe

            dataListWarehousesReceiver.clear()
            dataListWarehousesReceiver.add(
                WarehouseReceiver(
                    getString(R.string.not_chosen_text),
                    "",
                    "",
                    ""
                )
            )

            it.forEach { warehouseReceiver ->
                dataListWarehousesReceiver.add(warehouseReceiver)
            }
        }

        viewModel.physicalPersons.observe(viewLifecycleOwner)
        {
            if (it.isEmpty()) return@observe

            dataListPhysicalPersons.clear()
            dataListPhysicalPersons.add(PhysicalPerson(getString(R.string.not_chosen_text), ""))

            it.forEach { person ->
                dataListPhysicalPersons.add(person)
            }
        }

        viewModel.employees.observe(viewLifecycleOwner)
        {
            if (it.isEmpty()) return@observe

            dataListEmployees.clear()
            dataListEmployees.add(Employee(getString(R.string.not_chosen_text), ""))

            it.forEach { employee ->
                dataListEmployees.add(employee)
            }
        }

        viewModel.counterpartiesList.observe(viewLifecycleOwner)
        {
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
            if ((dlgHeadersAndOther?.isShowing == true) && dataListCounterparties.isEmpty()) {
                dialog?.dismiss()
                dialog = DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_SUCCESS,
                    getString(R.string.specify_name_or_inn_text),
                    getString(R.string.nothing_found_text),
                    titleIcon = R.drawable.ic_baseline_search_off_24
                )
            } else {
                dlgBinding.counterpartyTextEdit.showDropDown()
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
                    dialog?.dismiss()
                    dialog = DialogScreen.getDialog(requireContext(), DialogScreen.IDD_QUESTION,
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
        //progressDialog?.dismiss()
        //progressDialog?.dismiss()
        findNavController().navigate(
            R.id.action_tableScanFragment_to_documentSelectFragment,
            args
        )

    }

    private fun createDocumentIn1C() {
        dialog = DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
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

        with(dlgBinding) {

            if (docHeadersFields?.contains(HeaderFields.DIVISION) == true) {
                val divisionGuidByPrefs =
                    settingsViewModel.getPreferenceByKey<String>("division_guid", "")
                if (divisionGuidByPrefs.isNullOrBlank())
                    divisionTextInputLayout.error = "Подразделение не задано в настройках!"

                val divisionAdapter = DynamicListAdapter<Division>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListDivisions,
                    filterOff = true
                )

                divisionTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) divisionTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                divisionTextView.setAdapter(divisionAdapter)



                if (!isExternalDocument) {
                    var division = settingsViewModel.getDivisionByGuid(divisionGuidByPrefs ?: "")
                    if (division == null)
                        division = viewModel.getDivisionByGuid(user1C.getDefaultDivisionGUID())
                    if (DocumentHeaders.getDivision() == null) {
                        DocumentHeaders.setDivision(division)
                        val position = divisionAdapter.getPosition(division)
                        if (position != -1)
                        //divisionTextView.setSelection(position)
                            divisionTextView.listSelection = position
                    }
                }
                divisionTextView.setText(DocumentHeaders.getDivision()?.divisionTitle)
                divisionTextInputLayout.error = null

                divisionTextView.setOnClickListener {
                    if (divisionTextView.adapter == null || divisionTextView.adapter.count == 0) {
                        viewModel.getAllDivisions()
                    }
                }

                divisionTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val divisionItem = adapterView.getItemAtPosition(position) as Division
                    if (divisionItem.divisionGuid == "") {
                        DocumentHeaders.setDivision(null)
                    } else {
                        DocumentHeaders.setDivision(divisionItem)
                    }


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
                    settingsViewModel.getPreferenceByKey<String>("warehouse_guid", "")
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

                if (!isExternalDocument) {
                    val warehouse = settingsViewModel.getWarehouseByGuid(warehouseGuidByPrefs ?: "")
                    if (DocumentHeaders.getWarehouse() == null)
                        DocumentHeaders.setWarehouse(warehouse)
                }
                warehouseTextView.setText(DocumentHeaders.getWarehouse()?.warehouseTitle)
                warehouseTextInputLayout.error = null

                warehouseTextView.setOnClickListener {
                    if (warehouseTextView.adapter == null || warehouseTextView.adapter.count == 0) {
                        viewModel.getAllWarehousesList()
                    }
                }

                warehouseTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val warehouseItem = adapterView.getItemAtPosition(position) as Warehouse
                    if (warehouseItem.warehouseGuid == "") {
                        DocumentHeaders.setWarehouse(null)
                    } else {
                        DocumentHeaders.setWarehouse(warehouseItem)
                    }
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

            if (docHeadersFields?.contains(HeaderFields.WAREHOUSE_RECEIVER) == true) {
                val warehousesReceiverAdapter = DynamicListAdapter<WarehouseReceiver>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListWarehousesReceiver
                )

                warehouseReceiverTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) warehouseReceiverTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                warehouseReceiverTextView.setAdapter(warehousesReceiverAdapter)

                warehouseReceiverTextView.setText(DocumentHeaders.getWarehouseReceiver()?.warehouseReceiverTitle)
                warehouseReceiverTextInputLayout.error = null

                warehouseReceiverTextView.setOnClickListener {
                    if (warehouseReceiverTextView.adapter == null || warehouseReceiverTextView.adapter.count == 0) {
                        viewModel.getAllWarehousesList(receiver = true)
                    }
                }

                warehouseReceiverTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val warehouseReceiverItem =
                        adapterView.getItemAtPosition(position) as WarehouseReceiver
                    if (warehouseReceiverItem.warehouseReceiverGuid == "") {
                        DocumentHeaders.setWarehouseReceiver(null)
                    } else {
                        DocumentHeaders.setWarehouseReceiver(warehouseReceiverItem)
                    }
                    warehouseReceiverTextView.setText(warehouseReceiverItem?.warehouseReceiverTitle)
                    warehouseReceiverTextInputLayout.error = null
                    AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                warehouseReceiverTextInputLayout.isVisible = false
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
                    if (physicalPersonTextView.adapter == null || physicalPersonTextView.adapter.count == 0) {
                        //getPhysicalPersonList()
                        viewModel.getAllPhysicalPerson()
//                    progressDialog =
//                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    }
                }
                physicalPersonTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val physicalPerson = adapterView.getItemAtPosition(position) as PhysicalPerson
                    if (physicalPerson.physicalPersonGuid == "") {
                        DocumentHeaders.setPhysicalPerson(null)
                    } else {
                        DocumentHeaders.setPhysicalPerson(physicalPerson)
                    }
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

            if (docHeadersFields?.contains(HeaderFields.EMPLOYEE) == true) {
                val employeeAdapter = DynamicListAdapter<Employee>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListEmployees
                )

                employeeTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) employeeTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                employeeTextView.setAdapter(employeeAdapter)

                if (DocumentHeaders.getEmployee() != null)
                    employeeTextView.setText(DocumentHeaders.getEmployee()?.employeeFio)
                employeeTextView.setOnClickListener {
                    if (employeeTextView.adapter == null || employeeTextView.adapter.count == 0) {
                        //getPhysicalPersonList()
                        viewModel.getAllEmployees()
//                    progressDialog =
//                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    }
                }
                employeeTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val employee = adapterView.getItemAtPosition(position) as Employee
                    if (employee.employeeGuid == "") {
                        DocumentHeaders.setEmployee(null)
                    } else {
                        DocumentHeaders.setEmployee(employee)
                    }
                    employeeTextView.setText(employee?.employeeFio)
                    employeeTextInputLayout.error = null
                    AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                employeeTextInputLayout.isVisible = false
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
                        counterpartyTextInputLayout.helperText = null
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
            dlgHeadersAndOther = DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_INPUT,
                isCancelable = cancellable,
                customView = dlgBinding.root,
                positiveButtonTitle = getString(R.string.save_text),
            )
        }

        dlgHeadersAndOther?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
            if (!checkHeadersDataFail()) {
                if (args != null) {
                    openDetailScanFragment(args)
                } else {
                    dlgHeadersAndOther?.dismiss()
                }
            }
        }

        return dlgHeadersAndOther
    }

    private fun getCounterpartyHelperText(counterparty: Counterparty?): CharSequence? {
        return if (counterparty == null) null else "ИНН: ${counterparty.inn}. КПП: ${counterparty.kpp}"
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
                val divisionTextInputLayout =
                    dlgHeadersAndOther?.findViewById<TextInputLayout>(HeaderFields.DIVISION.viewId)
                if (fieldValueIsNotCorrect) {
                    divisionTextInputLayout?.error = getString(R.string.field_must_be_filled_text)
                    return@forEach
                    //return isNotCorrect
                }
                divisionTextInputLayout?.error = null
            }
            if (it == HeaderFields.WAREHOUSE) {
                fieldValueIsNotCorrect = DocumentHeaders.getWarehouse() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val warehouseTextInputLayout =
                    dlgHeadersAndOther?.findViewById<TextInputLayout>(HeaderFields.WAREHOUSE.viewId)
                if (fieldValueIsNotCorrect) {
                    warehouseTextInputLayout?.error = getString(R.string.field_must_be_filled_text)
                    return@forEach
                    //return isNotCorrect
                }
                warehouseTextInputLayout?.error = null
            }
            if (it == HeaderFields.WAREHOUSE_RECEIVER) {
                fieldValueIsNotCorrect = DocumentHeaders.getWarehouseReceiver() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val warehouseReceiverTextInputLayout =
                    dlgHeadersAndOther?.findViewById<TextInputLayout>(HeaderFields.WAREHOUSE_RECEIVER.viewId)
                if (fieldValueIsNotCorrect) {
                    warehouseReceiverTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                    //return isNotCorrect
                }
                warehouseReceiverTextInputLayout?.error = null
            }
            if (it == HeaderFields.PHYSICAL_PERSON) {
                fieldValueIsNotCorrect = DocumentHeaders.getPhysicalPerson() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val physicalPersonTextInputLayout =
                    dlgHeadersAndOther?.findViewById<TextInputLayout>(HeaderFields.PHYSICAL_PERSON.viewId)
                if (fieldValueIsNotCorrect) {
                    physicalPersonTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                    //return isNotCorrect
                }
                physicalPersonTextInputLayout?.error = null
            }
            if (it == HeaderFields.EMPLOYEE) {
                fieldValueIsNotCorrect = DocumentHeaders.getEmployee() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val employeeTextInputLayout =
                    dlgHeadersAndOther?.findViewById<TextInputLayout>(HeaderFields.EMPLOYEE.viewId)
                if (fieldValueIsNotCorrect) {
                    employeeTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                    //return isNotCorrect
                }
                employeeTextInputLayout?.error = null
            }
            if (it == HeaderFields.COUNTERPARTY) {
                fieldValueIsNotCorrect = DocumentHeaders.getCounterparty() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val counterpartyTextInputLayout =
                    dlgHeadersAndOther?.findViewById<TextInputLayout>(HeaderFields.COUNTERPARTY.viewId)
                if (fieldValueIsNotCorrect) {
                    counterpartyTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                    //return isNotCorrect
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
            dlgHeadersAndOther?.dismiss()
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
                dlgHeadersAndOther = getDocumentHeadersDialog()
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

        dlgHeadersAndOther?.dismiss()
        //dialog?.dismiss()

        super.onDestroy()
    }


}