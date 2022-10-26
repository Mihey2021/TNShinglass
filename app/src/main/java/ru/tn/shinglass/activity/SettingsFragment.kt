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
            savedDivision?.title ?: getString(R.string.division_list_description)
        divisionListPreference.icon =
            resources.getDrawable(R.drawable.ic_baseline_division_24, requireContext().theme)
        divisionListPreference.setDialogTitle(getString(R.string.division_list_description))
        divisionListPreference.onPreferenceClickListener = object :
            Preference.OnPreferenceClickListener {
            override fun onPreferenceClick(preference: Preference): Boolean {
                if (divisionListPreference.getDataListArray().isEmpty()) {
                    setDivisionDataList(divisionListPreference)
                    return true
                }
                return false
            }
        }

        //Заполним список подразделений сразу при открытии
        if (divisionListPreference.getDataListArray().isEmpty()) {
            setDivisionDataList(divisionListPreference, showSelectionList = false)
        }
//
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
            savedWarehouse?.title ?: getString(R.string.warehouse_list_description)
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
                                )?.divisionGuid ?: ""
                            )
                            if (division != null) {
                                divisionListPreference.value = division.guid
                                divisionListPreference.summary = division.title
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
                                    )?.defaultWarehouseGuid ?: ""
                                )
                            if (warehouse != null) {
                                warehouseListPreference.value = warehouse.guid
                                warehouseListPreference.summary = warehouse.title
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

        settingsViewModel.warehousesList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe

            val dataList = ArrayList<Warehouse>()
            dataList.add(Warehouse(-1, getString(R.string.not_chosen_text), "", "", ""))
            it.forEach { warehouse ->
                dataList.add(warehouse)
            }
            //progressDialog?.dismiss()

            if (warehouseListPreference.getDataListArray().isEmpty())
                warehouseListPreference.setDataListArray(dataList)
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
                    title = it.errorMessage
                )
        }
    }

    private fun setDivisionDataList(
        divisionListPreference: ExtendListPreference<Division>,
        showSelectionList: Boolean = true
    ) {

        //val progressDialog = DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)

        val arrayListDivision = ArrayList<Division>()
        val divisionListFromDb = getAllDivisionsFromDb()
        if (divisionListFromDb.isNotEmpty()) {
            arrayListDivision.add(Division(-1, getString(R.string.not_chosen_text), "", ""))
            divisionListFromDb.forEach { arrayListDivision.add(it) }
            divisionListPreference.setDataListArray(arrayListDivision)
            //progressDialog.cancel()
            if (showSelectionList)
                divisionListPreference.showDialog()
        } else {
            apiService?.getAllDivisionsList()?.enqueue(object : Callback<List<Division>> {
                override fun onResponse(
                    call: Call<List<Division>>,
                    response: Response<List<Division>>
                ) {
                    //progressDialog.cancel()
                    if (!response.isSuccessful) {
                        //TODO: Обработка не 2хх кода ответа
                        return
                    }

                    val divisionsList = response.body()
                    if (divisionsList == null) {
                        //TODO: Обработка пустого ответа
                        return
                    }
                    //Сохраним полученные подразделения в базу данных
                    settingsViewModel.saveDivisions(divisionsList)
                    arrayListDivision.add(
                        Division(
                            -1,
                            getString(R.string.not_chosen_text),
                            "",
                            ""
                        )
                    )
                    //Прочитаем из БД
                    getAllDivisionsFromDb().forEach { arrayListDivision.add(it) }
                    //Установим список
                    divisionListPreference.setDataListArray(arrayListDivision)
                    //Покажем диалог выбора склада
                    if (showSelectionList)
                        divisionListPreference.showDialog()
                    return
                }

                override fun onFailure(call: Call<List<Division>>, t: Throwable) {
                    //progressDialog.cancel()
                    DialogScreen.getDialog(
                        requireContext(),
                        DialogScreen.IDD_ERROR,
                        t.message.toString(),
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                setDivisionDataList(divisionListPreference)
                            }
                        })
                    return
                }

            })
        }

        divisionListPreference.setDataListArray(arrayListDivision)
    }


    private fun getAllWarehouses() {
        settingsViewModel.getAllWarehouses()
    }

    private fun getAllDivisionsFromDb(): List<Division> {
        return settingsViewModel.getAllDivisions()
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

