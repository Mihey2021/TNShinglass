package ru.tn.shinglass.activity

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.EditTextPreference.OnBindEditTextListener
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.databinding.InputIntDialogBinding
import ru.tn.shinglass.models.PrintLabelProperties


private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener

class PrinterSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.printer_preferences, rootKey)
        val rootPreference = preferenceManager.preferenceScreen
        setManualPrefsVisibility(rootPreference.sharedPreferences, "useManualSettings")

        val preferenceIP = rootPreference.findPreference<EditTextPreference>("printerIP")
        //if (preferenceIP != null) setFiltersIP(preferenceIP)
        preferenceIP?.setOnBindEditTextListener(object : OnBindEditTextListener {
            override fun onBindEditText(editText: EditText) {
                setFiltersIP(editText)
            }
        })


        val seekBarWidthPreference = rootPreference.findPreference<SeekBarPreference>("paperWidth")
        seekBarWidthPreference?.min = PrintLabelProperties.MIN_WIDTH.value
        seekBarWidthPreference?.max = PrintLabelProperties.MAX_WIDTH.value
        seekBarWidthPreference?.seekBarIncrement = 1
        seekBarWidthPreference?.setOnPreferenceClickListener {
            showInputDialogFromSeekBar(
                seekBarWidthPreference,
                PrintLabelProperties.MIN_WIDTH.value,
                PrintLabelProperties.MAX_WIDTH.value
            )
            true
        }

        val seekBarHeightPreference =
            rootPreference.findPreference<SeekBarPreference>("paperHeight")
        seekBarHeightPreference?.min = PrintLabelProperties.MIN_HEIGHT.value
        seekBarHeightPreference?.max = PrintLabelProperties.MAX_HEIGHT.value
        seekBarHeightPreference?.seekBarIncrement = 1
        seekBarHeightPreference?.setOnPreferenceClickListener {
            showInputDialogFromSeekBar(
                seekBarHeightPreference,
                PrintLabelProperties.MIN_HEIGHT.value,
                PrintLabelProperties.MAX_HEIGHT.value
            )
            true
        }

        listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                when (key) {
                    "useManualSettings" -> {
                        setManualPrefsVisibility(sharedPrefs, key)
                    }
                }
            }

        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)

    }

    private fun setFiltersIP(editText: EditText) {
//        val editTextIP = requireActivity().layoutInflater.inflate(preferenceIP.dialogLayoutResource, null)
//            .findViewById<EditText>(android.R.id.edit)

        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter { source, start, end, dest, dstart, dend ->
            if (end > start) {
                val destTxt = dest.toString()
                val resultingTxt = (destTxt.substring(0, dstart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dend))
                if (!resultingTxt
                        .matches(Regex("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?"))
                ) {
                    return@InputFilter ""
                } else {
                    val splits = resultingTxt.split(".").toTypedArray()
                    for (i in splits.indices) {
                        val currentValue = splits[i]
                        if (currentValue != "") {
                            if (Integer.valueOf(splits[i]) > 255) {
                                //if (splits[i].toInt() > 255) {
                                return@InputFilter ""
                            }
                        } else {
                            return@InputFilter "."
                        }
                    }
                }
            }
            null
        }

        editText.filters = filters
    }

    private fun showInputDialogFromSeekBar(
        seekBarPreference: SeekBarPreference,
        minValue: Int,
        maxValue: Int
    ) {
        val inputDialogBinding =
            InputIntDialogBinding.inflate(LayoutInflater.from(requireContext()))
        inputDialogBinding.dialogMessage.text = seekBarPreference.title
        inputDialogBinding.inputDialogValue.hint = "min: $minValue mm, max: $maxValue mm"
        inputDialogBinding.inputDialogValue.setText(seekBarPreference.value.toString())
        val inputDialog = DialogScreen.showDialog(
            requireContext(),
            DialogScreen.IDD_INPUT,
            isCancelable = false,
            customView = inputDialogBinding.root,
            positiveButtonTitle = getString(R.string.ok_text),
        )
        inputDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            ?.setOnClickListener {
                val currentValue =
                    inputDialogBinding.inputDialogValue.text?.toString()?.toInt() ?: minValue

                var isError = false

                if (currentValue < minValue) {
                    inputDialogBinding.inputDialogValue.setText(seekBarPreference.value.toString())
                    isError = true
                }

                if (currentValue > maxValue) {
                    inputDialogBinding.inputDialogValue.setText(seekBarPreference.value.toString())
                    isError = true
                }

                if (isError) {
                    DialogScreen.vibrate(requireContext())
                    return@setOnClickListener
                }

                seekBarPreference.value = currentValue
                inputDialog.dismiss()
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppAuth.getInstance().authStateFlow.observe(viewLifecycleOwner) { authState ->
            val authUser1C = authState.user1C
            if (authUser1C.getUserGUID().isEmpty()) findNavController().navigate(R.id.authFragment)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Фон экрана настроек
        val backgroundIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_tn)
        //backgroundIcon?.setTint(requireContext().getColor(R.color.red_400))
        backgroundIcon?.alpha = 20
        if (backgroundIcon != null) {
            requireView().background = backgroundIcon
        } else {
            requireView().setBackgroundResource(R.color.red_100)
        }
    }

    private fun setManualPrefsVisibility(sharedPrefs: SharedPreferences?, key: String) {
        val isVisible = sharedPrefs?.getBoolean(key, false) ?: false
        val prefManualSettings =
            preferenceManager.preferenceScreen.findPreference<PreferenceCategory>("manualPreferenceCategory")
        prefManualSettings?.isVisible = isVisible
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogScreen.getDialog(DialogScreen.IDD_INPUT)?.dismiss()
        ContextCompat.getDrawable(requireActivity(), R.drawable.ic_tn)?.alpha = 100
    }

}