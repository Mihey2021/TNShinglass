package ru.tn.shinglass.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.OnTableScanItemInteractionListener
import ru.tn.shinglass.adapters.TableScanAdapter
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.databinding.FragmentDocumentSelectBinding
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.DocumentSelectFragmentViewModel
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel
import java.text.SimpleDateFormat

class DocumentSelectFragment : Fragment() {

    private val viewModel: DocumentSelectFragmentViewModel by viewModels()
    private val viewModelTableScan: TableScanFragmentViewModel by viewModels()

    private lateinit var selectedOption: Option
    private lateinit var user1C: User1C

    private var progressDialog: AlertDialog? = null
    private val externalDocumentList: ArrayList<ExternalDocument> = arrayListOf()
    private var isNew: Boolean = false
    private var needShowingProgressDialog = true

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//
//        super.onViewCreated(view, savedInstanceState)
//    }

    override fun onStart() {
        if (isNew) getExternalDocumentsList(selectedOption.docType)
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        AppAuth.getInstance().authStateFlow.observe(viewLifecycleOwner) { authState ->
            val authUser1C = authState.user1C
            if (authUser1C.getUserGUID().isEmpty()) findNavController().navigate(R.id.authFragment)
        }

        BarcodeScannerReceiver.setEnabled(false)
        selectedOption = arguments?.getSerializable("selectedOption") as Option
        user1C = arguments?.getSerializable("userData") as User1C
        isNew = arguments?.getBoolean("isNew") ?: false

        viewModelTableScan.reloadTableScan(user1C.getUserGUID(), selectedOption.id)

        //Заполним таблицу физ лиц, чтобы после выбора документа привязать ответственного склада
        viewModelTableScan.getAllPhysicalPerson()

        val binding = FragmentDocumentSelectBinding.inflate(inflater, container, false)

        val adapter = TableScanAdapter(
            object : OnTableScanItemInteractionListener {
                override fun selectItem(item: TableScan) {
                    super.selectItem(item)
                }
            },
            isExternalDocument = true,
            isExternalDocumentDetail = true,
            emptyCellText = getString(R.string.not_scanned_yet)
        )

        with(binding) {
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
            documentTextInputLayout.isEnabled = isNew
            backButton.text = getString(R.string.back_text)

            list.adapter = adapter
            needShowingProgressDialog = isNew
            if (isNew) {
                initAutoCompleteTextView(binding.documentTextView)
                //getExternalDocumentsList(selectedOption.docType)
                documentTextView.setText(
                    (documentTextView.adapter.getItem(0) as ExternalDocument).externalOrderDocumentTitle,
                    false
                );
                DocumentHeaders.setExternalDocumentSelected(true)
                documentTextView.setOnClickListener {
                    if (documentTextView.adapter == null || documentTextView.adapter.count == 0) {
                        //viewModel.getInternalOrderList()
                        getExternalDocumentsList(selectedOption.docType)
                    }
                }
                documentTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val documentItem = adapterView.getItemAtPosition(position) as ExternalDocument
                    if (isNew) viewModelTableScan.deleteRecordsByOwnerAndOperationId(
                        user1C.getUserGUID(),
                        selectedOption.id
                    )

                    if (documentItem.externalOrderDocumentGuid == "") {
                        binding.backButton.text = getString(R.string.back_text)
                        groupTableItems.visibility = View.GONE
                        DocumentHeaders.setExternalDocumentSelected(true)
                        DocumentHeaders.setDivision(null)
                        DocumentHeaders.setWarehouse(null)
                        DocumentHeaders.setPhysicalPerson(null)
                    } else {
                        binding.backButton.text = getString(R.string.select_text)
                        groupTableItems.visibility = View.VISIBLE
                        DocumentHeaders.setExternalDocumentSelected(true)
                        DocumentHeaders.setDivision(documentItem.externalOrderDivision)
                        binding.documentTextInputLayout.hint = getString(R.string.document_1c_text)
                        if (documentItem.externalDocumentItems != null)
                        //adapter.submitList(documentItem.externalDocumentItems)
                            if (documentItem.externalDocumentItems.isEmpty()) saveEmptyExternalDocument(
                                documentItem
                            )
                        documentItem.externalDocumentItems?.forEach { item ->
                            DocumentHeaders.setWarehouse(item.warehouse)
//                            val physicalPerson = viewModelTableScan.getPhysicalPersonByGuid(
//                                item.warehouse?.warehouseResponsibleGuid ?: ""
//                            )
                            val physicalPerson: PhysicalPerson? = null
                            DocumentHeaders.setPhysicalPerson(physicalPerson)
                            val sdf = SimpleDateFormat("dd.MM.yyyy")
                            viewModelTableScan.saveRecord(
                                TableScan(
                                    OperationId = selectedOption.id,
                                    OperationTitle = selectedOption.docType?.title ?: "",
                                    docTitle = "${documentItem.externalOrderDocumentTitle} ${documentItem.externalOrderNumber} от ${
                                        sdf.format(
                                            documentItem.externalOrderDate
                                        )
                                    }",
                                    docGuid = documentItem.externalOrderDocumentGuid,
                                    docCount = item.itemCount,
                                    Count = if (selectedOption.docType == DocType.TOIR_REPAIR_ESTIMATE) item.itemCount else 0.0, //для Сметы проставляем кол-во отобранного из документа
                                    coefficient = item.itemCoefficient,
                                    cellTitle = item.cellTitle,
                                    cellGuid = item.cellGuid,
                                    ItemTitle = item.itemTitle,
                                    ItemGUID = item.itemGUID,
                                    ItemMeasureOfUnitGUID = item.itemMeasureOfUnitGUID,
                                    ItemMeasureOfUnitTitle = item.itemMeasureOfUnitTitle,
                                    docHeaders = DocumentHeaders,
                                    OwnerGuid = user1C.getUserGUID(),
                                ), false
                            )
                        }
                        viewModelTableScan.reloadTableScan(user1C.getUserGUID(), selectedOption.id)
                    }
                }
            } else {
                groupTableItems.visibility = View.VISIBLE
            }

            viewModel.externalDocumentList.observe(viewLifecycleOwner) {
                if (it.isEmpty()) return@observe

                externalDocumentList.clear()
                addEmptyItemExternalDocumentList()

                it.forEach { internalOrderDocument ->
                    externalDocumentList.add(internalOrderDocument)
                }

                initAutoCompleteTextView(binding.documentTextView)

                //DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()

//                val internalOrderAdapter = DynamicListAdapter<ExternalDocument>(
//                    requireContext(),
//                    R.layout.dynamic_prefs_layout,
//                    internalOrderList,
//                    filterOff = true
//                )
//
//                binding.documentTextView.setAdapter(internalOrderAdapter)

            }

            viewModel.dataState.observe(viewLifecycleOwner) {
                //val progressDialog = DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)
                val progressDialog = getCurrentProgressDialog()
                if (it.loading) {
                    needShowingProgressDialog = true
                } else {
                    progressDialog.dismiss()
                    needShowingProgressDialog = false
                }


                if (it.error) {
                    //DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR, title = it.errorMessage)
                    progressDialog.dismiss()
                    DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_ERROR,
                        it.errorMessage,
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                when (it.requestName) {
                                    "getInternalOrderList" -> {
                                        DialogScreen.showDialog(
                                            requireContext(),
                                            DialogScreen.IDD_PROGRESS
                                        )
                                        viewModel.getInternalOrderList()
                                    }
                                    "getRepairEstimate" -> {
                                        DialogScreen.showDialog(
                                            requireContext(),
                                            DialogScreen.IDD_PROGRESS
                                        )
                                        viewModel.getRepairEstimate()
                                    }
                                }
                            }
                        }
                    )
                }
            }

            viewModelTableScan.data.observe(viewLifecycleOwner) {
                //adapter.submitList(it)
                val groups = it.filter { groupRecord -> groupRecord.isGroup }

//                val list = mutableListOf<TableScan>()
//                groups.forEach { groupRecord ->
//                    list.add(groupRecord)
//                    list.addAll(it.filter { filterRecord ->
//                        filterRecord.OperationId == groupRecord.OperationId
//                                && filterRecord.OwnerGuid == groupRecord.OwnerGuid
//                                && filterRecord.uploaded == groupRecord.uploaded
//                                && filterRecord.docHeaders == groupRecord.docHeaders
//                                && filterRecord.ItemGUID == groupRecord.ItemGUID
//                                && filterRecord.ItemMeasureOfUnitGUID == groupRecord.ItemMeasureOfUnitGUID
//                                && filterRecord.docGuid == groupRecord.docGuid
//                                && !filterRecord.isGroup
//                    }.map { listRecord -> listRecord.copy(totalCount = groupRecord.totalCount) }
//                    )
//                }

//                adapter.submitList(list)
                if (groups.isNotEmpty()) {
                    if (groups[0].ItemGUID.isNotEmpty()) {
                        adapter.submitList(groups)
                    }
                }



                if (it.isNotEmpty()) {
                    binding.documentTextView.setText(it[0].docTitle)
                    binding.documentTextInputLayout.hint = getString(R.string.document_1c_text)
                }
            }

            BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {
                if (it.first == "" && it.second == "") return@observe
                BarcodeScannerReceiver.clearData()
            }

            return binding.root

        }
    }

    private fun getCurrentProgressDialog(): AlertDialog {
        var progressDialog = DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)
        if (progressDialog?.isShowing == false || progressDialog == null) {
            progressDialog =
                DialogScreen.showDialog(
                    this@DocumentSelectFragment.requireContext(),
                    DialogScreen.IDD_PROGRESS
                )
            progressDialog.show()
        }
        return progressDialog
    }

    private fun saveEmptyExternalDocument(documentItem: ExternalDocument) {
//        DocumentHeaders.setWarehouse(documentItem.externalOrderDivision?.divisionDefaultWarehouseGuid.)
//        val physicalPerson = viewModelTableScan.getPhysicalPersonByGuid(
//            ""
//        )
//        DocumentHeaders.setPhysicalPerson(physicalPerson)
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        viewModelTableScan.saveRecord(
            TableScan(
                OperationId = selectedOption.id,
                OperationTitle = selectedOption.docType?.title ?: "",
                docTitle = "${documentItem.externalOrderDocumentTitle} ${documentItem.externalOrderNumber} от ${
                    sdf.format(
                        documentItem.externalOrderDate
                    )
                }",
                docGuid = documentItem.externalOrderDocumentGuid,
                docCount = 0.0,
                coefficient = 0.0,
                cellTitle = "",
                cellGuid = "",
                ItemTitle = "",
                ItemGUID = "",
                ItemMeasureOfUnitGUID = "",
                ItemMeasureOfUnitTitle = "",
                docHeaders = DocumentHeaders,
                OwnerGuid = user1C.getUserGUID(),
            ), false
        )
    }

    private fun getExternalDocumentsList(docType: DocType?) {
        //DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS).show()
        when (docType) {
            DocType.TOIR_REQUIREMENT_INVOICE -> {
                viewModel.getInternalOrderList()
            }
            else -> {
                viewModel.getRepairEstimate()
            }
        }
    }

    private fun addEmptyItemExternalDocumentList() {
        externalDocumentList.add(
            ExternalDocument(
                getString(R.string.not_chosen_text),
                "",
                0L,
                "",
                null,
                null
            )
        )
    }

    private fun initAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView) {

        if (externalDocumentList.isEmpty())
            addEmptyItemExternalDocumentList()

        val internalOrderAdapter = DynamicListAdapter<ExternalDocument>(
            requireContext(),
            R.layout.dynamic_prefs_layout,
            externalDocumentList,
            filterOff = true
        )

        autoCompleteTextView.setAdapter(internalOrderAdapter)
    }

    override fun onDestroyView() {
        BarcodeScannerReceiver.setEnabled()
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        progressDialog?.dismiss()
        super.onDestroyView()
    }

    override fun onResume() {
        if (needShowingProgressDialog) getCurrentProgressDialog()
        super.onResume()
    }
}