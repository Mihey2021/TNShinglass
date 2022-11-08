package ru.tn.shinglass.activity

//import ru.tn.shinglass.adapters.ru.tn.shinglass.adapters.DynamicListAdapter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.extendsComponents.ExtendListPreference
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.data.api.ApiService
import ru.tn.shinglass.models.Division
import ru.tn.shinglass.models.PhysicalPerson
import ru.tn.shinglass.models.Warehouse
import ru.tn.shinglass.viewmodel.SettingsViewModel

private var apiService: ApiService? = null
private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
private var progressDialog: AlertDialog? = null

class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel: SettingsViewModel by viewModels()

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
            settingsViewModel.getPreferenceByKey(divisionKey) ?: ""
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
        warehouseListPreference
        val savedWarehouse = settingsViewModel.getWarehouseByGuid(
            settingsViewModel.getPreferenceByKey(warehouseKey) ?: ""
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

        listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                when (key) {
                    "warehouse_guid" -> {
                        if (sharedPrefs.getString("division_guid", "").toString() == "") {
                            val division = settingsViewModel.getDivisionByGuid(
                                settingsViewModel.getWarehouseByGuid(
                                    sharedPrefs.getString(
                                        "warehouse_guid",
                                        ""
                                    ).toString()
                                )?.warehouseDivisionGuid ?: ""
                            )
                            if (division != null) {
                                divisionListPreference.value = division.divisionGuid
                                divisionListPreference.summary = division.divisionTitle
                            }
                        }
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
                            }
                        }
                    }
                }
            }

        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val warehouseListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("warehouse_guid") as ExtendListPreference<Warehouse>

        val divisionListPreference =
            preferenceManager.preferenceScreen.findPreference<ListPreference>("division_guid") as ExtendListPreference<Division>


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

