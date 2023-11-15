package ru.tn.shinglass.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputLayout
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.AndroidUtils
import ru.tn.shinglass.activity.utilites.SoundPlayer
import ru.tn.shinglass.activity.utilites.SoundType
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.HeadersDialogFragmentDirections
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.*
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.databinding.FragmentStocksBinding
import ru.tn.shinglass.models.*
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel

enum class ChipType(val tag: String) {
    CELL_CHIP("cellChip"),
    NOMENCLATURE_CHIP("nomenclatureChip"),
    GVZO_CHIP("gvzoChip"),
    GVZO_FILTER_CHIP("byGvzo"),
}


class StocksFragment : Fragment() {

    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private var nomenclature: Nomenclature? = null
    private var gvzo: Gvzo? = null
    private var cell: Cell? = null
    private var headerTitle: TextView? = null
    private var headerCell: TextView? = null
    private var warehouse: Warehouse? = null

    private var byGvzo: Boolean = false
    private var chipCloseClick: Boolean = false

    private var stocksAdapter =
        NomenclatureStocksAdapter(onStocksItemInteractionListener = getItemClickListener())

    override fun onContextItemSelected(item: MenuItem): Boolean {
        var position: NomenclatureStocks? = null
        try {
            position = stocksAdapter.getPosition()
        } catch (e: Exception) {
            Log.d("CONTEXT_MENU_ERROR", e.localizedMessage, e)
            return super.onContextItemSelected(item)
        }
        when (item.itemId) {
            IDM_PRINT_CELL -> {
//                Toast.makeText(requireContext(), "Печать ${position?.cell}", Toast.LENGTH_SHORT)
//                    .show()
                val direction = BarcodePrintFragmentDirections.actionGlobalBarcodePrintFragment(
                    cellArg = position?.cell,
                    nomenclatureArg = null
                )
                findNavController().navigate(direction)
            }
            IDM_PRINT_ITEM -> {
//                Toast.makeText(
//                    requireContext(),
//                    "Печать ${position?.nomenclature}",
//                    Toast.LENGTH_SHORT
//                ).show()
                val direction = BarcodePrintFragmentDirections.actionGlobalBarcodePrintFragment(
                    nomenclatureArg = position?.nomenclature,
                    cellArg = null
                )
                findNavController().navigate(direction)
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun getItemClickListener(): OnStocksItemInteractionListener =
        object : OnStocksItemInteractionListener {
            override fun selectItem(item: NomenclatureStocks) {
                //val binding = FragmentStocksBinding.inflate(LayoutInflater.from(requireContext()), )
                if (item.isGroup) return
                val byCell = getSearchGroupingState()
                val recyclerView = requireView().findViewById<RecyclerView>(R.id.list)
                BarcodeScannerReceiver.setEnabled(false)
                DialogScreen.getDialog()?.dismiss()
                if (byCell) {
                    DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_QUESTION,
                        title = getString(R.string.stocks),
                        message = "${getString(R.string.show_balances_by)} \"${item.nomenclature.itemTitle}\"?",
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                BarcodeScannerReceiver.setEnabled()
                                nomenclature = item.nomenclature
                                gvzo = null
                                val nomenclatureTextView =
                                    requireView().findViewById<AutoCompleteTextView>(R.id.nomenclatureTextEdit)
                                val nomenclatureTextInputLayout =
                                    requireView().findViewById<TextInputLayout>(R.id.nomenclatureTextInputLayout)
                                val gvzoSwitcher =
                                    requireView().findViewById<MaterialSwitch>(R.id.gvzoSwitcher)
                                nomenclatureTextView.setText(item.nomenclature.itemTitle)
                                nomenclatureTextInputLayout.helperText =
                                    "Код: ${nomenclature?.code ?: ""}"
                                clearSearchParamsInForm(isNomenclature = false)
                                setClearEndIcon(isNomenclature = true)
                                prepareChips()
                                gvzoSwitcher.isChecked = false
                                getNomenclatureStocks(recyclerView)
                                super.onPositiveClickButton()
                            }

                            override fun onNegativeClickButton() {
                                BarcodeScannerReceiver.setEnabled()
                                super.onNegativeClickButton()
                            }
                        }
                    )
                } else {
                    DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_QUESTION,
                        title = getString(R.string.stocks),
                        message = "${getString(R.string.show_balances_by)} \"${item.cell.title}\"?",
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                BarcodeScannerReceiver.setEnabled()
                                cell = item.cell
                                val cellTextView =
                                    requireView().findViewById<AutoCompleteTextView>(R.id.cellTextEdit)
                                cellTextView.setText(item.cell.title)
                                //clearSearchParamsInForm(isNomenclature = true, clearTextField = true, clearGvzo = true)

                                setClearEndIcon(isNomenclature = false)
                                refreshSearchParamsByCellChange()
                                prepareChips()
                                getNomenclatureStocks(recyclerView)
                                super.onPositiveClickButton()
                            }

                            override fun onNegativeClickButton() {
                                BarcodeScannerReceiver.setEnabled()
                                super.onNegativeClickButton()
                            }
                        }
                    )
                }
            }
        }

    private fun refreshSearchParamsByCellChange() {
        if (byGvzo) {
            clearSearchParamsInForm(
                isNomenclature = true,
                clearTextField = false,
                clearGvzo = false
            )
            setClearEndIcon(isNomenclature = true)
        } else {
            clearSearchParamsInForm(
                isNomenclature = true,
                clearTextField = true,
                clearGvzo = true
            )
        }
    }

    private fun getSearchGroupingState(): Boolean =
        (nomenclature?.itemGuid.isNullOrBlank() && !cell?.guid.isNullOrBlank())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentStocksBinding.inflate(inflater, container, false)
        var nomenclatureAdapter: DynamicListAdapter<Nomenclature>
        var cellAdapter: DynamicListAdapter<Cell>
        var gvzoAdapter: DynamicListAdapter<Gvzo>

        binding.apply {
            headerTitle = tableScanHeaders.headerTitleTextView
            headerCell = tableScanHeaders.headerCellTextView

            searchGroupButton.setOnClickListener {
                setChipsGroupVisibility()
            }

            warehouseChip.setOnClickListener {
                if (warehouse == null)
                    findNavController().navigate(R.id.action_global_settingsFragment)
            }

            gvzoSwitcher.setOnCheckedChangeListener { buttonView, isChecked ->
                AndroidUtils.hideKeyboard(nomenclatureTextEdit)
                if (isChecked)
                    addNewChip(ChipType.GVZO_FILTER_CHIP, getString(R.string.by_gvzo_text))
                else
                    deleteChip(ChipType.GVZO_FILTER_CHIP)
                //setGvzoChipText()
                var text = getString(R.string.nomenclature_title)
                if (isChecked) text = getString(R.string.gvzo_text)
                nomenclatureTextInputLayout.hint = text
                nomenclatureTextInputLayout.endIconContentDescription = text
                //clearSearchParamsInForm(isNomenclature = true)
                byGvzo = isChecked
                val nomenclatureText = nomenclatureTextEdit.text.toString()
                if (!byGvzo) {
                    gvzo = null
                    if (nomenclatureText.isNotEmpty() && nomenclatureText.length >= 3)
                        retrofitViewModel.getItemByTitleOrCode(nomenclatureText)
                    else
                        getNomenclatureStocks(list)
                } else {
                    nomenclature = null
                    if (nomenclatureText.isNotEmpty() && nomenclatureText.length >= 3)
                        retrofitViewModel.getGvzoByTitle(nomenclatureText)
                    else
                        getNomenclatureStocks(list)
                }
                prepareChips()
                //getNomenclatureStocks(list)
            }
            retrofitViewModel.getWarehousesListByGuid(getWarehouseFromSettings())
            nomenclatureTextEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    nomenclatureTextInputLayout.error = null
                    nomenclatureTextInputLayout.helperText = null
                    //if (!chipCloseClick) deleteChip(ChipType.NOMENCLATURE_OR_GVZO_CHIP)
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            nomenclatureTextInputLayout.setEndIconOnClickListener {
                if (nomenclatureTextEdit.text.isNullOrBlank()) return@setEndIconOnClickListener
                if (nomenclatureTextEdit.text.length < 3) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.enter_least_3_characters),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setEndIconOnClickListener
                }
                AndroidUtils.hideKeyboard(nomenclatureTextEdit)
                if (nomenclatureTextEdit.inputType != InputType.TYPE_NULL) {
                    if (byGvzo)
                        retrofitViewModel.getGvzoByTitle(nomenclatureTextEdit.text.toString())
                    else
                        retrofitViewModel.getItemByTitleOrCode(nomenclatureTextEdit.text.toString())
                } else {
                    if (byGvzo)
                        clearSearchParamsInForm(isNomenclature = true, clearGvzo = true)
                    else
                        clearSearchParamsInForm(isNomenclature = true)
                    prepareChips()
                    getNomenclatureStocks(binding.list)
                }
            }

            nomenclatureTextEdit.setOnItemClickListener { adapterView, _, position, _ ->
                AndroidUtils.hideKeyboard(nomenclatureTextEdit)
                if (byGvzo) {
                    gvzo = adapterView.getItemAtPosition(position) as Gvzo
                    setNomenclatureSelectedAndGetStocks(gvzo)
//                    nomenclature = null
//                    nomenclatureTextEdit.setText(gvzo?.title ?: "")
//                    nomenclatureTextInputLayout.error = null
//                    nomenclatureTextInputLayout.helperText = "Код: ${gvzo?.code ?: ""}"
//                    val detailHelperText = cellTextInputLayout.helperText
//                    cellTextInputLayout.helperText = "${detailHelperText}\tГВЗО: ${gvzo?.title}"
                    prepareChips()
                } else {
                    nomenclature = adapterView.getItemAtPosition(position) as Nomenclature
                    setNomenclatureSelectedAndGetStocks(nomenclature)
//                    gvzo = null
//                    nomenclatureTextEdit.setText(nomenclature?.itemTitle ?: "")
//                    nomenclatureTextInputLayout.error = null
//                    nomenclatureTextInputLayout.helperText = "Код: ${nomenclature?.code ?: ""}"
                    prepareChips()
                }
                setClearEndIcon(isNomenclature = true)
                getNomenclatureStocks(binding.list)
            }

            cellTextEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    cellTextInputLayout.error = null
                    //cellTextInputLayout.helperText = null
                    //if (!chipCloseClick) deleteChip(ChipType.CELL_CHIP)
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            cellTextInputLayout.setEndIconOnClickListener {
                AndroidUtils.hideKeyboard(cellTextEdit)
                if (cellTextEdit.inputType != InputType.TYPE_NULL) {
                    retrofitViewModel.getCellsList(
                        warehouseGuid = getWarehouseFromSettings(),
                        partNameCode = cellTextEdit.text.toString()
                    )
                } else {
                    clearSearchParamsInForm()
                    prepareChips()
                    getNomenclatureStocks(binding.list)
                }
            }

            cellTextEdit.setOnItemClickListener { adapterView, _, position, _ ->
                AndroidUtils.hideKeyboard(cellTextEdit)
                cell = adapterView.getItemAtPosition(position) as Cell
                cellTextEdit.setText(cell?.title ?: "")
                prepareChips()
                setClearEndIcon()
                cellTextInputLayout.error = null
//                cellTextInputLayout.helperText =
//                    "Текущий склад в настройках: ${getWarehouseFromSettings()}"
                getNomenclatureStocks(binding.list)
            }

            groupTableItems.visibility = View.VISIBLE
            list.adapter = stocksAdapter

            registerForContextMenu(list)

            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) { dataScanPair ->

            val dataScanBarcode = dataScanPair.first
            val dataScanBarcodeType = dataScanPair.second

            if (dataScanBarcode == "") return@observe

            BarcodeScannerReceiver.clearData()

            if (!BarcodeScannerReceiver.isEnabled() || DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.isShowing == true) {
                //SoundPlayer(requireContext(), SoundType.SMALL_ERROR).playSound()
                return@observe
            }

            try {
//                if (dataScanBarcodeType != "Code 128" || dataScanBarcodeType != "QR Code") {
//                    throw Exception(getString(R.string.err_scan_ean13_barcode))
//                } else {
//                    if (dataScanBarcode.length != 13) {
//                        throw Exception(getString(R.string.err_scan_ean13_barcode))
//                    }
//                }
                if (dataScanBarcodeType == "Code 128" || dataScanBarcodeType == "QR Code") {
                    retrofitViewModel.getCellByBarcode(
                        barcode = dataScanBarcode,
                        warehouseGuid = getWarehouseFromSettings()
                    )
                } else {
                    if (!byGvzo)
                        retrofitViewModel.getItemByBarcode(dataScanBarcode)
                    else
                        SoundPlayer(requireContext(), SoundType.SMALL_ERROR).playSound()
                }
            } catch (e: Exception) {
                BarcodeScannerReceiver.setEnabled(false)
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                    message = e.message.toString(),
                    title = getString(R.string.err_barcode_format),
                    positiveButtonTitle = getString(R.string.ok_text),
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            BarcodeScannerReceiver.setEnabled()
                            super.onPositiveClickButton()
                        }
                    }
                ).show()
            }
        }

        retrofitViewModel.listDataWarehouses.observe(viewLifecycleOwner) {
            warehouse = it.firstOrNull()
            if (it.count() > 1) warehouse = null
            prepareChips()
        }

        retrofitViewModel.itemData.observe(viewLifecycleOwner) {
            nomenclature = it
            val oldNomenclatureText = binding.nomenclatureTextEdit.text
            binding.nomenclatureTextEdit.setText(nomenclature?.itemTitle ?: "")
            binding.nomenclatureTextInputLayout.helperText =
                if (nomenclature?.code.isNullOrBlank()) null else "Код: ${nomenclature?.code}"

            if (nomenclature == null) return@observe

            if (nomenclature?.itemGuid?.isBlank() != false) {
                BarcodeScannerReceiver.setEnabled(false)
                SoundPlayer(requireContext(), SoundType.ITEM_NOT_FOUND).playSound()
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                    title = getString(R.string.nomenclature_not_found_text),
                    message = "${getString(R.string.check_label_scan_again_text)} [$oldNomenclatureText]",
                    positiveButtonTitle = getString(R.string.ok_text),
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            BarcodeScannerReceiver.setEnabled()
                            super.onPositiveClickButton()
                        }
                    }
                )
                stocksAdapter.submitList(listOf<NomenclatureStocks>())
                return@observe
            }
            setClearEndIcon(isNomenclature = true)
            getNomenclatureStocks(binding.list)
        }

        retrofitViewModel.cellData.observe(viewLifecycleOwner) {
            cell = it
            binding.cellTextEdit.setText(cell?.title ?: "")
//            binding.cellTextInputLayout.helperText =
//                if (cell?.code.isNullOrBlank()) null else "Код: ${nomenclature?.code}"

            if (cell == null) return@observe

            if (cell?.guid?.isBlank() != false) {
                BarcodeScannerReceiver.setEnabled(false)
                SoundPlayer(requireContext(), SoundType.ITEM_NOT_FOUND).playSound()
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                    title = getString(R.string.cell_not_found_text),
                    message = getString(R.string.check_label_scan_again_text),
                    positiveButtonTitle = getString(R.string.ok_text),
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            BarcodeScannerReceiver.setEnabled()
                            super.onPositiveClickButton()
                        }
                    }
                )
                stocksAdapter.submitList(listOf<NomenclatureStocks>())
                return@observe
            }
            setClearEndIcon()
            getNomenclatureStocks(binding.list)
        }

        retrofitViewModel.dataState.observe(viewLifecycleOwner) {
            if (it.loading) {
                //BarcodeScannerReceiver.setEnabled(false)
                if (DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.isShowing == false || DialogScreen.getDialog(
                        DialogScreen.IDD_PROGRESS
                    ) == null
                )
                    DialogScreen.showDialog(requireContext(), DialogScreen.IDD_PROGRESS)
                return@observe
            } else {
                //BarcodeScannerReceiver.setEnabled()
                DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
            }

            if (it.error) {
                BarcodeScannerReceiver.setEnabled(false)
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    message = it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getItemByBarcode" -> {
                                    retrofitViewModel.getItemByBarcode(it.barcode)
                                }
                                "getNomenclatureStocks" -> {
                                    getNomenclatureStocks(binding.list)
                                }
                                "getCellsList" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "partNameCode")
                                        retrofitViewModel.getCellsList(
                                            warehouseGuid = getWarehouseFromSettings(),
                                            partNameCode = requestParam.propertyValue
                                        )
                                    else
                                        retrofitViewModel.getCellsList(warehouseGuid = getWarehouseFromSettings())
                                }
                                "getCellByBarcode" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "barcode") {
                                        retrofitViewModel.getCellByBarcode(
                                            barcode = requestParam.propertyValue,
                                            warehouseGuid = getWarehouseFromSettings()
                                        )
                                    } else {
                                        retrofitViewModel.getCellByBarcode(
                                            barcode = "",
                                            warehouseGuid = getWarehouseFromSettings()
                                        )
                                    }
                                }
                                "getItemByTitleOrCode" -> {
                                    val requestParam = it.additionalRequestProperties.firstOrNull()
                                    if (requestParam?.propertyName == "partNameCode")
                                        retrofitViewModel.getItemByTitleOrCode(requestParam.propertyValue)
                                    else
                                        retrofitViewModel.getItemByTitleOrCode("")
                                }
                            }
                        }

                        override fun onNegativeClickButton() {
                            BarcodeScannerReceiver.setEnabled()
                            clearSearchParamsInForm(isNomenclature = true)
                            clearSearchParamsInForm()
                            super.onNegativeClickButton()
                        }
                    }
                )
            }
        }

        retrofitViewModel.listNomenclatureStocks.observe(viewLifecycleOwner) { nomenclatureStocksList ->
            //Log.d("STOCKS_INFO", nomenclatureStocksList.toString())
            AndroidUtils.hideKeyboard(binding.nomenclatureTextEdit)
            stocksAdapter.submitList(nomenclatureStocksList)
        }

        retrofitViewModel.listGvzo.observe(viewLifecycleOwner) {
            gvzoAdapter = DynamicListAdapter(
                requireContext(),
                R.layout.counterparty_item_layout,
                it?.toList() ?: listOf()
            )
            (binding.nomenclatureTextEdit as? AutoCompleteTextView)?.setAdapter(gvzoAdapter)
            if (it?.isEmpty() != false) {
                //stocksAdapter.submitList(listOf<NomenclatureStocks>())
                if (byGvzo) gvzo = null else nomenclature = null
                if (it != null) {
                    setClearEndIcon(isNomenclature = true)
                    BarcodeScannerReceiver.setEnabled(false)
                    DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                        title = getString(R.string.search_text),
                        message = getString(R.string.gvzo_not_found),
                        positiveButtonTitle = getString(R.string.ok_text),
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                clearSearchParamsInForm(
                                    isNomenclature = true,
                                    clearTextField = false
                                )
                                BarcodeScannerReceiver.setEnabled()
                                super.onPositiveClickButton()
                            }
                        }
                    )
                }
                getNomenclatureStocks(binding.list)
            } else {
                if (it.count() == 1)
                    setNomenclatureSelectedAndGetStocks(it.first())
                else
                    binding.nomenclatureTextEdit.showDropDown()
            }
        }

        retrofitViewModel.itemListData.observe(viewLifecycleOwner) {
            nomenclatureAdapter = DynamicListAdapter<Nomenclature>(
                requireContext(),
                R.layout.counterparty_item_layout,
                it?.toList() ?: listOf()
            )
            (binding.nomenclatureTextEdit as? AutoCompleteTextView)?.setAdapter(nomenclatureAdapter)
            if (it?.isEmpty() != false) {
                //stocksAdapter.submitList(listOf<NomenclatureStocks>())
                val oldNomenclatureText = binding.nomenclatureTextEdit.text
                if (byGvzo) gvzo = null else nomenclature = null
                if (it != null) {
                    setClearEndIcon(isNomenclature = true)
                    BarcodeScannerReceiver.setEnabled(false)
                    DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                        title = getString(R.string.search_text),
                        message = "${getString(R.string.nomenclature_not_found_text)} [$oldNomenclatureText]",
                        positiveButtonTitle = getString(R.string.ok_text),
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                clearSearchParamsInForm(isNomenclature = true)
                                BarcodeScannerReceiver.setEnabled()
                                super.onPositiveClickButton()
                            }
                        }
                    )
                }
                getNomenclatureStocks(binding.list)
            } else {
                if (it.count() == 1)
                    setNomenclatureSelectedAndGetStocks(it.first())
                else
                    binding.nomenclatureTextEdit.showDropDown()
            }
        }

        retrofitViewModel.cellListData.observe(viewLifecycleOwner) {
            cellAdapter = DynamicListAdapter<Cell>(
                requireContext(),
                R.layout.counterparty_item_layout,
                it?.toList() ?: listOf()
            )
            (binding.cellTextEdit as? AutoCompleteTextView)?.setAdapter(cellAdapter)
            if (it?.isEmpty() != false) {
                //stocksAdapter.submitList(listOf<NomenclatureStocks>())
                cell = null
                if (it != null) {
                    setClearEndIcon()
                    BarcodeScannerReceiver.setEnabled(false)
                    DialogScreen.showDialog(
                        requireContext(),
                        DialogScreen.IDD_ERROR_SINGLE_BUTTON,
                        title = getString(R.string.search_text),
                        message = getString(R.string.cell_not_found_text),
                        positiveButtonTitle = getString(R.string.ok_text),
                        onDialogsInteractionListener = object : OnDialogsInteractionListener {
                            override fun onPositiveClickButton() {
                                clearSearchParamsInForm()
                                BarcodeScannerReceiver.setEnabled()
                                super.onPositiveClickButton()
                            }
                        }
                    )
                }
                getNomenclatureStocks(binding.list)
            } else {
                try {
                    binding.cellTextEdit.showDropDown()
                } catch (e: Exception) {
                    Log.d("TN_ERROR", "${e.message.toString()}: ${e.printStackTrace()}")
                }

            }
        }

        return binding.root
    }

    private fun setChipsGroupVisibility() {
        val searchGroupButton = requireView().findViewById<MaterialButton>(R.id.searchGroupButton)
        val searchGroup = requireView().findViewById<Group>(R.id.searchGroup)
        //val chipsDetailGroup = requireView().findViewById<ChipGroup>(R.id.chipsDetailGroup)
        if (searchGroupButton.isChecked) {
            searchGroup.visibility = View.GONE
            //chipsDetailGroup.visibility = View.VISIBLE
        } else {
            searchGroup.visibility = View.VISIBLE
            //chipsDetailGroup.visibility = View.GONE
        }

        setDetailChipsVisibility(searchGroupButton.isChecked)

    }

    private fun setDetailChipsVisibility(visibility: Boolean) {
        val chipGroup = requireView().findViewById<ChipGroup>(R.id.chipGroup)
        val chipsCount = chipGroup.childCount
        val chips: MutableList<View> = mutableListOf()
        val visibilityState = if (visibility) View.VISIBLE else View.GONE
        for (i in 0..chipsCount) {
            val chip = chipGroup.getChildAt(i)
            if (chip != null && chip.id != R.id.warehouseChip) {
                chip.visibility = visibilityState
            }
        }
    }

    private fun setGvzoChipText() {
        //val gvzoChip = requireView().findViewById<Chip>(R.id.gvzoChip)
//        val text = getString(R.string.by_gvzo_text)
//        gvzoChip.text = if (gvzoChip.isChecked) "$text: ${getString(R.string.text_yes)}" else "$text: ${getString(R.string.text_no)}"
        //val text = if (gvzoChip.isChecked) getString(R.string.by_gvzo_text) else getString(R.string.by_nomenclature_text)
        //val text = getString(R.string.by_gvzo_text)
        //gvzoChip.text = text
    }

    private fun <T> setNomenclatureSelectedAndGetStocks(item: T) {
        val nomenclatureTextEdit =
            requireView().findViewById<AutoCompleteTextView>(R.id.nomenclatureTextEdit)
        val nomenclatureTextInputLayout =
            requireView().findViewById<TextInputLayout>(R.id.nomenclatureTextInputLayout)
        val recyclerView =
            requireView().findViewById<RecyclerView>(R.id.list)
        if (item is Gvzo) {
            gvzo = item
            nomenclature = null
            nomenclatureTextEdit.setText(gvzo?.title ?: "")
            nomenclatureTextInputLayout.error = null
            nomenclatureTextInputLayout.helperText = "Код: ${item?.code ?: ""}"
        }
        if (item is Nomenclature) {
            nomenclature = item
            gvzo = null
            nomenclatureTextEdit.setText(nomenclature?.itemTitle ?: "")
            nomenclatureTextInputLayout.error = null
            nomenclatureTextInputLayout.helperText = "Код: ${nomenclature?.code ?: ""}"
        }
        setClearEndIcon(isNomenclature = true)
        prepareChips()
        getNomenclatureStocks(recyclerView)
    }

    private fun prepareChips() {
        val chipWarehouse = requireView().findViewById<Chip>(R.id.warehouseChip)
        chipWarehouse.text = warehouse?.warehouseTitle ?: getString(R.string.failed_get_warehouse)

        if (warehouse == null)
            chipWarehouse.setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red_900
                    )
                )
            )
        else
            chipWarehouse.setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.detailHelperTextColor
                    )
                )
            )

        if (byGvzo) {
            if (gvzo != null)
                addNewChip(ChipType.GVZO_CHIP, gvzo!!.title)
            else
                deleteChip(ChipType.GVZO_CHIP)
//            searchStatusText.text =
//                "${searchStatusText.text} \tГВЗО: ${gvzo?.title ?: "[Не указана]"} "
            if (gvzo != null)
                if (nomenclature != null) addNewChip(
                    ChipType.NOMENCLATURE_CHIP,
                    nomenclature!!.itemTitle
                )
                else
                    deleteChip(ChipType.NOMENCLATURE_CHIP)
//                nomenclatureTextInputLayout.helperText =
//                    "${getString(R.string.code_text)}: ${gvzo?.code ?: "[Не определен]"}"
        } else {
            if (nomenclature != null)
                addNewChip(ChipType.NOMENCLATURE_CHIP, nomenclature!!.itemTitle)
            else
                deleteChip(ChipType.NOMENCLATURE_CHIP)

            if (!byGvzo) deleteChip(ChipType.GVZO_CHIP)
//                nomenclatureTextInputLayout.helperText =
//                    "${getString(R.string.code_text)}: ${nomenclature?.code ?: "[Не определен]"}"
        }

        if (cell != null)
            addNewChip(ChipType.CELL_CHIP, cell!!.title)
        else
            deleteChip(ChipType.CELL_CHIP)
    }

    private fun addNewChip(type: ChipType, text: String): Chip {
        deleteChip(type)
        chipCloseClick = false
        val newChip = Chip(requireContext())
        newChip.id = ViewCompat.generateViewId()
        //newChip.chipMinHeight= 0f
        newChip.setEnsureMinTouchTargetSize(false)
        newChip.chipStrokeColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red_400))
        newChip.closeIconTint =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red_900))
        newChip.tag = type.tag
        newChip.text = text
        newChip.isCloseIconVisible = true
        newChip.isChecked = true
        newChip.setOnCloseIconClickListener {
            chipCloseClick = true
            deleteChip(type)
        }

        val chipGroup = requireView().findViewById<ChipGroup>(R.id.chipGroup)
        chipGroup.addView(newChip)

        setChipsGroupVisibility()

        return newChip
    }

    private fun deleteChip(type: ChipType) {
        val chipGroup = requireView().findViewById<ChipGroup>(R.id.chipGroup)
        val chipsCount = chipGroup.childCount
        val chips: MutableList<View> = mutableListOf()
        for (i in 0..chipsCount) {
            val chip = chipGroup.getChildAt(i)
            if (chip != null) chips.add(chip)
        }

//        val chips = chipGroup.checkedChipIds
        chips.forEach { chip ->
            if (chip.tag == type.tag) {
                when (type) {
                    ChipType.GVZO_FILTER_CHIP -> {
                        requireView().findViewById<MaterialSwitch>(R.id.gvzoSwitcher).isChecked =
                            false
                    }
                    ChipType.GVZO_CHIP -> {
                        if (chipCloseClick) {
                            clearSearchParamsInForm(
                                isNomenclature = true,
                                clearGvzo = true,
                                clearTextField = true
                            )
                            chipCloseClick = false
                        }
                        val stocksList = requireView().findViewById<RecyclerView>(R.id.list)
                        getNomenclatureStocks(stocksList)
                    }
                    ChipType.NOMENCLATURE_CHIP -> {
                        if (chipCloseClick) {
                            clearSearchParamsInForm(
                                isNomenclature = true,
                                clearGvzo = false,
                                clearTextField = true
                            )
                            chipCloseClick = false
                        }
                        val stocksList = requireView().findViewById<RecyclerView>(R.id.list)
                        getNomenclatureStocks(stocksList)
                    }
                    ChipType.CELL_CHIP -> {
                        if (chipCloseClick) {
                            clearSearchParamsInForm()
                            chipCloseClick = false
                        }
                        //refreshSearchParamsByCellChange()
                        val stocksList = requireView().findViewById<RecyclerView>(R.id.list)
                        getNomenclatureStocks(stocksList)
                    }
                }
                chipGroup.removeView(chip)
            }
        }
    }

    private fun clearSearchParamsInForm(
        isNomenclature: Boolean = false,
        clearTextField: Boolean = true,
        clearGvzo: Boolean = true,
    ) {
        val textEdit: AutoCompleteTextView
        val textInputLayout: TextInputLayout

        if (isNomenclature) {
            if (clearGvzo) gvzo = null
            if (!byGvzo) nomenclature = null
            textEdit = requireView().findViewById(R.id.nomenclatureTextEdit)
            textInputLayout = requireView().findViewById(R.id.nomenclatureTextInputLayout)
        } else {
            cell = null
            textEdit = requireView().findViewById(R.id.cellTextEdit)
            textInputLayout = requireView().findViewById(R.id.cellTextInputLayout)
        }
        if (clearTextField) textEdit.text = null
        if (isNomenclature) textInputLayout.helperText = null
        textEdit.inputType = InputType.TYPE_CLASS_TEXT
        textInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        textInputLayout.endIconDrawable =
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_search_24
            )
        textInputLayout.endIconContentDescription =
            getString(R.string.find_text)
    }

    private fun setClearEndIcon(isNomenclature: Boolean = false) {
        var textEdit: AutoCompleteTextView
        var textInputLayout: TextInputLayout

        if (isNomenclature) {
            textEdit = requireView().findViewById(R.id.nomenclatureTextEdit)
            textInputLayout = requireView().findViewById(R.id.nomenclatureTextInputLayout)
        } else {
            textEdit = requireView().findViewById(R.id.cellTextEdit)
            textInputLayout = requireView().findViewById(R.id.cellTextInputLayout)
        }
        textInputLayout.endIconDrawable = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.ic_baseline_clear_24
        )
        textInputLayout.endIconContentDescription =
            getString(R.string.clear_text)

        textEdit.inputType = InputType.TYPE_NULL
    }

    private fun getNomenclatureStocks(recyclerView: RecyclerView) {
        if (byGvzo) {
            if (gvzo?.guid.isNullOrBlank() && cell?.guid.isNullOrBlank()) {
                stocksAdapter.submitList(listOf<NomenclatureStocks>())
                return
            }
        } else {
            if (nomenclature?.itemGuid.isNullOrBlank() && cell?.guid.isNullOrBlank()) {
                stocksAdapter.submitList(listOf<NomenclatureStocks>())
                return
            }
        }
        val byCell = getSearchGroupingState()
        headerTitle?.text =
            if (byCell) getString(R.string.cell_text) else getString(R.string.header_nomenclature)
        headerCell?.text =
            if (byCell) getString(R.string.header_nomenclature) else getString(R.string.cell_text)
        stocksAdapter = NomenclatureStocksAdapter(
            groupByCell = byCell,
            onStocksItemInteractionListener = getItemClickListener()
        )
        recyclerView.adapter = stocksAdapter
        retrofitViewModel.getNomenclatureStocks(
            warehouseGuid = getWarehouseFromSettings(),
            nomenclatureGuid = if (byGvzo) "" else nomenclature?.itemGuid ?: "",
            cellGuid = cell?.guid ?: "",
            byCell = byCell,
            gvzoGuid = if (byGvzo) gvzo?.guid ?: "" else "",
        )
    }

    private fun getWarehouseFromSettings(): String =
        settingsViewModel.getPreferenceByKey("warehouse_guid", "") ?: ""

    override fun onDestroyView() {
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.dismiss()
        DialogScreen.getDialog()?.dismiss()
        super.onDestroyView()
    }
}