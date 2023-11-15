package ru.tn.shinglass.activity.utilites.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.databinding.DocumentsHeadersInitDialogBinding
import ru.tn.shinglass.dto.models.DocumentHeaders
import ru.tn.shinglass.dto.models.HeaderFields
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import ru.tn.shinglass.viewmodel.TableScanFragmentViewModel
import java.text.SimpleDateFormat
import java.util.*

class HeadersDialogFragment : DialogFragment() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val viewModel: TableScanFragmentViewModel by viewModels()
    private val retrofitViewModel: RetrofitViewModel by viewModels()

    private val dataListWarehouses: ArrayList<Warehouse> = arrayListOf()
    private val dataListWarehousesReceiver: ArrayList<WarehouseReceiver> = arrayListOf()
    private val dataListPhysicalPersons: ArrayList<PhysicalPerson> = arrayListOf()
    private val dataListEmployees: ArrayList<Employee> = arrayListOf()
    private val dataListDivisions: ArrayList<Division> = arrayListOf()
    private val dataListCounterparties: ArrayList<Counterparty> = arrayListOf()

    private val args: HeadersDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dlgBinding = DocumentsHeadersInitDialogBinding.inflate(layoutInflater)
        val tableIsEmpty = args.tableIsEmpty //arguments?.getBoolean("tableIsEmpty", false) ?: false
        val isExternalDocument =
            args.isExternalDocument //arguments?.getBoolean("isExternalDocument", false) ?: false
        val docHeadersFields =
            args.selectedOption.subOption?.headerFields ?: arrayOf<HeaderFields>()
        val user1C = args.user1C
        val itemList = args.itemList.asList()

        if (docHeadersFields.isEmpty()) this.dismiss()

        BarcodeScannerReceiver.setEnabled(false)

        if (itemList.isNotEmpty()) dlgBinding.headerPositiveButton.text =
            getString(R.string.back_text)

        dlgBinding.headerPositiveButton.setOnClickListener {
            if (!checkHeadersDataFail(docHeadersFields)) this.dismiss()
        }

        initFields(
            dlgBinding = dlgBinding,
            docHeadersFields = docHeadersFields,
            tableIsEmpty = tableIsEmpty,
            isExternalDocument = isExternalDocument,
            user1C = user1C,
            itemList = itemList
        )

        viewModel.dataState.observe(viewLifecycleOwner)
        {
            if (it.loading) {
                closeDialogs()
                DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            }

            if (it.error) {
                //DialogScreen.getDialog(requireContext(), DialogScreen.IDD_ERROR, title = it.errorMessage)
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getAllWarehousesList" -> {
                                    viewModel.getAllWarehousesList()
                                }
                                "getAllWarehousesReceiverList" -> {
                                    viewModel.getAllWarehousesList(receiver = true)
                                }
                                "getAllPhysicalPerson" -> {
                                    viewModel.getAllPhysicalPerson()
                                }
//                                "createDocumentIn1C" ->
//                                    createDocumentIn1C()
                                "getCounterpartiesList" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "title")
                                        viewModel.getCounterpartiesList(title = requestParam.propertyValue)
                                }
                                "getEmployeesFromPartName" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "partName")
                                        viewModel.getEmployeesFromPartName(partName = requestParam.propertyValue)
                                }
                            }
                        }
                    })
            }
        }

        viewModel.divisionsList.observe(viewLifecycleOwner)
        {
            closeDialogs()
            if (it.isEmpty()) return@observe

            dataListDivisions.clear()
            dataListDivisions.add(Division(getString(R.string.not_chosen_text), "", ""))

            it.forEach { division ->
                dataListDivisions.add(division)
            }
        }

        viewModel.warehousesList.observe(viewLifecycleOwner)
        {
            closeDialogs()

            dataListWarehouses.clear()
            if (it.isEmpty())
                dataListWarehouses.add(Warehouse(getString(R.string.no_data), "", "", ""))
            else
                dataListWarehouses.add(Warehouse(getString(R.string.not_chosen_text), "", "", ""))

            it.forEach { warehouse ->
                dataListWarehouses.add(warehouse)
            }
        }

        viewModel.warehousesReceiverList.observe(viewLifecycleOwner)
        {
            closeDialogs()
            if (it.isEmpty()) return@observe

            dataListWarehousesReceiver.clear()
            if (it.isEmpty())
                dataListWarehouses.add(Warehouse(getString(R.string.no_data), "", "", ""))
            else
                dataListWarehousesReceiver.add(
                    WarehouseReceiver(
                        getString(R.string.not_chosen_text),
                        "",
                        "",
                        ""
                    )
                )

            it.forEach { warehouseReceiver ->
                dataListWarehousesReceiver.add(warehouseReceiver)
            }
        }

        viewModel.physicalPersons.observe(viewLifecycleOwner)
        {
            closeDialogs()
            if (it.isEmpty()) return@observe

            dataListPhysicalPersons.clear()
            dataListPhysicalPersons.add(PhysicalPerson(getString(R.string.not_chosen_text), ""))

            it.forEach { person ->
                dataListPhysicalPersons.add(person)
            }
        }

        viewModel.employeeList.observe(viewLifecycleOwner) {
            closeDialogs()
            if (it.isEmpty()) return@observe

            dataListEmployees.clear()
            dataListEmployees.add(Employee(getString(R.string.not_chosen_text), ""))

            it.forEach { employee ->
                dataListEmployees.add(employee)
            }

            val employeeAdapter = DynamicListAdapter<Employee>(
                requireContext(),
                R.layout.dynamic_prefs_layout,
                dataListEmployees
            )

            (dlgBinding.employeeTextView as? AutoCompleteTextView)?.setAdapter(employeeAdapter)

            if (dlgBinding.employeeTextView.text.isNotEmpty() && dataListEmployees.isEmpty()) {
                closeDialogs()
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_SUCCESS,
                    getString(R.string.enter_part_of_employee_name),
                    getString(R.string.nothing_found_text),
                    titleIcon = R.drawable.ic_baseline_search_off_24
                )
            } else {
                if (DocumentHeaders.getEmployee() == null) dlgBinding.employeeTextView.showDropDown()
            }
        }

//        viewModel.employees.observe(viewLifecycleOwner)
//        {
//            closeDialogs()
//            if (it.isEmpty()) return@observe
//
//            dataListEmployees.clear()
//            dataListEmployees.add(Employee(getString(R.string.not_chosen_text), ""))
//
//            it.forEach { employee ->
//                dataListEmployees.add(employee)
//            }
//
//            val employeeAdapter = DynamicListAdapter<Employee>(
//                requireContext(),
//                R.layout.dynamic_prefs_layout,
//                dataListEmployees
//            )
//
//            (dlgBinding.employeeTextView as? AutoCompleteTextView)?.setAdapter(employeeAdapter)
//
//            if (dlgBinding.employeeTextView.text.isNotEmpty() && dataListEmployees.isEmpty()) {
//                closeDialogs()
//                DialogScreen.showDialog(
//                    requireContext(),
//                    DialogScreen.IDD_SUCCESS,
//                    getString(R.string.enter_part_of_employee_name),
//                    getString(R.string.nothing_found_text),
//                    titleIcon = R.drawable.ic_baseline_search_off_24
//                )
//            } else {
//                if (DocumentHeaders.getEmployee() == null) dlgBinding.employeeTextView.showDropDown()
//            }
//        }

        viewModel.counterpartiesList.observe(viewLifecycleOwner)
        {
            closeDialogs()
            dataListCounterparties.clear()
            it.forEach { counterparty ->
                dataListCounterparties.add(counterparty)
            }

            val adapter = DynamicListAdapter<Counterparty>(
                requireContext(),
                R.layout.counterparty_item_layout,
                dataListCounterparties
            )
            (dlgBinding.counterpartyTextEdit as? AutoCompleteTextView)?.setAdapter(adapter)
            if (dlgBinding.counterpartyTextEdit.text.isNotEmpty() && dataListCounterparties.isEmpty()) {
                closeDialogs()
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_SUCCESS,
                    getString(R.string.specify_name_or_inn_text),
                    getString(R.string.nothing_found_text),
                    titleIcon = R.drawable.ic_baseline_search_off_24
                )
            } else {
                dlgBinding.counterpartyTextEdit.showDropDown()
            }
        }

        retrofitViewModel.physicalPerson.observe(viewLifecycleOwner) {
            //if (it.physicalPersonGuid.isEmpty()) return@observe
            if (DocumentHeaders.getPhysicalPerson()?.physicalPersonGuid?.isEmpty() == false) return@observe
            fillResponsible(
                it.physicalPersonGuid,
                dlgBinding.physicalPersonTextView
            )
        }

        retrofitViewModel.dataState.observe(viewLifecycleOwner) {
            if (it.loading) {
                if (DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.isShowing == false || DialogScreen.getDialog(
                        DialogScreen.IDD_PROGRESS
                    ) == null
                )
                    DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
            } else {
                DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
            }

            if (it.error) {
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getPhysicalPersonFormUser" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "userGUID")
                                        retrofitViewModel.getPhysicalPersonFormUser(userGUID = requestParam.propertyValue)
                                }
                            }
                        }
                    }
                )
            }
        }

//
//
//        var dialogHeadersNeedOpen = checkHeadersDataFail()
//        return //inflater.inflate(R.layout.documents_headers_init_dialog, container, false)
        return dlgBinding.root
    }

    private fun closeDialogs() {
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.dismiss()
        DialogScreen.getDialog()?.dismiss()
    }

    private fun fillResponsible(
        responsibleGuid: String,
        physicalPersonTextView: AutoCompleteTextView
    ) {
        val physicalPerson = viewModel.getPhysicalPersonByGuid(responsibleGuid)
        DocumentHeaders.setPhysicalPerson(physicalPerson)
        physicalPersonTextView.setText(physicalPerson?.physicalPersonFio ?: "")
    }

    private fun getCounterpartyHelperText(counterparty: Counterparty?): CharSequence? {
        return if (counterparty == null) null else "ИНН: ${counterparty.inn}. КПП: ${counterparty.kpp}"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_DialogWhenLarge)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_DialogWhenLarge_NoActionBar)
    }

    private fun checkHeadersDataFail(docHeadersFields: Array<HeaderFields>): Boolean {
        var isNotCorrect = false
        var fieldValueIsNotCorrect = false
        val dialogForm = requireDialog()
        if (docHeadersFields?.isEmpty() == true)
            return true

        docHeadersFields.forEach {
            if (it == HeaderFields.DIVISION) {
                fieldValueIsNotCorrect = DocumentHeaders.getDivision() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val divisionTextInputLayout =
                    dialogForm.findViewById<TextInputLayout>(HeaderFields.DIVISION.viewId)
                if (fieldValueIsNotCorrect) {
                    divisionTextInputLayout?.error = getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                divisionTextInputLayout?.error = null
            }
            if (it == HeaderFields.WAREHOUSE) {
                fieldValueIsNotCorrect = DocumentHeaders.getWarehouse() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val warehouseTextInputLayout =
                    dialogForm.findViewById<TextInputLayout>(HeaderFields.WAREHOUSE.viewId)
                if (fieldValueIsNotCorrect) {
                    warehouseTextInputLayout?.error = getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                warehouseTextInputLayout?.error = null
            }
            if (it == HeaderFields.WAREHOUSE_RECEIVER) {
                fieldValueIsNotCorrect = DocumentHeaders.getWarehouseReceiver() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val warehouseReceiverTextInputLayout =
                    dialogForm.findViewById<TextInputLayout>(HeaderFields.WAREHOUSE_RECEIVER.viewId)
                if (fieldValueIsNotCorrect) {
                    warehouseReceiverTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                warehouseReceiverTextInputLayout?.error = null
            }
            if (it == HeaderFields.PHYSICAL_PERSON) {
                fieldValueIsNotCorrect = DocumentHeaders.getPhysicalPerson() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val physicalPersonTextInputLayout =
                    dialogForm.findViewById<TextInputLayout>(HeaderFields.PHYSICAL_PERSON.viewId)
                if (fieldValueIsNotCorrect) {
                    physicalPersonTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                physicalPersonTextInputLayout?.error = null
            }
            if (it == HeaderFields.EMPLOYEE) {
                fieldValueIsNotCorrect = DocumentHeaders.getEmployee() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val employeeTextInputLayout =
                    dialogForm.findViewById<TextInputLayout>(HeaderFields.EMPLOYEE.viewId)
                if (fieldValueIsNotCorrect) {
                    employeeTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                employeeTextInputLayout?.error = null
            }
            if (it == HeaderFields.COUNTERPARTY) {
                fieldValueIsNotCorrect = DocumentHeaders.getCounterparty() == null
                isNotCorrect = (fieldValueIsNotCorrect || isNotCorrect)
                val counterpartyTextInputLayout =
                    dialogForm.findViewById<TextInputLayout>(HeaderFields.COUNTERPARTY.viewId)
                if (fieldValueIsNotCorrect) {
                    counterpartyTextInputLayout?.error =
                        getString(R.string.field_must_be_filled_text)
                    return@forEach
                }
                counterpartyTextInputLayout?.error = null
            }
        }
        return isNotCorrect
    }

    private fun initFields(
        dlgBinding: DocumentsHeadersInitDialogBinding,
        docHeadersFields: Array<HeaderFields>,
        tableIsEmpty: Boolean,
        isExternalDocument: Boolean,
        user1C: User1C,
        itemList: List<TableScan>
    ) {

        with(dlgBinding) {

            if (docHeadersFields.contains(HeaderFields.DIVISION)) {
                val divisionGuidByPrefs =
                    settingsViewModel.getPreferenceByKey<String>("division_guid", "")
                if (divisionGuidByPrefs.isNullOrBlank())
                    divisionTextInputLayout.error = "Подразделение не задано в настройках!"

                val divisionAdapter = DynamicListAdapter<Division>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListDivisions,
                    filterOff = true
                )

                divisionTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) divisionTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                divisionTextView.setAdapter(divisionAdapter)



                if (!isExternalDocument) {
                    var division = settingsViewModel.getDivisionByGuid(divisionGuidByPrefs ?: "")
                    if (division == null)
                        division = viewModel.getDivisionByGuid(user1C.getDefaultDivisionGUID())
                    if (DocumentHeaders.getDivision() == null) {
                        DocumentHeaders.setDivision(division)
                        val position = divisionAdapter.getPosition(division)
                        if (position != -1)
                        //divisionTextView.setSelection(position)
                            divisionTextView.listSelection = position
                    }
                }
                divisionTextView.setText(DocumentHeaders.getDivision()?.divisionTitle)
                divisionTextInputLayout.error = null

                divisionTextView.setOnClickListener {
                    if (divisionTextView.adapter == null || divisionTextView.adapter.count == 0) {
                        viewModel.getAllDivisions()
                    }
                }

                divisionTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val divisionItem = adapterView.getItemAtPosition(position) as Division
                    if (divisionItem.divisionGuid == "") {
                        DocumentHeaders.setDivision(null)
                    } else {
                        DocumentHeaders.setDivision(divisionItem)
                    }

                    divisionTextView.setText(divisionItem.divisionTitle)
                    divisionTextInputLayout.error = null
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                divisionTextInputLayout.isVisible = false
            }

            if (docHeadersFields.contains(HeaderFields.WAREHOUSE) == true) {
                val warehouseGuidByPrefs =
                    settingsViewModel.getPreferenceByKey<String>("warehouse_guid", "")
                if (warehouseGuidByPrefs.isNullOrBlank())
                    warehouseTextInputLayout.error = "Склад не задан в настройках!"

                val warehousesAdapter = DynamicListAdapter(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListWarehouses
                )

                warehouseTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) warehouseTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                warehouseTextView.setAdapter(warehousesAdapter)

                if (!isExternalDocument) {
                    val warehouse = settingsViewModel.getWarehouseByGuid(warehouseGuidByPrefs ?: "")
                    if (DocumentHeaders.getWarehouse() == null)
                        DocumentHeaders.setWarehouse(warehouse)
                }
                warehouseTextView.setText(DocumentHeaders.getWarehouse()?.warehouseTitle)
                warehouseTextInputLayout.error = null

                warehouseTextView.setOnClickListener {
                    if (warehouseTextView.adapter == null || warehouseTextView.adapter.count == 0) {
                        viewModel.getAllWarehousesList()
                    }
                }

                warehouseTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val warehouseItem = adapterView.getItemAtPosition(position) as Warehouse
                    if (warehouseItem.warehouseGuid == "") {
                        DocumentHeaders.setWarehouse(null)
                    } else {
                        DocumentHeaders.setWarehouse(warehouseItem)
                    }
                    //if (physicalPersonTextView.text.isNullOrBlank()) {
                    //Если Смета (ТОиР), установим МОЛ авторизовавшегося пользователя, а не МОЛ склада из настроек
                    if (args.selectedOption.subOption == SubOptionType.TOIR_REPAIR_ESTIMATE) {
                        retrofitViewModel.getPhysicalPersonFormUser(args.user1C.getUserGUID())
                    } else {
                        fillResponsible(
                            DocumentHeaders.getWarehouse()?.warehouseResponsibleGuid ?: "",
                            dlgBinding.physicalPersonTextView
                        )
                    }
                    //}
                    warehouseTextView.setText(warehouseItem.warehouseTitle)
                    warehouseTextInputLayout.error = null
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                warehouseTextInputLayout.isVisible = false
            }

            if (docHeadersFields.contains(HeaderFields.WAREHOUSE_RECEIVER) == true) {
                val warehousesReceiverAdapter = DynamicListAdapter<WarehouseReceiver>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListWarehousesReceiver
                )

                warehouseReceiverTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) warehouseReceiverTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                warehouseReceiverTextView.setAdapter(warehousesReceiverAdapter)

                warehouseReceiverTextView.setText(DocumentHeaders.getWarehouseReceiver()?.warehouseReceiverTitle)
                warehouseReceiverTextInputLayout.error = null

                warehouseReceiverTextView.setOnClickListener {
                    if (warehouseReceiverTextView.adapter == null || warehouseReceiverTextView.adapter.count == 0) {
                        viewModel.getAllWarehousesList(receiver = true)
                    }
                }

                warehouseReceiverTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val warehouseReceiverItem =
                        adapterView.getItemAtPosition(position) as WarehouseReceiver
                    if (warehouseReceiverItem.warehouseReceiverGuid == "") {
                        DocumentHeaders.setWarehouseReceiver(null)
                    } else {
                        DocumentHeaders.setWarehouseReceiver(warehouseReceiverItem)
                    }
                    warehouseReceiverTextView.setText(warehouseReceiverItem.warehouseReceiverTitle)
                    warehouseReceiverTextInputLayout.error = null
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                warehouseReceiverTextInputLayout.isVisible = false
            }

            if (docHeadersFields.contains(HeaderFields.PHYSICAL_PERSON) == true) {
                val physicalPersonAdapter = DynamicListAdapter<PhysicalPerson>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    dataListPhysicalPersons
                )

                physicalPersonTextView.isEnabled = tableIsEmpty

                if (!tableIsEmpty) physicalPersonTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                physicalPersonTextView.setAdapter(physicalPersonAdapter)

                physicalPersonTextInputLayout.hint = "МОЛ"
                if (DocumentHeaders.getPhysicalPerson() != null)
                    physicalPersonTextView.setText(DocumentHeaders.getPhysicalPerson()?.physicalPersonFio)
                physicalPersonTextView.setOnClickListener {
                    if (physicalPersonTextView.adapter == null || physicalPersonTextView.adapter.count == 0) {
                        viewModel.getAllPhysicalPerson()
                    }
                }
                physicalPersonTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val physicalPerson = adapterView.getItemAtPosition(position) as PhysicalPerson
                    if (physicalPerson.physicalPersonGuid == "") {
                        DocumentHeaders.setPhysicalPerson(null)
                    } else {
                        DocumentHeaders.setPhysicalPerson(physicalPerson)
                    }
                    physicalPersonTextView.setText(physicalPerson?.physicalPersonFio)
                    physicalPersonTextInputLayout.error = null
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(dlgBinding.root)
                }

                if (args.selectedOption.subOption == SubOptionType.TOIR_REPAIR_ESTIMATE) {
                    retrofitViewModel.getPhysicalPersonFormUser(args.user1C.getUserGUID())
                } else {
                    if (DocumentHeaders.getPhysicalPerson()?.physicalPersonGuid?.isEmpty() != false) {
                        fillResponsible(
                            DocumentHeaders.getWarehouse()?.warehouseResponsibleGuid ?: "",
                            dlgBinding.physicalPersonTextView
                        )
                    }
                }
            } else {
                physicalPersonTextInputLayout.isVisible = false
            }

            if (docHeadersFields.contains(HeaderFields.EMPLOYEE)) {
//                val employeeAdapter = DynamicListAdapter<Employee>(
//                    requireContext(),
//                    R.layout.dynamic_prefs_layout,
//                    dataListEmployees
//                )
//
//                employeeTextView.setAdapter(employeeAdapter)

                employeeTextView.isEnabled = tableIsEmpty

                if (DocumentHeaders.getEmployee() != null) {
                    employeeTextView.inputType = android.text.InputType.TYPE_NULL
                    employeeTextInputLayout.endIconDrawable =
                        androidx.appcompat.content.res.AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_clear_24
                        )
                    employeeTextInputLayout.endIconContentDescription =
                        getString(R.string.clear_text)
                }

                if (!tableIsEmpty) employeeTextInputLayout.endIconMode =
                    TextInputLayout.END_ICON_NONE

                if (DocumentHeaders.getEmployee() != null)
                    employeeTextView.setText(DocumentHeaders.getEmployee()?.employeeFio)
//                employeeTextView.setOnClickListener {
//                    if (employeeTextView.adapter == null || employeeTextView.adapter.count == 0) {
//                        viewModel.getAllEmployees()
//                    }

                employeeTextView.setOnClickListener {
                    if (employeeTextInputLayout.error == null || !tableIsEmpty) return@setOnClickListener
                    employeeTextInputLayout.error = null
                    employeeTextView.text = null
                    employeeTextView.inputType = android.text.InputType.TYPE_CLASS_TEXT
                    employeeTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    employeeTextInputLayout.endIconDrawable =
                        androidx.appcompat.content.res.AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_search_24
                        )
                    employeeTextInputLayout.endIconContentDescription =
                        getString(R.string.find_text)
                }

                employeeTextInputLayout.setEndIconOnClickListener {
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(employeeTextView)
                    if (employeeTextView.inputType != android.text.InputType.TYPE_NULL) {
                        viewModel.getEmployeesFromPartName(employeeTextView.text.toString())
                    } else {
                        DocumentHeaders.setEmployee(null)
                        employeeTextView.text = null
                        employeeTextInputLayout.helperText = null
                        employeeTextView.inputType = android.text.InputType.TYPE_CLASS_TEXT
                        employeeTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        employeeTextInputLayout.endIconDrawable =
                            androidx.appcompat.content.res.AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_search_24
                            )
                        employeeTextInputLayout.endIconContentDescription =
                            getString(R.string.find_text)
                    }
                }

                employeeTextView.setOnItemClickListener { adapterView, _, position, _ ->
                    val employee = adapterView.getItemAtPosition(position) as Employee
                    if (employee.employeeGuid == "") {
                        DocumentHeaders.setEmployee(null)
                    } else {
                        DocumentHeaders.setEmployee(employee)
                    }
                    employeeTextView.setText(employee?.employeeFio)
                    employeeTextView.inputType = android.text.InputType.TYPE_NULL
                    employeeTextInputLayout.endIconDrawable =
                        androidx.appcompat.content.res.AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_clear_24
                        )
                    employeeTextInputLayout.endIconContentDescription =
                        getString(R.string.clear_text)
                    employeeTextInputLayout.error = null
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(dlgBinding.root)
                }
            } else {
                employeeTextInputLayout.isVisible = false
            }

            if (docHeadersFields?.contains(HeaderFields.COUNTERPARTY) == false) {
                counterpartyTextInputLayout.isVisible = false
            } else {

                counterpartyTextInputLayout.isEnabled = itemList.isEmpty()
                val counterparty =
                    if (itemList.isNotEmpty()) itemList[0].docHeaders.getCounterparty() else null
                if (DocumentHeaders.getCounterparty() == null)
                    DocumentHeaders.setCounterparty(counterparty)
                counterpartyTextEdit.setText(DocumentHeaders.getCounterparty()?.title)
                counterpartyTextInputLayout.helperText =
                    getCounterpartyHelperText(DocumentHeaders.getCounterparty())

                counterpartyTextEdit.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        counterpartyTextInputLayout.error = null
                        counterpartyTextInputLayout.helperText = null
                    }

                    override fun afterTextChanged(p0: Editable?) {
                    }

                })

                counterpartyTextInputLayout.setEndIconOnClickListener {
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(counterpartyTextEdit)
                    if (counterpartyTextEdit.inputType != android.text.InputType.TYPE_NULL) {
                        viewModel.getCounterpartiesList(counterpartyTextEdit.text.toString())
                    } else {
                        DocumentHeaders.setCounterparty(null)
                        counterpartyTextEdit.text = null
                        counterpartyTextInputLayout.helperText = null
                        counterpartyTextEdit.inputType = android.text.InputType.TYPE_CLASS_TEXT
                        counterpartyTextInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        counterpartyTextInputLayout.endIconDrawable =
                            androidx.appcompat.content.res.AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_search_24
                            )
                        counterpartyTextInputLayout.endIconContentDescription =
                            getString(R.string.find_text)
                    }
                }

                counterpartyTextEdit.setOnItemClickListener { adapterView, _, position, _ ->
                    ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(counterpartyTextEdit)
                    val counterparty = adapterView.getItemAtPosition(position) as Counterparty
                    DocumentHeaders.setCounterparty(counterparty)
                    counterpartyTextEdit.setText(counterparty?.title)
                    counterpartyTextEdit.inputType = android.text.InputType.TYPE_NULL
                    counterpartyTextInputLayout.endIconDrawable =
                        androidx.appcompat.content.res.AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_clear_24
                        )
                    counterpartyTextInputLayout.endIconContentDescription =
                        getString(R.string.clear_text)
                    counterpartyTextInputLayout.error = null
                    counterpartyTextInputLayout.helperText = getCounterpartyHelperText(counterparty)

                }
            }

            if (docHeadersFields?.contains(HeaderFields.INCOMING_DATE) == false) {
                incomingDateTextInputLayout.isVisible = false
            } else {
                incomingDateTextInputLayout.isEnabled = itemList.isEmpty()
                val incomingDate =
                    if (itemList.isNotEmpty()) itemList[0].docHeaders.getIncomingDate() else null
                if (DocumentHeaders.getIncomingDate() == null)
                    DocumentHeaders.setIncomingDate(incomingDate)
                val userDateAlready = DocumentHeaders.getIncomingDate() != null
                val userDate =
                    if (userDateAlready) DocumentHeaders.getIncomingDate() else com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds()
                val sdf = SimpleDateFormat("dd.MM.yyyy")
                incomingDateEditText.setText(if (userDateAlready) sdf.format(Date(userDate!!)) else null)
                incomingDateTextInputLayout.setEndIconOnClickListener {
                    if (incomingDateTextInputLayout.endIconContentDescription == getString(R.string.clear_text)) {
                        DocumentHeaders.setIncomingDate(null)
                        incomingDateEditText.text = null
                        incomingDateTextInputLayout.endIconDrawable =
                            androidx.appcompat.content.res.AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_calendar_24
                            )
                        incomingDateTextInputLayout.endIconContentDescription =
                            getString(R.string.incoming_date_text)
                        return@setEndIconOnClickListener
                    }
                    val datePicker =
                        com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                            .setTitleText(R.string.incoming_date_text)
                            .setSelection(userDate)
                            .build()
                    datePicker.show(requireActivity().supportFragmentManager, "datePicker")
                    datePicker.addOnPositiveButtonClickListener {
                        DocumentHeaders.setIncomingDate(it)
                        incomingDateEditText.setText(sdf.format(Date(it)))
                        incomingDateTextInputLayout.endIconDrawable =
                            androidx.appcompat.content.res.AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_clear_24
                            )
                        incomingDateTextInputLayout.endIconContentDescription =
                            getString(R.string.clear_text)
                    }
                }
            }

            if (docHeadersFields?.contains(HeaderFields.INCOMING_NUMBER) == false) {
                incomingNumberTextInputLayout.isVisible = false
            } else {
                incomingNumberTextInputLayout.isEnabled = itemList.isEmpty()
                val incomingNumber =
                    if (itemList.isNotEmpty()) itemList[0].docHeaders.getIncomingNumber() else ""
                if (DocumentHeaders.getIncomingNumber() == "")
                    DocumentHeaders.setIncomingNumber(incomingNumber)
                incomingNumberEditText.setText(DocumentHeaders.getIncomingNumber())
                incomingNumberEditText.addTextChangedListener(
                    object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            DocumentHeaders.setIncomingNumber(s.toString())
                        }

                        override fun afterTextChanged(s: Editable?) {
//                            if (s != null && s.isNotEmpty()) {
//
//                            }
                        }
                    }
                )
            }

        }
    }

    override fun onDestroyView() {
        BarcodeScannerReceiver.setEnabled()
        super.onDestroyView()
    }

}

