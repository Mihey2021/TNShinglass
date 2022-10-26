package ru.tn.shinglass.activity

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.databinding.FragmentTableScanBinding
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.OnTableScanItemInteractionListener
import ru.tn.shinglass.adapters.TableScanAdapter
import ru.tn.shinglass.databinding.InventoryInitDialogBinding
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel

class TableScanFragment : Fragment() {

    private val viewModel: TableScanFragmentViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var selectedOption: Option
    private lateinit var user1C: User1C

    //private lateinit var itemList: List<TableScan>
    private lateinit var dlgBinding: InventoryInitDialogBinding
    private var dlg: AlertDialog? = null

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

        selectedOption = arguments?.getSerializable("selectedOption") as Option
        setFragmentResult("requestSelectedOption", bundleOf("selectedOption" to selectedOption))
        user1C = arguments?.getSerializable("userData") as User1C
        setFragmentResult("requestUserData", bundleOf("userData" to user1C))

        binding.infoTextView.text = getString(R.string.info_table_scan_start_text)

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
        getPhysicalPersonList()


        binding.completeAndSendBtn.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (selectedOption.option == OptionType.INVENTORY) {
                        retrofitViewModel.createInventoryOfGoods(itemList) //primary = Первичная инвент. (поиск в ТЧ 1С идет без учета ячейки, т.к. в ячейках там еще ничего нет)
                        progressDialog =
                            DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    }
                    false
                }
                else -> false
            }
        }


        viewModel.refreshTableScan(user1C.getUserGUID(), selectedOption.id)

        retrofitViewModel.docCreated.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            progressDialog?.dismiss()
            //Toast.makeText(requireContext(),"Документ отправлен!", Toast.LENGTH_LONG).show()
            DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_SUCCESS,
                "Документ в 1С успешно создан.\nНомер: ${it.docNumber}\nДетали:${if (it.details.isNotEmpty()) "\n${it.details}" else "[нет]"}",
                it.docTitle
            )
            retrofitViewModel.resetTheDocumentCreatedFlag()
            viewModel.deleteRecordsByOwnerAndOperationId(user1C.getUserGUID(), selectedOption.id)
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

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {

            val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcode = dataScanPair?.first ?: ""
            val dataScanBarcodeType = dataScanPair?.second ?: ""

            if (dataScanBarcode == "") return@observe

            val args = Bundle()
            args.putSerializable("userData", user1C)
            args.putSerializable("selectedOption", selectedOption)
            args.putString("barcode", dataScanBarcode)

            if (itemList.isEmpty()) {
                if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null) {
                    var showingHeadersDialog = true
                    if (dlg != null)
                        showingHeadersDialog = !dlg!!.isShowing
                    if (showingHeadersDialog) {
                        dlg = getDocumentHeadersDialog(args)
                    }
                }
                openDetailScanFragment(args, dlg)
            } else {
                //val lastIndex = itemList.count() - 1
                //binding.infoTextView.text = "Чтобы начать, отсканируйте ячейку или номенклатуру.\nТекущая ячейка: ${itemList[lastIndex].cellTitle}"
                args.putSerializable(
                    "editRecord",
                    TableScan(
                        cellGuid = itemList[0].cellGuid,
                        cellTitle = itemList[0].cellTitle,
                        warehouseGuid = itemList[0].warehouseGuid,
                        PhysicalPersonGUID = itemList[0].PhysicalPersonGUID,
                        PhysicalPersonTitle = itemList[0].PhysicalPersonTitle,
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
            if (it.loading)
                progressDialog = DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            else
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
                                "getPhysicalPersonList" -> {
                                    progressDialog?.show()
                                    getPhysicalPersonList()
                                }
                                "getAllWarehousesList" -> {
                                    progressDialog?.show()
                                    LayoutInflater.from(requireContext())//.inflate(R.layout.inventory_init_dialog, null)
                                    dlgBinding = InventoryInitDialogBinding.inflate(layoutInflater)
                                    getAllWarehousesList(dlgBinding)
                                }

                            }
                        }
                    })
            }
        }

        viewModel.warehousesList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            viewModel.saveWarehouses(it)
            setWarehousesAdapter(dlgBinding)
        }

        viewModel.physicalPersons.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            val dataList = arrayListOf<PhysicalPerson>()
            it.forEach { person ->
                dataList.add(person)
            }
            progressDialog?.dismiss()
            val adapter = DynamicListAdapter<PhysicalPerson>(
                requireContext(),
                R.layout.dynamic_prefs_layout,
                dataList
            )

            dlgBinding.phisicalPersonTextView.setAdapter(adapter)
        }

        return binding.root
    }

    private fun getDocumentHeadersDialog(
        args: Bundle? = null,
        cancellable: Boolean = true
    ): AlertDialog? {
        val layoutInflater =
            LayoutInflater.from(requireContext())//.inflate(R.layout.inventory_init_dialog, null)
        dlgBinding = InventoryInitDialogBinding.inflate(layoutInflater)

        with(dlgBinding) {
            val warehouseGuidByPrefs =
                settingsViewModel.getPreferenceByKey("warehouse_guid")
            if (warehouseGuidByPrefs.isNullOrBlank())
                warehouseTextInputLayout.error = "Склад не задан в настройках!".toString()

            val warehouse = settingsViewModel.getWarehouseByGuid(warehouseGuidByPrefs ?: "")
            if (DocumentHeaders.getWarehouse() == null)
                DocumentHeaders.setWarehouse(warehouse)
            warehouseTextView.setText(DocumentHeaders.getWarehouse()?.title)
            warehouseTextInputLayout.error = null

            warehouseTextView.setOnClickListener {
                if (warehouseTextView.adapter == null) {
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                    getAllWarehousesList(dlgBinding)
                }
            }
            warehouseTextView.setOnItemClickListener { adapterView, _, position, _ ->
                val warehouseItem = adapterView.getItemAtPosition(position) as Warehouse
                DocumentHeaders.setWarehouse(warehouseItem)
                if (phisicalPersonTextView.text.isNullOrBlank()) {
                    phisicalPersonTextView.setText(warehouseItem.responsibleGuid)
                }
                warehouseTextView.setText(warehouseItem?.title)
                warehouseTextInputLayout.error = null
            }


            phisicalPersonTextInputLayout.hint = "МОЛ"
            if (DocumentHeaders.getPhysicalPerson() != null)
                phisicalPersonTextView.setText(DocumentHeaders.getPhysicalPerson()?.fio)
            phisicalPersonTextView.setOnClickListener {
                if (phisicalPersonTextView.adapter == null) {
                    getPhysicalPersonList()
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                }
            }
            phisicalPersonTextView.setOnItemClickListener { adapterView, _, position, _ ->
                val physicalPerson = adapterView.getItemAtPosition(position) as PhysicalPerson
                DocumentHeaders.setPhysicalPerson(physicalPerson)
                phisicalPersonTextView.setText(physicalPerson?.fio)
                phisicalPersonTextInputLayout.error = null
            }
        }

        if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null) {
            dlg = DialogScreen.getDialog(
                requireContext(),
                DialogScreen.IDD_INPUT,
                isCancelable = cancellable,
                customView = dlgBinding.root,
                onDialogsInteractionListener = object : OnDialogsInteractionListener {
                    override fun onPositiveClickButton() {
                        if (DocumentHeaders.getWarehouse() == null)
                            dlgBinding.warehouseTextInputLayout.error = "Склад не заполнен!"
                        if (DocumentHeaders.getPhysicalPerson() == null)
                            dlgBinding.phisicalPersonTextInputLayout.error = "МОЛ не указан!"
                        if (DocumentHeaders.getWarehouse() != null && DocumentHeaders.getPhysicalPerson() != null)
                            if (args != null)
                                openDetailScanFragment(args, dlg!!)
                    }
                }
            )
        }

        return dlg
    }

    private fun openDetailScanFragment(args: Bundle, dlg: AlertDialog?) {
        if (DocumentHeaders.getWarehouse() != null && DocumentHeaders.getPhysicalPerson() != null) {
            val warehouseGuid = DocumentHeaders.getWarehouse()?.guid.toString()
            val physicalPersonGuid =
                DocumentHeaders.getPhysicalPerson()?.guid.toString()
            val physicalPersonTitle =
                DocumentHeaders.getPhysicalPerson()?.fio.toString()
            args.putSerializable(
                "editRecord",
                TableScan(
                    OwnerGuid = user1C.getUserGUID(),
                    warehouseGuid = warehouseGuid,
                    PhysicalPersonGUID = physicalPersonGuid,
                    PhysicalPersonTitle = physicalPersonTitle
                )
            )
            dlg?.dismiss()
            findNavController().navigate(
                R.id.action_tableScanFragment_to_detailScanFragment,
                args
            )
        }
    }

    private fun getAllWarehousesList(binding: InventoryInitDialogBinding) {
        viewModel.getAllWarehousesList()
    //        val dbWarehouses = viewModel.getAllWarehousesList()
//        if (dbWarehouses.isEmpty()) {
//            retrofitViewModel.getAllWarehouses()
//        } else {
//            setWarehousesAdapter(binding)
//        }
    }

    private fun setWarehousesAdapter(
        binding: InventoryInitDialogBinding
    ) {

        val dataList = arrayListOf<Warehouse>()
        //val warehousesList = viewModel.getAllWarehousesList()
        val warehousesList = viewModel.warehousesList.value
        warehousesList?.forEach { warehouse -> dataList.add(warehouse) }
        progressDialog?.dismiss()
        val adapter = DynamicListAdapter<Warehouse>(
            requireContext(),
            R.layout.dynamic_prefs_layout,
            dataList
        )
        binding.warehouseTextView.setAdapter(adapter)

    }

    private fun getPhysicalPersonList() {
        //retrofitViewModel.getPhysicalPersonList()
        viewModel.getAllPhysicalPerson()
    }

    override fun onResume() {
        viewModel.refreshTableScan(user1C.getUserGUID(), selectedOption.id)
        if (itemList.isEmpty()) {
            if (DocumentHeaders.getWarehouse() == null || DocumentHeaders.getPhysicalPerson() == null)
                dlg = getDocumentHeadersDialog()
        }
        super.onResume()
    }

    override fun onDestroy() {
        if (itemList.isEmpty()) {
            DocumentHeaders.setWarehouse(null)
            DocumentHeaders.setPhysicalPerson(null)
        }
        super.onDestroy()
    }
}