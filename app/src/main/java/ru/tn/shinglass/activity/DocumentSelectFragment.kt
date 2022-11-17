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
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.OnTableScanItemInteractionListener
import ru.tn.shinglass.adapters.TableScanAdapter
import ru.tn.shinglass.databinding.FragmentDocumentSelectBinding
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.ExternalDocument
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.viewmodel.DocumentSelectFragmentViewModel
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel
import java.text.SimpleDateFormat

class DocumentSelectFragment : Fragment() {

    private val viewModel: DocumentSelectFragmentViewModel by viewModels()
    private val viewModelTableScan: TableScanFragmentViewModel by viewModels()

    private lateinit var selectedOption: Option
    private lateinit var user1C: User1C

    private var progressDialog: AlertDialog? = null
    private val internalOrderList: ArrayList<ExternalDocument> = arrayListOf()
    private var isNew: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        selectedOption = arguments?.getSerializable("selectedOption") as Option
        user1C = arguments?.getSerializable("userData") as User1C
        isNew = arguments?.getBoolean("isNew") ?: false

        viewModelTableScan.refreshTableScan(user1C.getUserGUID(), selectedOption.id)

        //Заполним таблицу физ лиц, чтобы после выбора документа привязать ответственного склада
        viewModelTableScan.getAllPhysicalPerson()

        val binding = FragmentDocumentSelectBinding.inflate(inflater, container, false)

        val adapter = TableScanAdapter(object : OnTableScanItemInteractionListener {
            override fun selectItem(item: TableScan) {
                super.selectItem(item)
            }
        }, isExternalDocumentDetail = true)

        with(binding) {
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
            documentTextInputLayout.isEnabled = isNew
            list.adapter = adapter
            if (isNew) {
                initAutoCompleteTextView(binding.documentTextView)
                viewModel.getInternalOrderList()
                documentTextView.setText((documentTextView.adapter.getItem(0) as ExternalDocument).externalOrderDocumentTitle, false);
                DocumentHeaders.setExternalDocumentSelected(true)
                documentTextView.setOnClickListener {
                    if (documentTextView.adapter == null || documentTextView.adapter.count == 0) {
                        viewModel.getInternalOrderList()
                    }
                }
                documentTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val documentItem = adapterView.getItemAtPosition(position) as ExternalDocument
                    if (isNew) viewModelTableScan.deleteRecordsByOwnerAndOperationId(
                        user1C.getUserGUID(),
                        selectedOption.id
                    )

                    if (documentItem.externalOrderDocumentGuid == "") {
                        groupTableItems.visibility = View.GONE
                        DocumentHeaders.setExternalDocumentSelected(true)
                        DocumentHeaders.setDivision(null)
                        DocumentHeaders.setWarehouse(null)
                        DocumentHeaders.setPhysicalPerson(null)
                    } else {
                        groupTableItems.visibility = View.VISIBLE
                        DocumentHeaders.setExternalDocumentSelected(true)
                        DocumentHeaders.setDivision(documentItem.externalOrderDivision)
                        binding.documentTextInputLayout.hint = getString(R.string.document_1c_text)
                        if (documentItem.externalDocumentItems != null)
                        //adapter.submitList(documentItem.externalDocumentItems)
                            documentItem.externalDocumentItems.forEach { item ->
                                DocumentHeaders.setWarehouse(item.warehouse)
                                val physicalPerson = viewModelTableScan.getPhysicalPersonByGuid(
                                    item.warehouse?.warehouseResponsibleGuid ?: ""
                                )
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
                        viewModelTableScan.refreshTableScan(user1C.getUserGUID(), selectedOption.id)
                    }
                }
            } else {
                groupTableItems.visibility = View.VISIBLE
            }

            viewModel.internalOrderList.observe(viewLifecycleOwner) {
                if (it.isEmpty()) return@observe

                internalOrderList.clear()
                addEmptyItemInternalOrderList()

                it.forEach { internalOrderDocument ->
                    internalOrderList.add(internalOrderDocument)
                }

                initAutoCompleteTextView(binding.documentTextView)

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
                                    "getInternalOrderList" -> {
                                        viewModel.getInternalOrderList()
                                    }
                                }
                            }
                        }
                    )
                }
            }

            viewModelTableScan.data.observe(viewLifecycleOwner) {
                adapter.submitList(it)
                if (it.isNotEmpty()) {
                    binding.documentTextView.setText(it[0].docTitle)
                    binding.documentTextInputLayout.hint = getString(R.string.document_1c_text)
                }
            }

            return binding.root

        }
    }

    private fun addEmptyItemInternalOrderList() {
        internalOrderList.add(
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

        if (internalOrderList.isEmpty())
            addEmptyItemInternalOrderList()

        val internalOrderAdapter = DynamicListAdapter<ExternalDocument>(
            requireContext(),
            R.layout.dynamic_prefs_layout,
            internalOrderList,
            filterOff = true
        )

        autoCompleteTextView.setAdapter(internalOrderAdapter)
    }


}