package ru.tn.shinglass.activity

//import ru.tn.shinglass.adapters.ru.tn.shinglass.adapters.DynamicListAdapter
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
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

        val root = preferenceManager.preferenceScreen

        val categoryDocSettings = PreferenceCategory(requireContext())
        categoryDocSettings.key = "document_settings"
        categoryDocSettings.title = getString(R.string.documents_settings_title)

        root.addPreference(categoryDocSettings)

        apiService = ApiUtils.getApiService(settingsViewModel.getBasicPreferences())

        val divisionListPreference = ExtendListPreference<Division>(requireContext())
        val divisionAdapter =
            DynamicListAdapter<Division>(requireContext(), R.layout.dynamic_prefs_layout)

        divisionListPreference.setAdapter(divisionAdapter)
        val divisionKey = "division_guid"
        divisionListPreference.key = divisionKey
        divisionListPreference.title = getString(R.string.division_title)
        val savedDivision = settingsViewModel.getDivisionByGuid(
            settingsViewModel.getPreferenceByKey<String>(divisionKey, "")  ?: ""
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
        if (warehouseListPreference.getDataListArray().isEmpty()) {
            settingsViewModel.getAllWarehouses()
        }

        categoryDocSettings.addPreference(warehouseListPreference)

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

        listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                when (key) {
                    "warehouse_guid" -> {
                        val warehouseGuidPrefSaved = sharedPrefs.getString(
                            "warehouse_guid",
                            ""
                        ).toString()
                        if (sharedPrefs.getString("division_guid", "").toString() == "") {
                            val division = settingsViewModel.getDivisionByGuid(
                                settingsViewModel.getWarehouseByGuid(
                                    warehouseGuidPrefSaved
                                )?.warehouseDivisionGuid ?: ""
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
                                            "division_guid",
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

    private fun getVirtualCellAdapter(): DynamicListAdapter<Cell> {
        return DynamicListAdapter<Cell>(requireContext(), R.layout.dynamic_prefs_layout)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val warehouseListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("warehouse_guid") as ExtendListPreference<Warehouse>

        val divisionListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("division_guid") as ExtendListPreference<Division>

        val virtualCellsListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("virtual_cell_guid") as ExtendListPreference<Cell>


        settingsViewModel.warehousesList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            val dataList = ArrayList<Warehouse>()
            dataList.add(Warehouse(getString(R.string.not_chosen_text), "", "", ""))
            it.forEach { warehouse ->
                dataList.add(warehouse)
            }
            //progressDialog?.dismiss()

            if (warehouseListPreference.getDataListArray().isEmpty())
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
                if (progressDialog?.isShowing == false || progressDialog == null)
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else
                progressDialog?.dismiss()

            if (it.error)
                DialogScreen.getDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    message = it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getAllWarehousesList" -> settingsViewModel.getAllWarehouses()
                                "getAllDivisions" -> settingsViewModel.getAllDivisions()
                            }

                        }
                    }
                )
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
}

