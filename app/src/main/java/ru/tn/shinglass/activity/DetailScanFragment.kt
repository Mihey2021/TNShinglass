package ru.tn.shinglass.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
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
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.databinding.FragmentDesktopBinding
import ru.tn.shinglass.databinding.FragmentDetailScanBinding
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.PhisicalPerson
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDetailScanBinding.inflate(inflater, container, false)

        val selectedOption = arguments?.getSerializable("selectedOption") as Option
        val user1C = arguments?.getSerializable("userData") as User1C
        val itemBarCode = arguments?.getString("itemBarCode")

        with(binding) {
            operationTitleTextView.text = selectedOption.title
            //divisionTextView.setText("Подразделение из настроек")

            divisionTextInputLayout.error = "Подразделение не указано в настройках!".toString()


            val warehouseGuid = viewModel.getPreferenceByKey("warehouse_guid")
            if (warehouseGuid.isNullOrBlank()) {
                warehouseTextInputLayout.error = "В настройках не задан склад!".toString()
            } else {
                warehouseTextView.setText(viewModel.getWarehouseByGuid(warehouseGuid)?.title)
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
            }
//            warehouseTextInputLayout.setEndIconOnClickListener {
//                if (warehouseTextView.adapter == null) {
//                    progressDialog =
//                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
//                    getAllWarehousesList(binding)
//                }
//            }

            itemTextView.setText(itemBarCode)
            countEditText.setText("1.0")
            //countEditText.contentDescription = "шт."
            if (!countEditText.text.isNullOrBlank())
                countTextInputLayout.hint = "шт."

            //itemMeasureOfUnitTitleTextView.text = "шт."
            workwearDisposableCheckBox.isChecked = true
            purposeOfUseTextView.setText("Выбранное назначение использования")
            purposeOfUseTextView.setOnClickListener {
                //TODO: Обработка выбора назначения использования
                Toast.makeText(requireContext(), "Клик по ссылке", Toast.LENGTH_SHORT).show()
            }

            //phisicalPersonTextView.setText("Выбранное физическое лицо")

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
            }

            ownerTextView.setText("${getString(R.string.owner_title)} ${user1C.getUser1C()}")
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
            setWarehousesAdapter(it, binding)
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

    private fun setWarehousesAdapter(
        warehousesList: List<Warehouse>,
        binding: FragmentDetailScanBinding
    ) {

        val dataList = arrayListOf<Warehouse>()
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
            setWarehousesAdapter(dbWarehouses, binding)
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


}