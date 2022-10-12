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

    private var dataList = arrayListOf<PhisicalPerson>()
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
            val warehouseGuid = viewModel.getPreferenceByKey("warehouse_guid")
            if (warehouseGuid.isNullOrBlank()) {
                divisionTextInputLayout.error = "Подразделение не указано в настройках!".toString()
            } else {
                divisionTextView.setText(viewModel.getWarehouseByGuid(warehouseGuid)?.title)
                divisionTextInputLayout.error = null
            }

            warehouseTextView.setText("Склад из натроек")
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
                //dataList = setPhisicalPersonDataList()
                if (dataList.isNotEmpty()) {
                    val adapter = DynamicListAdapter<PhisicalPerson>(
                        requireContext(),
                        R.layout.dynamic_prefs_layout,
                        dataList
                    )
                    phisicalPersonTextView.setAdapter(adapter)
                } else {
                    getPhysicalPersonList()
                    progressDialog =
                        DialogScreen.getDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                }
            }

            ownerTextView.setText("${getString(R.string.owner_title)} ${user1C.getUser1C()}")
        }

        retrofitViewModel.listDataPhisicalPersons.observe(viewLifecycleOwner) {
            it.forEach { person -> dataList.add(person) }
            progressDialog?.dismiss()
            binding.phisicalPersonTextView.callOnClick()
        }

        retrofitViewModel.requestError.observe(viewLifecycleOwner) {error ->
            progressDialog?.dismiss()
            DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR, error.message, null, object : OnDialogsInteractionListener {
                override fun onPositiveClickButton() {
                    if(error.requestName == "getPhysicalPersonList")
                        progressDialog?.show()
                        getPhysicalPersonList()
                }
            })
        }


        return binding.root
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