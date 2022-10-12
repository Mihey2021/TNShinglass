package ru.tn.shinglass.activity

//import ru.tn.shinglass.adapters.ru.tn.shinglass.adapters.DynamicListAdapter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.viewModels
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
import ru.tn.shinglass.models.Warehouse
//import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel

private var apiService: ApiService? = null

class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val root = preferenceManager.preferenceScreen

        val categoryDocSettings = PreferenceCategory(requireContext())
        categoryDocSettings.key = "document_settings"
        categoryDocSettings.title = getString(R.string.documents_settings_title)
        //categoryDocSettings.summary = getString(R.string.settings_for_substituting_text)
        //categoryDocSettings.icon = requireContext().getDrawable(R.drawable.ic_baseline_settings_24)

        root.addPreference(categoryDocSettings)

        apiService = ApiUtils.getApiService(settingsViewModel.getBasicPreferences())

        val warehouseListPreference = ExtendListPreference<Warehouse>(requireContext())
        val adapter =
            DynamicListAdapter<Warehouse>(requireContext(), R.layout.dynamic_prefs_layout)
        warehouseListPreference.setAdapter(adapter)
        val warehouseKey = "warehouse_guid"
        warehouseListPreference.key = warehouseKey
        warehouseListPreference.title = getString(R.string.warehouse_title)
        val savedWarehouse = settingsViewModel.getWarehouseByGuid(
            settingsViewModel.getPreferenceByKey(warehouseKey) ?: ""
        )

        warehouseListPreference.summary =
            savedWarehouse?.title ?: getString(R.string.warehouse_list_description)
        warehouseListPreference.icon =
            resources.getDrawable(R.drawable.ic_baseline_warehouse_24, requireContext().theme)
        warehouseListPreference.setDialogTitle(getString(R.string.warehouse_list_description))
        warehouseListPreference.setOnPreferenceClickListener(object :
            Preference.OnPreferenceClickListener {
            override fun onPreferenceClick(preference: Preference): Boolean {
                if (warehouseListPreference.getDataListArray().isEmpty()) {

                    setWarehousesDataList(warehouseListPreference)
                    return true
                }
                return false
            }
        })

        val divisionListPreference = ExtendListPreference<Division>(requireContext())
        val divisionAdapter = DynamicListAdapter<Division>(requireContext(), R.layout.dynamic_prefs_layout)

        categoryDocSettings.addPreference(warehouseListPreference)

        val prefListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                settingsViewModel.getBasicPreferences()
                when (key) {
                    "serviceUrl" -> {
//                        serviceViewModel.updateBasicPreferences(requireContext(), sharedPrefs)
//                        val url = sharedPrefs.getString("serviceUrl", "").toString()
//                        val service1C = serviceViewModel.connectionService1C.value
//                        val okHttpClient = service1C?.okHttpClient
//                        val retrofit = service1C?.retrofit
//                        if (retrofit != null) {
//                            retrofit.newBuilder()
//                                .baseUrl(url)
//                                .client(okHttpClient)
//                                .addConverterFactory(GsonConverterFactory.create())
//                                .build()
//
//                            service1C.httpService1CUserApi = retrofit.create(UserApi::class.java)
//                        }
                    }

                }
            }

        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(prefListener)
    }

    private fun setWarehousesDataList(warehouseListPreference: ExtendListPreference<Warehouse>) {

        val progressDialog = DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)

        val arrayListWarehouse = ArrayList<Warehouse>()
        val warehouseListFromDb = getAllWarehousesFromDb()
        if (warehouseListFromDb.isNotEmpty()) {
            warehouseListFromDb.forEach { arrayListWarehouse.add(it) }
            warehouseListPreference.setDataListArray(arrayListWarehouse)
            progressDialog.cancel()
            warehouseListPreference.showDialog()
        } else {
            apiService?.getAllWarehousesList()?.enqueue(object : Callback<List<Warehouse>> {
                override fun onResponse(
                    call: Call<List<Warehouse>>,
                    response: Response<List<Warehouse>>
                ) {
                    progressDialog.cancel()
                    if (!response.isSuccessful) {
                        //TODO: Обработка не 2хх кода ответа
                        return
                    }

                    val warehousesList = response.body()
                    if (warehousesList == null) {
                        //TODO: Обработка пустого ответа
                        return
                    }
                    //Сохраним полученные склады в базу данных
                    settingsViewModel.save(warehousesList)
                    //Прочитаем из БД
                    getAllWarehousesFromDb().forEach { arrayListWarehouse.add(it) }
                    //Установим список
                    warehouseListPreference.setDataListArray(arrayListWarehouse)
                    //Покажем диалог выбора склада
                    warehouseListPreference.showDialog()
                    return
                }

                override fun onFailure(call: Call<List<Warehouse>>, t: Throwable) {
                    progressDialog.cancel()
                    DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR, t.message.toString(), onDialogsInteractionListener = object : OnDialogsInteractionListener{
                        override fun onPositiveClickButton() {
                            setWarehousesDataList(warehouseListPreference)
                        }
                    })
//                    DialogScreen.getDialogBuilder(
//                        requireContext(),
//                        DialogScreen.IDD_ERROR,
//                        t.message.toString()
//                    )
//                        .setNegativeButton(resources.getString(R.string.cancel_text)) { dialog, _ ->
//                            dialog.cancel()
//                        }
//                        .setPositiveButton(resources.getString(R.string.retry_loading)) { dialog, _ ->
//                            setWarehousesDataList(warehouseListPreference)
//                            dialog.dismiss()
//                        }
//                        .show()
                    return
                }

            })
        }

        warehouseListPreference.setDataListArray(arrayListWarehouse)
        //return arrayListWarehouse
    }


    private fun getAllWarehousesFromDb(): List<Warehouse> {
        return settingsViewModel.getAllWarehouses()
    }

}

