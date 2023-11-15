package ru.tn.shinglass.activity

import android.content.Context.PRINT_SERVICE
import android.graphics.*
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Bundle
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tscdll.TscWifiActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.runBlocking
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.AndroidUtils
import ru.tn.shinglass.activity.utilites.ScreenMetricsCompat
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.adapters.DynamicListAdapter
import ru.tn.shinglass.adapters.MyPrintDocumentAdapter
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.databinding.FragmentBarcodePrintBinding
import ru.tn.shinglass.models.Barcode
import ru.tn.shinglass.models.Cell
import ru.tn.shinglass.models.Nomenclature
import ru.tn.shinglass.models.PrintLabelProperties
import ru.tn.shinglass.viewmodel.RetrofitViewModel
import ru.tn.shinglass.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class BarcodePrintFragment : Fragment() {

    private val retrofitViewModel: RetrofitViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val args: BarcodePrintFragmentArgs by navArgs()

    private var barCodeType: BarcodeFormat = BarcodeFormat.QR_CODE

    private var warehouseGuid: String = ""
    private var barcodeLabelText: String = ""
    private var nomenclatureBarcodeList: List<Barcode> = emptyList()
    private var nomenclatureBarcodeIndex: Int = -1

    private val inchToMm = 0.03937007874f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        AppAuth.getInstance().authStateFlow.observe(viewLifecycleOwner) { authState ->
            val authUser1C = authState.user1C
            if (authUser1C.getUserGUID().isEmpty()) findNavController().navigate(R.id.authFragment)
        }

        val nomenclatureArg = args.nomenclatureArg
        val cellArg = args.cellArg

        val binding = FragmentBarcodePrintBinding.inflate(inflater, container, false)
        with(binding) {

            setTextInputElementProperties(
                element = cellTextView,
                elementLayout = cellTextInputLayout
            )

            setTextInputElementProperties(
                element = nomenclatureTextEdit,
                elementLayout = nomenclatureTextInputLayout
            )

            radioQRCode.setOnClickListener {
                barCodeType = BarcodeFormat.QR_CODE
                showBarcode()
            }
            radioEAN13.setOnClickListener {
                barCodeType = BarcodeFormat.EAN_13
                showBarcode()
            }

            radioCode128.setOnClickListener {
                barCodeType = BarcodeFormat.CODE_128
                showBarcode()
            }
            buttonPrint.setOnClickListener {
                createPdf(cellTextView.text.toString())
            }

            printerSettingsButton.setOnClickListener {
                findNavController().navigate(R.id.action_global_printerSettingsFragment)
            }

            textUOM.setOnClickListener() {
                showBarcodesList(userClick = true)
            }

            toggleButton.addOnButtonCheckedListener() { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.cellButton -> {
                            nomenclatureFields.visibility = View.GONE
                            cellTextInputLayout.visibility = View.VISIBLE
                            clearInputField(
                                nomenclatureTextEdit,
                                nomenclatureTextInputLayout,
                                false
                            )
                            showBarcode()
                        }
                        R.id.nomenclatureButton -> {
                            nomenclatureFields.visibility = View.VISIBLE
                            cellTextInputLayout.visibility = View.GONE
                            clearInputField(cellTextView, cellTextInputLayout, false)
                            showBarcode()
                        }
                    }
                }
            }

            if (nomenclatureArg != null) {
                (nomenclatureButton as MaterialButton).isChecked = true
                nomenclatureTextEdit.setText(nomenclatureArg.itemTitle)
                retrofitViewModel.getBarcodesByItem(nomenclatureArg.itemGuid)
            }

            if (cellArg != null) {
                (cellButton as MaterialButton).isChecked = true
                cellTextView.setText(cellArg.title)
            }

        }

        retrofitViewModel.barcodesData.observe(viewLifecycleOwner) {
            nomenclatureBarcodeList = it
            //if (it.isEmpty()) return@observe
            //Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_LONG).show()
//            val adapter = DynamicListAdapter<Barcode>(requireContext(), R.layout.dynamic_prefs_layout_choice, it)
//            DialogScreen.showDialog(requireContext(), DialogScreen.IDD_INPUT, message = "Выберите ЕИ",
//            isCancelable = false,
//            //customView = DynamicPrefsLayoutBinding.inflate(LayoutInflater.from(requireContext())).root,
//            singleChoiceAdapter = adapter)
            showBarcodesList()

        }

        retrofitViewModel.cellListData.observe(viewLifecycleOwner) {
            if (it.isEmpty() || barcodeLabelText.isNotEmpty()) return@observe

            val cellArrayList: ArrayList<Cell> = arrayListOf()
            cellArrayList.clear()
            cellArrayList.add(Cell(getString(R.string.not_chosen_text), "", ""))

            it.forEach { cell ->
                cellArrayList.add(cell)
            }

            val cellAdapter = DynamicListAdapter<Cell>(
                requireContext(),
                R.layout.dynamic_prefs_layout,
                cellArrayList
            )

            (binding.cellTextView as? AutoCompleteTextView)?.setAdapter(cellAdapter)
            try {
                binding.cellTextView.showDropDown()
            } catch (e: Exception) {
            }
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
                barcodeLabelText = ""
                DialogScreen.showDialog(
                    requireContext(),
                    DialogScreen.IDD_ERROR,
                    it.errorMessage,
                    onDialogsInteractionListener = object : OnDialogsInteractionListener {
                        override fun onPositiveClickButton() {
                            when (it.requestName) {
                                "getCellsList" -> {
                                    DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.show()
                                    var partNameCode = ""
                                    var warehouseGuid = ""
                                    for (requestParam in it.additionalRequestProperties) {
                                        if (requestParam.propertyName == "partNameCode")
                                            partNameCode = requestParam.propertyValue
                                        if (requestParam.propertyName == "warehouseGuid")
                                            warehouseGuid = requestParam.propertyValue
                                    }
                                    retrofitViewModel.getCellsList(
                                        partNameCode = partNameCode,
                                        warehouseGuid = warehouseGuid
                                    )
                                }
                                "getBarcodesByItem" -> {
                                    DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.show()
                                    var itemGuid = ""
                                    for (requestParam in it.additionalRequestProperties) {
                                        if (requestParam.propertyName == "itemGuid")
                                            itemGuid = requestParam.propertyValue
                                    }
                                    retrofitViewModel.getBarcodesByItem(
                                        itemGuid = itemGuid
                                    )
                                }
                            }
                        }
                    })
            }
        }

        retrofitViewModel.itemListData.observe(viewLifecycleOwner) {
//            if (it?.isEmpty() == true || it == null) return@observe
//            val nomenclatureAdapter = DynamicListAdapter<Nomenclature>(
//                requireContext(),
//                R.layout.counterparty_item_layout,
//                it?.toList() ?: listOf()
//            )
//            (binding.nomenclatureTextEdit as? AutoCompleteTextView)?.setAdapter(nomenclatureAdapter)
            //           if (it?.isEmpty() != false) {
            //stocksAdapter.submitList(listOf<NomenclatureStocks>())
            if (it != null) {
                val nomenclatureArrayList: ArrayList<Nomenclature> = arrayListOf()
                nomenclatureArrayList.clear()
                nomenclatureArrayList.add(
                    Nomenclature(
                        itemTitle = getString(R.string.not_chosen_text),
                        itemGuid = "",
                        code = "",
                        unitOfMeasurementGuid = "",
                        unitOfMeasurementTitle = "",
                        coefficient = 0.0,
                        qualityGuid = "",
                        qualityTitle = "",
                    )
                )

                it.forEach { nomenclature ->
                    nomenclatureArrayList.add(nomenclature)
                }

                val nomenclatureAdapter = DynamicListAdapter<Nomenclature>(
                    requireContext(),
                    R.layout.dynamic_prefs_layout,
                    nomenclatureArrayList
                )

                (binding.nomenclatureTextEdit as? AutoCompleteTextView)?.setAdapter(
                    nomenclatureAdapter
                )
                try {
                    binding.nomenclatureTextEdit.showDropDown()
                } catch (e: Exception) {
                }
            }
//            }
        }

        return binding.root
    }

    private fun showBarcodesList(userClick: Boolean = false) {
        nomenclatureBarcodeIndex = -1
        if (nomenclatureBarcodeList.isEmpty()) return
        val textUOM = requireActivity().findViewById<TextView>(R.id.textUOM)
        if (nomenclatureBarcodeList.count() == 1) {
            nomenclatureBarcodeIndex = 0
            textUOM.text = nomenclatureBarcodeList[nomenclatureBarcodeIndex].unitOfMeasurementTitle
            if (!userClick) return
        }
        val adapter: ListAdapter = ArrayAdapter<Barcode>(
            requireContext(), android.R.layout.select_dialog_singlechoice, nomenclatureBarcodeList
        )

        DialogScreen.showDialog(
            requireContext(), DialogScreen.IDD_INPUT, message = "Выберите ЕИ",
            isCancelable = false,
            //customView = DynamicPrefsLayoutBinding.inflate(LayoutInflater.from(requireContext())).root,
            singleChoiceAdapter = adapter,
            onDialogsInteractionListener = object : OnDialogsInteractionListener {
                override fun onClick(index: Int) {
//                    Toast.makeText(
//                        requireContext(),
//                        "${index.toString()}: ${nomenclatureBarcodeList[index].toString()}",
//                        Toast.LENGTH_LONG
//                    ).show()
                    nomenclatureBarcodeIndex = index
                    textUOM.text = nomenclatureBarcodeList[index].toString()
                    if (args.nomenclatureArg != null) {
                        barCodeType = BarcodeFormat.QR_CODE
                    }
                    showBarcode()
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        //val binding = FragmentBarcodePrintBinding.inflate(LayoutInflater.from(requireContext()))
//        val activity = requireActivity()
//        val cellTextView = activity.findViewById<AutoCompleteTextView>(R.id.cellTextView)
//        val cellTextInputLayout = activity.findViewById<TextInputLayout>(R.id.cellTextInputLayout)
//        setTextInputElementProperties(
//            element = cellTextView,
//            elementLayout = cellTextInputLayout
//        )
//        val nomenclatureTextView =
//            activity.findViewById<AutoCompleteTextView>(R.id.nomenclatureTextEdit)
//        val nomenclatureTextInputLayout =
//            activity.findViewById<TextInputLayout>(R.id.nomenclatureTextInputLayout)
//        setTextInputElementProperties(
//            element = nomenclatureTextView,
//            elementLayout = nomenclatureTextInputLayout
//        )

    }

    private fun setTextInputElementProperties(
        element: AutoCompleteTextView,
        elementLayout: TextInputLayout,
        receivedCell: Boolean = false
    ) {
        val printButton = requireActivity().findViewById<MaterialButton>(R.id.buttonPrint)
        if (!element.text.isNullOrBlank()) {
            printButton?.isEnabled = true
            element.inputType = android.text.InputType.TYPE_NULL
            elementLayout.endIconDrawable =
                androidx.appcompat.content.res.AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_clear_24
                )
            elementLayout.endIconContentDescription =
                getString(R.string.clear_text)
            elementLayout.error = null
            ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(
                FragmentBarcodePrintBinding.inflate(
                    LayoutInflater.from(requireContext())
                ).root
            )
        } else {
            printButton?.isEnabled = false
        }

        element.setOnClickListener {
            if (elementLayout.error == null) return@setOnClickListener
            elementLayout.error = null
            barcodeLabelText = ""
            element.text = null
            element.inputType = android.text.InputType.TYPE_CLASS_TEXT
            elementLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            elementLayout.endIconDrawable =
                androidx.appcompat.content.res.AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_search_24
                )
            elementLayout.endIconContentDescription =
                getString(R.string.find_text)
        }

        elementLayout.setEndIconOnClickListener {
            clearInputField(element, elementLayout)
        }

        element.setOnItemClickListener { adapterView, _, position, _ ->
            if (element.id == R.id.cellTextView) {
                val cell = adapterView.getItemAtPosition(position) as Cell
                element.setText(cell.title)
                barcodeLabelText = cell.barcodeLabel
                printButton?.isEnabled = cell.guid.isNotBlank()
            } else {
                val nomenclature = adapterView.getItemAtPosition(position) as Nomenclature
                element.setText(nomenclature.itemTitle)
                getNomenclatureUOM(nomenclature.itemGuid)
                //barcodeLabelText = nomenclature.
                //printButton?.isEnabled = nomenclature.itemGuid.isNotBlank()
            }
            element.inputType = android.text.InputType.TYPE_NULL
            elementLayout.endIconDrawable =
                androidx.appcompat.content.res.AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_clear_24
                )
            elementLayout.endIconContentDescription =
                getString(R.string.clear_text)
            elementLayout.error = null
            showBarcode()
            val binding = FragmentBarcodePrintBinding.inflate(LayoutInflater.from(requireContext()))
            ru.tn.shinglass.activity.utilites.AndroidUtils.hideKeyboard(binding.root)
        }
    }

    private fun clearInputField(
        element: AutoCompleteTextView,
        elementLayout: TextInputLayout,
        showSpinner: Boolean = true
    ) {
        AndroidUtils.hideKeyboard(element)
        if (element.inputType != android.text.InputType.TYPE_NULL) {
            if (elementLayout.id == R.id.cellTextInputLayout) {
                warehouseGuid = settingsViewModel.getPreferenceByKey("warehouse_guid", "") ?: ""
                barcodeLabelText = ""
                showBarcode()
                if (showSpinner) {
                    retrofitViewModel.getCellsList(
                        warehouseGuid = warehouseGuid,
                        partNameCode = element.text.toString()
                    )
                }
            } else {
                if (element.text.isNullOrBlank()) return
                if (element.text.length < 3) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.enter_least_3_characters),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                AndroidUtils.hideKeyboard(element)
                if (showSpinner) {
                    retrofitViewModel.getItemByTitleOrCode(element.text.toString())
                }
            }
        } else {
            barcodeLabelText = ""
            nomenclatureBarcodeList = emptyList()
            nomenclatureBarcodeIndex = -1
            val textUOM = requireActivity().findViewById<TextView>(R.id.textUOM)
            textUOM.text = getString(R.string.undefined_value)
            showBarcode()
            element.text = null
            elementLayout.helperText = null
            element.inputType = android.text.InputType.TYPE_CLASS_TEXT
            elementLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            elementLayout.endIconDrawable =
                androidx.appcompat.content.res.AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_search_24
                )
            elementLayout.endIconContentDescription =
                getString(R.string.find_text)
            val printButton = requireActivity().findViewById<Button>(R.id.buttonPrint)
            printButton?.isEnabled = false
        }
    }

    private fun getNomenclatureUOM(itemGuid: String) {
        retrofitViewModel.getBarcodesByItem(itemGuid)
    }

    private fun showBarcode() {

        val activity = requireActivity()
        var pdfLayout = activity.findViewById<ConstraintLayout>(R.id.pdfLayout)
//        pdfLayout.post(Runnable{
//            runBlocking {  }
//        })
        val barCode = activity.findViewById<ImageView>(R.id.barCode)
        val cellTextView = activity.findViewById<AutoCompleteTextView>(R.id.cellTextView)
        val barcodeLabel = activity.findViewById<TextView>(R.id.barcodeLabel)
        val nomenclatureFields = activity.findViewById<LinearLayout>(R.id.nomenclatureFields)

        var height = ScreenMetricsCompat.getScreenSize(requireContext()).height
        var width = ScreenMetricsCompat.getScreenSize(requireContext()).width
        if (barCodeType == BarcodeFormat.QR_CODE) {
            barcodeLabel.text = ""
            barcodeLabel.visibility = View.GONE
            val lp = barCode.layoutParams
            lp.height = pdfLayout.height
            lp.width = pdfLayout.width
            barCode.layoutParams = lp
            width = barCode.width
            height = barCode.height
            barCode.setBackgroundResource(R.color.white)
//            if (barcodeLabelText.isBlank()) {
//                barCode.setBackgroundResource(R.color.white)
//            } else {
//                barCode.setBackgroundResource(R.drawable.img_border)
//            }
        } else {
            width -= 2
            height = 150
            //barcodeLabel.text = barcodeLabelText
            barcodeLabel.visibility = View.VISIBLE
            barCode.setBackgroundResource(R.color.white)
        }
//        if (cellTextView.visibility != View.VISIBLE) {
//            barcodeLabelText = cellTextView.text.toString()
//        }

        barcodeLabel.text =
            if (nomenclatureFields.visibility == View.GONE) {
                cellTextView.text.toString()
            } else {
                if (nomenclatureBarcodeIndex == -1) "" else nomenclatureBarcodeList[nomenclatureBarcodeIndex].barcode
            }

        barcodeLabelText = if (nomenclatureFields.visibility == View.GONE) {
            cellTextView.text.toString()
        } else {
            barcodeLabel.text.toString()
        }
        val bitmapBarCode = encodeAsBitmap(
            barcodeLabelText,
            barCodeType,
            img_width = width,
            img_height = height
        )
        barCode.setImageBitmap(bitmapBarCode)

//        if (barCodeType == BarcodeFormat.QR_CODE) {
//            lp.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
//            lp.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
//        } else {
//        lp.width = width
//        lp.height = height
////        }
//        barCode.layoutParams = lp

    }

    private fun createPdf(barcodeCaption: String) {

        val useAndroidService =
            !(settingsViewModel.getPreferenceByKey("useManualSettings", false) ?: false)
        if (useAndroidService) {
            val printManager = requireActivity().getSystemService(PRINT_SERVICE) as PrintManager?
            printManager!!.print(
                "barcode_print_job", MyPrintDocumentAdapter(
                    requireContext(),
                    requireActivity().findViewById(R.id.pdfLayout)
                ), null
            )
            return
        }

        //val filePath = generatePdfFile(740, 520)
        val width: Int =
            settingsViewModel.getPreferenceByKey("paperWidth", PrintLabelProperties.MIN_WIDTH.value)
                ?: PrintLabelProperties.MIN_WIDTH.value
        val height: Int = settingsViewModel.getPreferenceByKey(
            "paperHeight",
            PrintLabelProperties.MIN_HEIGHT.value
        ) ?: PrintLabelProperties.MIN_HEIGHT.value
        val printerIP = settingsViewModel.getPreferenceByKey("printerIP", "") ?: ""
        val printerModel =
            settingsViewModel.getPreferenceByKey("printerModel", "Another") ?: "Another"

        var widthToPx = (width * inchToMm * 72).toInt()
        var heightToPx = (height * inchToMm * 72).toInt()
        val file = generatePdfFile(width, height)

        if (printerModel != "TSC") {
            DialogScreen.vibrate(requireContext())
            Toast.makeText(
                requireContext(),
                "${getString(R.string.type_of_printer_is_not_supported_yet)}: [$printerModel]",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        //return
        DialogScreen.showDialog(
            requireContext(),
            DialogScreen.IDD_PROGRESS,
            title = getString(R.string.printing_in_process)
        )

        val ethernetTSC = TscWifiActivity()
        //ethernetTSC.openport("10.93.62.17", 9100)
        ethernetTSC.openport(printerIP, 9100)
        ethernetTSC.clearbuffer()
        ethernetTSC.sendcommand("SIZE $width mm, $height mm\r\n")
        if (file == null) {
            when (barCodeType) {
                BarcodeFormat.CODE_128 -> {
                    val centerX = widthToPx + (widthToPx / 3) + 7
                    val centerY = heightToPx - (heightToPx / 4)
                    ethernetTSC.sendcommand("DIRECTION 1\r\n")
                    ethernetTSC.sendcommand("CLS\r\n")
                    //ethernetTSC.sendcommand("BARCODE $widthToPx, $heightToPx, \"128\",$heightToPx,2,0,2,6,2, \"$barcodeCaption\"\r\n")
                    ethernetTSC.sendcommand("BARCODE $centerX, $centerY, \"128\",$heightToPx,0,0,2,3,2, \"$barcodeLabelText\"\r\n")
                    ethernetTSC.sendcommand("TEXT $centerX,${centerY + heightToPx + 5}, \"2\",0,1,1,2, \"$barcodeCaption\"\r\n")

                    ethernetTSC.sendcommand("PRINT 1\r\n")
                    //Печать теста Ethernet (информация о сети, IP)
                    //ethernetTSC.sendcommand("SELFTEST ETHERNET\r\n")
                }
//                BarcodeFormat.QR_CODE -> {
//                    val centerX = heightToPx - (heightToPx / 2)//widthToPx + (widthToPx / 3) + 7
//                    val centerY = centerX
//                    ethernetTSC.sendcommand("DIRECTION 1\r\n")
//                    ethernetTSC.sendcommand("CLS\r\n")
//                    //ethernetTSC.sendcommand("BARCODE $widthToPx, $heightToPx, \"128\",$heightToPx,2,0,2,6,2, \"$barcodeCaption\"\r\n")
//                    ethernetTSC.sendcommand("QRRCODE $centerX, $centerY, H,10,A,0,\"$barcodeLabelText\"\r\n")
//                    ethernetTSC.sendcommand("TEXT $centerX,${centerY + heightToPx + 5}, \"2\",0,1,1,2, \"$barcodeCaption\"\r\n")
//
//                    ethernetTSC.sendcommand("PRINT 1,1\r\n")
//                }
            }
        } else {
            ethernetTSC.printPDFbyFile(file, 0, 0, 203)
        }
        //ethernetTSC.sendcommand("BITMAP 10, 10,$width,$height,0,$barcodeCaption");
        //ethernetTSC.pr
        //ethernetTSC.printlabel(1, 1)
        val status = ethernetTSC.printerstatus()
        ethernetTSC.closeport(10)
        val printButton =
            FragmentBarcodePrintBinding.inflate(LayoutInflater.from(requireContext())).buttonPrint
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        printButton.isEnabled = true
        Toast.makeText(requireContext(), status, Toast.LENGTH_LONG).show()
    }

    private fun generatePdfFile(width: Int, height: Int): File? {

//        var widthToPx = (width*3.794).toInt()
//        var heightToPx = (height*3.794).toInt()
        var widthToPx = (width * inchToMm * 72).toInt()
        var heightToPx = (height * inchToMm * 72).toInt()
//        var widthToPx = (width).toInt()
//        var heightToPx = (height).toInt()

        // создаем документ
        val document = PdfDocument()

        // определяем размер страницы
        val pageInfo = PageInfo.Builder(widthToPx, heightToPx, 1).create()

        // получаем страницу, на котором будем генерировать контент
        val page = document.startPage(pageInfo)

        // получаем холст (Canvas) страницы
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawPaint(paint)
        val pdfLayout = requireActivity().findViewById<ConstraintLayout>(R.id.pdfLayout)
        // получаем контент, который нужно добавить в PDF, и загружаем его в Bitmap
        //var bitmap = loadBitmapFromView(pdfLayout, pdfLayout.getWidth(), pdfLayout.getHeight())
//        var pdfLayoutWidth = pdfLayout.width
//        var pdfLayoutHeight = pdfLayout.height
//        if (barCodeType == BarcodeFormat.QR_CODE) {
//            pdfLayoutHeight = pdfLayoutWidth
//        }
        var barcodeX = 0f//(canvas.width / 4).toFloat()
        var barcodeY = 0f//(canvas.height).toFloat()
        if (barCodeType == BarcodeFormat.QR_CODE) {
            widthToPx = heightToPx
        } else {
            heightToPx = (heightToPx / 2.5).toInt()
        }
        //var bitmap = loadBitmapFromView(pdfLayout, pdfLayoutWidth, pdfLayoutHeight)

//        var bitmap = loadBitmapFromView(pdfLayout, widthToPx, heightToPx)
        var bitmap = encodeAsBitmap(
            barcodeLabelText,
            barCodeType,
            widthToPx / 2,
            heightToPx / 2
        )

        val originalBitmapWidth = bitmap?.width ?: 0
        bitmap = Bitmap.createScaledBitmap(bitmap!!, widthToPx, heightToPx, false)
        //bitmap = subColor(bitmap) ?: return null
        //bitmap.eraseColor(Color.WHITE)
        // рисуем содержимое и закрываем страницу
        paint.color = Color.BLACK
        canvas.drawColor(Color.WHITE)
        if (barCodeType == BarcodeFormat.QR_CODE) {
            barcodeX = ((canvas.width - bitmap.width) / 2).toFloat()//(canvas.width / 4).toFloat()
        } else {
            barcodeY = ((canvas.height - bitmap.height) / 2).toFloat()
        }
        canvas.drawBitmap(bitmap, barcodeX, barcodeY, paint)
        document.finishPage(page)
        val dir = File(requireActivity().applicationInfo.dataDir + "/PDF")
        if (!dir.exists()) {
            dir.mkdirs()
        }


        // сохраняем записанный контент
        var targetPdf: String? = dir.absolutePath + "/barcode.pdf"
        val filePath = File(targetPdf)
        try {
            document.writeTo(FileOutputStream(filePath))
            Toast.makeText(
                requireContext(),
                "PDf сохранён в " + filePath.absolutePath,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Что-то пошло не так: $e", Toast.LENGTH_LONG).show()
            targetPdf = null
        }

        // закрываем документ
        document.close()

        return if (widthToPx < originalBitmapWidth) null else filePath
    }

    fun subColor(src: Bitmap): Bitmap? {
        val output = Bitmap.createScaledBitmap(src, src.width, src.height, true)
        for (x in 0 until output.width) for (y in 0 until output.height) {
            val pixel = output.getPixel(x, y)

            val r: Int = pixel shr 16 and 0xff
            val g: Int = pixel shr 8 and 0xff
            val b: Int = pixel shr 0 and 0xff
            val Y = 0.2126 * r + 0.7152 * g + 0.0722 * b

            if (Y < 128) {
                output.setPixel(x, y, Color.BLACK)
            } else {
                output.setPixel(x, y, Color.WHITE)
            }

        }
        return output
    }

    private fun loadBitmapFromView(v: View, width: Int, height: Int): Bitmap? {
        //v.measure(width, height)
        //v.layout(0,0, v.measuredWidth, v.measuredHeight)
        //v.layout(0,0, width, height)
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    @Throws(WriterException::class)
    private fun encodeAsBitmap(
        contents: String?,
        format: BarcodeFormat,
        img_width: Int,
        img_height: Int
    ): Bitmap? {
        val contentsToEncode = contents ?: return null
        var hints: MutableMap<EncodeHintType?, Any?>? = null
        val encoding: String? = guessAppropriateEncoding(contentsToEncode)
        hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        if (encoding != null) {
            hints[EncodeHintType.CHARACTER_SET] = encoding
        }

        hints[EncodeHintType.MARGIN] = 2

        val writer = MultiFormatWriter()
        val result: BitMatrix = try {
            writer.encode(contentsToEncode, format, img_width * 2, img_height * 2, hints)
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }
        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result[x, y]) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun guessAppropriateEncoding(contents: CharSequence): String? {
        // Very crude at the moment
        for (element in contents) {
            if (element.code > 0xFF) {
                return "UTF-8"
            }
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        //val binding = FragmentBarcodePrintBinding.inflate(LayoutInflater.from(requireContext()))
        val activity = requireActivity()
        val cellTextView = activity.findViewById<AutoCompleteTextView>(R.id.cellTextView)
        val cellTextInputLayout = activity.findViewById<TextInputLayout>(R.id.cellTextInputLayout)
        setTextInputElementProperties(cellTextView, cellTextInputLayout)
        showBarcode()
    }

}