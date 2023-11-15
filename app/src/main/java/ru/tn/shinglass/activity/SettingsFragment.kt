package ru.tn.shinglass.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.extendsComponents.ExtendListPreference
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.data.api.ApiService
import ru.tn.shinglass.models.Cell
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.Warehouse
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel

private var apiService: ApiService? = null
private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
private var progressDialog: AlertDialog? = null

class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val serviceUserPassword = findPreference<EditTextPreference>("serviceUserPassword")
        serviceUserPassword?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
        }


        val root = preferenceManager.preferenceScreen

        val categoryDocSettings = PreferenceCategory(requireContext())
        categoryDocSettings.key = "document_settings"
        categoryDocSettings.title = getString(R.string.documents_settings_title)

        root.addPreference(categoryDocSettings)

        getCurrentApiService()

        val divisionListPreference = ExtendListPreference<Division>(requireContext())
        val divisionAdapter =
            DynamicListAdapter<Division>(requireContext(), R.layout.dynamic_prefs_layout)

        divisionListPreference.setAdapter(divisionAdapter)
        val divisionKey = "division_guid"
        divisionListPreference.key = divisionKey
        divisionListPreference.title = getString(R.string.division_title)
        val savedDivision = settingsViewModel.getDivisionByGuid(
            settingsViewModel.getPreferenceByKey<String>(divisionKey, "") ?: ""
        )

        divisionListPreference.summary =
            savedDivision?.divisionTitle ?: getString(R.string.division_list_description)
        divisionListPreference.icon =
            resources.getDrawable(R.drawable.ic_baseline_division_24, requireContext().theme)
        divisionListPreference.setDialogTitle(getString(R.string.division_list_description))

        //Заполним список подразделений сразу при открытии
        if (divisionListPreference.getDataListArray().isEmpty()) {
            settingsViewModel.getAllDivisions()
        }

        categoryDocSettings.addPreference(divisionListPreference)

        val warehouseListPreference = ExtendListPreference<Warehouse>(requireContext())
        val adapter =
            DynamicListAdapter<Warehouse>(requireContext(), R.layout.dynamic_prefs_layout)
        warehouseListPreference.setAdapter(adapter)
        val warehouseKey = "warehouse_guid"
        warehouseListPreference.key = warehouseKey
        warehouseListPreference.title = getString(R.string.warehouse_title)
        //warehouseListPreference
        val savedWarehouse = settingsViewModel.getWarehouseByGuid(
            settingsViewModel.getPreferenceByKey<String>(warehouseKey, "") ?: ""
        )

        warehouseListPreference.summary =
            savedWarehouse?.warehouseTitle ?: getString(R.string.warehouse_list_description)
        warehouseListPreference.icon =
            resources.getDrawable(R.drawable.ic_baseline_warehouse_24, requireContext().theme)
        warehouseListPreference.setDialogTitle(getString(R.string.warehouse_list_description))

        //Заполним список складов сразу при открытии
        //if (warehouseListPreference.getDataListArray().isEmpty()) {
        settingsViewModel.getAllWarehouses()
        //}

        warehouseListPreference.setOnPreferenceClickListener {
            settingsViewModel.getAllWarehouses()
            false
        }

        categoryDocSettings.addPreference(warehouseListPreference)

        val usedLogistics = CheckBoxPreference(requireContext())
        usedLogistics.key = "usedLogistics"
        usedLogistics.title = getString(R.string.warehouse_uses_logistics)
        usedLogistics.isSelectable = false
        //usedLogistics.isEnabled = false

        categoryDocSettings.addPreference(usedLogistics)

        val virtualCellsListPreference = ExtendListPreference<Cell>(requireContext())
        val virtualCellsAdapter = getVirtualCellAdapter()

        virtualCellsListPreference.setAdapter(virtualCellsAdapter)
        val virtualCellKey = "virtual_cell_guid"
        virtualCellsListPreference.key = virtualCellKey
        virtualCellsListPreference.title = getString(R.string.header_virtual_cell)
        //virtualCellsListPreference
        //val savedCell =
        retrofitViewModel.getCellByGuid(
            settingsViewModel.getPreferenceByKey<String>(virtualCellKey, "") ?: ""
        )


        virtualCellsListPreference.summary = getString(R.string.select_virtual_cell)
        virtualCellsListPreference.icon =
            resources.getDrawable(R.drawable.ic_baseline_virtual_cell_24, requireContext().theme)
        virtualCellsListPreference.setDialogTitle(getString(R.string.select_virtual_cell))

        //Заполним список складов сразу при открытии
        if (virtualCellsListPreference.getDataListArray().isEmpty()) {
            retrofitViewModel.getCellsList(savedWarehouse?.warehouseGuid ?: "")
        }

        categoryDocSettings.addPreference(virtualCellsListPreference)

        setLogisticsViewsParams(savedWarehouse, virtualCellsListPreference, usedLogistics)

        listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                when (key) {
                    "serviceUrl", "serviceUserName", "serviceUserPassword" -> {
                        if (sharedPrefs.getString("serviceUrl", "").toString()
                                .isEmpty() || sharedPrefs.getString("serviceUserName", "")
                                .toString()
                                .isEmpty() || sharedPrefs.getString("serviceUserPassword", "")
                                .toString().isEmpty()
                        ) {
                            getCurrentApiService(newService = true)
                            return@OnSharedPreferenceChangeListener
                        }

                        DialogScreen.getDialog()?.dismiss()
                        DialogScreen.showDialog(
                            requireContext(),
                            DialogScreen.IDD_QUESTION,
                            resources.getString(R.string.recreate_connection_text),
                            resources.getString(R.string.settings_header),
                            onDialogsInteractionListener = object :
                                OnDialogsInteractionListener {
                                override fun onPositiveClickButton() {
                                    getCurrentApiService(newService = true)
                                    findNavController().navigateUp()
                                    //restartApplication()
                                }
                            }
                        )
                    }
                    "warehouse_guid" -> {
                        val warehouseGuidPrefSaved = sharedPrefs.getString(
                            key,
                            ""
                        ).toString()
                        val warehouse = settingsViewModel.getWarehouseByGuid(warehouseGuidPrefSaved)
                        setLogisticsViewsParams(warehouse, virtualCellsListPreference, usedLogistics)
                        if (sharedPrefs.getString("division_guid", "").toString() == "") {
                            val division = settingsViewModel.getDivisionByGuid(
                                warehouse?.warehouseDivisionGuid ?: ""
                            )
                            if (division != null) {
                                divisionListPreference.value = division.divisionGuid
                                divisionListPreference.summary = division.divisionTitle
                            }
                        }
                        virtualCellsListPreference.value = ""
                        virtualCellsListPreference.summary = getString(R.string.select_virtual_cell)
                        retrofitViewModel.getCellsList(warehouseGuidPrefSaved)
                    }
                    "division_guid" -> {
                        if (sharedPrefs.getString("warehouse_guid", "").toString() == "") {
                            val warehouse =
                                settingsViewModel.getWarehouseByGuid(
                                    settingsViewModel.getDivisionByGuid(
                                        sharedPrefs.getString(
                                            key,
                                            ""
                                        ).toString()
                                    )?.divisionDefaultWarehouseGuid ?: ""
                                )
                            if (warehouse != null) {
                                warehouseListPreference.value = warehouse.warehouseGuid
                                warehouseListPreference.summary = warehouse.warehouseTitle
                                virtualCellsListPreference.value = ""
                                virtualCellsListPreference.summary =
                                    getString(R.string.select_virtual_cell)
                                retrofitViewModel.getCellsList(warehouse.warehouseGuid)
                                setLogisticsViewsParams(warehouse, virtualCellsListPreference, usedLogistics)
                            }

                        }
                    }
//                    "virtual_cell_guid" -> {
//                        if (sharedPrefs.getString("virtual_cell_guid", "").toString() == "") {
//                            val virtualCell = retrofitViewModel.getCellByGuid(
//                                settingsViewModel.getWarehouseByGuid(
//                                    sharedPrefs.getString(
//                                        "warehouse_guid",
//                                        ""
//                                    ).toString()
//                                )?.warehouseDivisionGuid ?: ""
//                            )
//                            if (virtualCell != null) {
//                                divisionListPreference.value = virtualCell.g
//                                divisionListPreference.summary = division.divisionTitle
//                            }
//                        }
//                    }
                }
            }

        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun setLogisticsViewsParams(warehouse: Warehouse?, virtualCellsListPreference: ExtendListPreference<Cell>, usedLogistics: CheckBoxPreference) {
        usedLogistics.isChecked = warehouse?.usesLogistics ?: false
        virtualCellsListPreference.isVisible = usedLogistics.isChecked
    }

    private fun getCurrentApiService(newService: Boolean = false) {
        apiService = ApiUtils.getApiService(settingsViewModel.getBasicPreferences(), newService)
    }

    private fun getVirtualCellAdapter(): DynamicListAdapter<Cell> {
        return DynamicListAdapter<Cell>(requireContext(), R.layout.dynamic_prefs_layout)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Фон экрана настроек
        val backgroundIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_tn)
        //backgroundIcon?.setTint(requireContext().getColor(R.color.red_400))
        backgroundIcon?.alpha = 20
        if (backgroundIcon != null) {
            requireView().background = backgroundIcon
        } else {
            requireView().setBackgroundResource(R.color.red_100)
        }
        val warehouseListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("warehouse_guid") as ExtendListPreference<Warehouse>

        val divisionListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("division_guid") as ExtendListPreference<Division>

        val virtualCellsListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("virtual_cell_guid") as ExtendListPreference<Cell>


        settingsViewModel.warehousesList.observe(viewLifecycleOwner) {
            //if (it.isEmpty()) return@observe

            val dataList = ArrayList<Warehouse>()
            if (it.isEmpty())
                dataList.add(Warehouse(getString(R.string.no_data), "", "", ""))
            else
                dataList.add(Warehouse(getString(R.string.not_chosen_text), "", "", ""))

            it.forEach { warehouse ->
                dataList.add(warehouse)
            }
            //progressDialog?.dismiss()

            //if (warehouseListPreference.getDataListArray().isEmpty())
            //warehouseListPreference.clearAdapterData()
            warehouseListPreference.setDataListArray(dataList)
        }

        settingsViewModel.divisionsList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            val dataList = ArrayList<Division>()
            dataList.add(Division(getString(R.string.not_chosen_text), "", ""))
            it.forEach { division ->
                dataList.add(division)
            }

            if (divisionListPreference.getDataListArray().isEmpty())
                divisionListPreference.setDataListArray(dataList)
        }


        settingsViewModel.dataState.observe(viewLifecycleOwner) {
            if (it.loading) {
                //if (progressDialog?.isShowing == false || progressDialog == null)
                if (DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.isShowing == false || DialogScreen.getDialog(
                        DialogScreen.IDD_PROGRESS
                    ) == null
                )
                //progressDialog =
                    DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else {
                DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
                //progressDialog?.dismiss()
            }

            if (it.error) {
                DialogScreen.getDialog()?.dismiss()
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    message = it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getAllWarehousesList" -> settingsViewModel.getAllWarehouses()
                                "getAllDivisions" -> settingsViewModel.getAllDivisions()
                                "getCellByGuid" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "cellGuid")
                                        retrofitViewModel.getCellByGuid(requestParam.propertyValue)
                                }
                            }
                        }
                    }
                )
            }
        }

        retrofitViewModel.dataState.observe(viewLifecycleOwner) {
            if (it.loading) {
                //if (progressDialog?.isShowing == false || progressDialog == null)
                if (DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.isShowing == false || DialogScreen.getDialog(
                        DialogScreen.IDD_PROGRESS
                    ) == null
                )
                //progressDialog =
                    DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else {
                DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
                //progressDialog?.dismiss()
            }

            if (it.error) {
                DialogScreen.getDialog()?.dismiss()
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    message = it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getCellByGuid" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "cellGuid")
                                        retrofitViewModel.getCellByGuid(requestParam.propertyValue)
                                }
                            }
                        }
                    }
                )
            }
        }

        retrofitViewModel.cellListData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            //if (it.isEmpty()) return@observe

            val dataList = ArrayList<Cell>()
            dataList.add(Cell(getString(R.string.not_chosen_text), ""))
            it.forEach { cell ->
                dataList.add(cell)
            }

            //if (virtualCellsListPreference.getDataListArray().isEmpty())
            virtualCellsListPreference.clearAdapterData()
            virtualCellsListPreference.setDataListArray(dataList)
        }

        retrofitViewModel.virtualCellData.observe(viewLifecycleOwner) {
            var cell = getString(R.string.select_virtual_cell)

            if (it != null)
                if (it.title != "")
                    cell = it.title

            virtualCellsListPreference.summary = cell
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {
            if (it.first == "" && it.second == "") return@observe
            BarcodeScannerReceiver.clearData()
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun onDestroyView() {
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        DialogScreen.getDialog()?.dismiss()
        ContextCompat.getDrawable(requireActivity(), R.drawable.ic_tn)?.alpha = 100
        super.onDestroyView()
    }

//    private fun restartApplication() {
////        Log.d("RESTART", "Restart application")
//        val mStartActivity = Intent(requireContext(), AppActivity::class.java)
//        val mPendingIntentId: Int = 1234567
//        val mPendingIntent = PendingIntent.getActivity(
//            requireContext(),
//            mPendingIntentId,
//            mStartActivity,
//            PendingIntent.FLAG_CANCEL_CURRENT
//        )
//        val mgr: AlarmManager =
//            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
//
//        AndroidUtils.closeActivity(requireActivity())
//    }

}

