package ru.tn.shinglass.activity

import android.bluetooth.le.ScanRecord
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.databinding.FragmentDesktopBinding
import ru.tn.shinglass.databinding.FragmentDetailScanBinding
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.PhisicalPerson
import ru.tn.shinglass.models.TableScan
import ru.tn.shinglass.models.Warehouse
import ru.tn.shinglass.viewmodel.DetailScanViewModel
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import kotlin.coroutines.EmptyCoroutineContext

class DetailScanFragment : Fragment() {

//    companion object {
//        fun newInstance() = DetailScanFragment()
//    }

    private val viewModel: DetailScanViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()

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
        var scanRecord = arguments?.getSerializable("scanRecord") as TableScan

        //BarcodeScannerReceiver.clearData()

        val tempScanRecord = TempScanRecord(scanRecord)
        tempScanRecord.OperationId = selectedOption.id
        tempScanRecord.OperationTitle = selectedOption.title

        with(binding) {
            operationTitleTextView.text = selectedOption.title
            //divisionTextView.setText("Подразделение из настроек")

            if (selectedOption.type == "ИНВЕНТАРИЗАЦИЯ") {
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


            val warehouseGuidByPrefs = viewModel.getPreferenceByKey("warehouse_guid")
            if (warehouseGuidByPrefs.isNullOrBlank()) {
                warehouseTextInputLayout.error = "Склад не задан в настройках!".toString()
            } else {
                val warehouse = viewModel.getWarehouseByGuid(
                    scanRecord?.warehouseGuid
                        ?: warehouseGuidByPrefs
                )
                warehouseTextView.setText(warehouse?.title)
                tempScanRecord.warehouseGuid = warehouse?.guid.toString()
                warehouseTextInputLayout.error = null
            }
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

            itemTextView.setText(barcode)
            countEditText.setText("1.0")
            //countEditText.contentDescription = "шт."
            if (!countEditText.text.isNullOrBlank())
                countTextInputLayout.hint = "шт."
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
            workwearDisposableCheckBox.isChecked = true

            purposeOfUseTextView.setText("Выбранное назначение использования")
            purposeOfUseTextView.setOnClickListener {
                //TODO: Обработка выбора назначения использования
                Toast.makeText(requireContext(), "Клик по ссылке", Toast.LENGTH_SHORT).show()
            }

            //phisicalPersonTextView.setText("Выбранное физическое лицо")

            phisicalPersonTextInputLayout.hint = "МОЛ"
            phisicalPersonTextView.setOnClickListener {
                if (phisicalPersonTextView.adapter == null) {
                    getPhysicalPersonList()
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                }
            }
            phisicalPersonTextView.setOnItemClickListener { adapterView, _, position, _ ->
                val physivalPeron = adapterView.getItemAtPosition(position) as PhisicalPerson
                phisicalPersonTextView.setText(physivalPeron.fio)
                phisicalPersonTextInputLayout.error = null
                tempScanRecord.PhysicalPersonGUID = physivalPeron.guid
                tempScanRecord.PhysicalPersonTitle = physivalPeron.fio
            }

            ownerTextView.setText("${getString(R.string.owner_title)} ${user1C.getUser1C()}")
            tempScanRecord.OwnerGuid = user1C.getUserGUID()

            buttonApply.setOnClickListener {
                сheckFillingAndSave(tempScanRecord, user1C, selectedOption, binding)
            }
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {

            val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcodeFromScaner = dataScanPair?.first ?: ""
            val dataScanBarcode =
                if (dataScanBarcodeFromScaner == "") barcode ?: "" else dataScanBarcodeFromScaner
            barcode = ""

            val dataScanBarcodeType = dataScanPair?.second ?: ""

            if (dataScanBarcode == "") return@observe

            if (dataScanBarcodeType == "Code 128") {
                retrofitViewModel.getCellByBarcode(dataScanBarcode)
                binding.cellTextInputLayout.error = null
            }
        }

        retrofitViewModel.cellData.observe(viewLifecycleOwner) {

            if (it == null) return@observe
            binding.cellTextView.setText(it.title)
            tempScanRecord.cellGuid = it.guid
            tempScanRecord.cellTitle = it.title

        }



        retrofitViewModel.listDataPhisicalPersons.observe(viewLifecycleOwner) {

            if (it.isEmpty()) return@observe

            val dataList = arrayListOf<PhisicalPerson>()
            it.forEach { person -> dataList.add(person) }
            progressDialog?.dismiss()
            val adapter = DynamicListAdapter<PhisicalPerson>(
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
                null,
                object : OnDialogsInteractionListener {
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

        viewModel.saveScanRecord(tempScanRecord.toScanRecord())
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

        val dataList = arrayListOf<Warehouse>()
        val warehousesList = viewModel.getAllWarehousesList()
        warehousesList.forEach { warehouse -> dataList.add(warehouse) }
        progressDialog?.dismiss()
        val adapter = DynamicListAdapter<Warehouse>(
            requireContext(),
            R.layout.dynamic_prefs_layout,
            dataList
        )
        binding.warehouseTextView.setAdapter(adapter)

    }

    private fun getAllWarehousesList(binding: FragmentDetailScanBinding) {
        val dbWarehouses = viewModel.getAllWarehousesList()
        if (dbWarehouses.isEmpty()) {
            retrofitViewModel.getAllWarehouses()
        } else {
            setWarehousesAdapter(binding)
        }
    }


    private fun getPhysicalPersonList() {
        retrofitViewModel.getPhysicalPersonList()
    }


//private fun setPhisicalPersonDataList(): List<PhisicalPerson> {
//
//    val progressDialog = DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
//
//    val arrayPhisicalPersonList = ArrayList<PhisicalPerson>()
//
////        val warehouseListFromDb = getAllWarehousesFromDb()
////        if (warehouseListFromDb.isNotEmpty()) {
////            warehouseListFromDb.forEach { arrayListWarehouse.add(it) }
////            warehouseListPreference.setDataListArray(arrayListWarehouse)
////            progressDialog.cancel()
////            warehouseListPreference.showDialog()
////        } else {
//    val apiService = ApiUtils.getApiService(settingsViewModel.getBasicPreferences())
//    apiService?.getPhysicalPersonList()?.enqueue(object : Callback<List<PhisicalPerson>> {
//        override fun onResponse(
//            call: Call<List<PhisicalPerson>>,
//            response: Response<List<PhisicalPerson>>
//        ) {
//            progressDialog.cancel()
//            if (!response.isSuccessful) {
//                //TODO: Обработка не 2хх кода ответа
//                return
//            }
//
//            val phisicalPersonList = response.body()
//            if (phisicalPersonList == null) {
//                //TODO: Обработка пустого ответа
//                return
//            }
//            //Сохраним полученные склады в базу данных
//            //settingsViewModel.save(phisicalPersonList)
//            //Прочитаем из БД
//            //getAllWarehousesFromDb().forEach { arrayListWarehouse.add(it) }
//            //Установим список
////                    warehouseListPreference.setDataListArray(arrayListWarehouse)
////                    //Покажем диалог выбора склада
////                    warehouseListPreference.showDialog()
//            phisicalPersonList.forEach { arrayPhisicalPersonList.add(it) }
//            return
//        }
//
//        override fun onFailure(call: Call<List<PhisicalPerson>>, t: Throwable) {
//            progressDialog.cancel()
//            DialogScreen.getDialog(
//                requireContext(),
//                DialogScreen.IDD_ERROR,
//                t.message.toString(),
//                onDialogsInteractionListener = object :
//                    OnDialogsInteractionListener {
//                    override fun onPositiveClickButton() {
//                        setPhisicalPersonDataList()
//                    }
//                })
////                    DialogScreen.getDialogBuilder(
////                        requireContext(),
////                        DialogScreen.IDD_ERROR,
////                        t.message.toString()
////                    )
////                        .setNegativeButton(resources.getString(R.string.cancel_text)) { dialog, _ ->
////                            dialog.cancel()
////                        }
////                        .setPositiveButton(resources.getString(R.string.retry_loading)) { dialog, _ ->
////                            setWarehousesDataList(warehouseListPreference)
////                            dialog.dismiss()
////                        }
////                        .show()
//            return
//        }
//
//    })
//    //}
//
//    //warehouseListPreference.setDataListArray(arrayListWarehouse)
//    return arrayPhisicalPersonList
//}

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


