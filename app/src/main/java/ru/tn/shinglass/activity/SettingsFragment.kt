package ru.tn.shinglass.activity

//import ru.tn.shinglass.adapters.ru.tn.shinglass.adapters.DynamicListAdapter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.tn.shinglass.R
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.extendsPreferences.ExtendListPreference
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.data.api.ApiService
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
        warehouseListPreference.icon = requireContext().getDrawable(R.drawable.ic_baseline_warehouse_24)
        warehouseListPreference.setDialogTitle(getString(R.string.warehouse_list_description))
        warehouseListPreference.setOnPreferenceClickListener(object: Preference.OnPreferenceClickListener{
            override fun onPreferenceClick(preference: Preference): Boolean {
                if (warehouseListPreference.getDataListArray().isEmpty())
                    setWarehousestDataList(warehouseListPreference)
                return true
            }
        })
//        {
//            if (warehouseListPreference.getDataListArray().isEmpty())
//                setWarehousestDataList(warehouseListPreference)
//            //settingsViewModel.updateWarehousesDataList(apiService)
//            return@setOnPreferenceClickListener true
//        }
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

    //private fun getWarehousesList(): ArrayList<Warehouse> {
    private fun setWarehousestDataList(warehouseListPreference: ExtendListPreference<Warehouse>) {
        val arrayListWarehouse = ArrayList<Warehouse>()
        val warehouseListFromDb = getAllWarehousesFromDb()
        if (warehouseListFromDb.isNotEmpty()) {
            warehouseListFromDb.forEach { arrayListWarehouse.add(it) }
        } else {
            apiService?.getAllWarehousesList()?.enqueue(object : Callback<List<Warehouse>> {
                override fun onResponse(
                    call: Call<List<Warehouse>>,
                    response: Response<List<Warehouse>>
                ) {
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
                    return
                }

                override fun onFailure(call: Call<List<Warehouse>>, t: Throwable) {
                    //TODO:"Not yet implemented"
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

