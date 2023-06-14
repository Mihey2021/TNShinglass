package ru.tn.shinglass.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.OnBackPressedListener
import ru.tn.shinglass.activity.utilites.dialogs.DialogScreen
import ru.tn.shinglass.activity.utilites.dialogs.OnDialogsInteractionListener
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.adapters.OnOptionsInteractionListener
import ru.tn.shinglass.adapters.OptionsMenuExpListAdapter
import ru.tn.shinglass.databinding.FragmentDesktopBinding
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.models.OptionType
import ru.tn.shinglass.viewmodel.DesktopViewModel

private const val ATTR_OPTION_GROUP = "option"
private const val ATTR_SUB_OPTION_GROUP = "subOption"

class DesktopFragment : Fragment(), OnBackPressedListener {

    private var dialog: AlertDialog? = null

    private lateinit var binding: FragmentDesktopBinding
    private val desktopViewModel: DesktopViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDesktopBinding.inflate(inflater, container, false)

        BarcodeScannerReceiver.setEnabled(false)

//        val user1C = arguments?.getSerializable("userData") as User1C
//        setFragmentResult("requestUserData", bundleOf("userData" to user1C))

        desktopViewModel.optionsData.observe(viewLifecycleOwner) { options ->

            val groupData = ArrayList<Map<Option, ArrayList<Option>>>()

            for (optionType in OptionType.values()) {
                options
                    .filter { option -> option.option == optionType && option.subOption == null }
                    .forEach { groupOption ->
                        val childDataItem = ArrayList<Option>()
                        options
                            .filter { subOption -> subOption.option == groupOption.option && subOption.subOption != null }
                            .forEach { subOption -> childDataItem.add(subOption) }
                            .also { groupData.add(hashMapOf(groupOption to childDataItem)) }
                    }
            }

//            options
//                .filter { option -> option.subOption == null }
//                .forEach { groupOption ->
//                    val childDataItem = ArrayList<Option>()
//                    options
//                        .filter { subOption -> subOption.option == groupOption.option }
//                        .forEach { subOption -> childDataItem.add(subOption) }
//                        .also { groupData.add(hashMapOf(groupOption to childDataItem)) }
//                }

            val adapter = OptionsMenuExpListAdapter(
                requireContext(),
                groupData,
                object : OnOptionsInteractionListener {
                    override fun selectOption(option: Option) {
                        val args = Bundle()
                        //args.putSerializable("userData", user1C)
                        args.putSerializable("selectedOption", option)
                        BarcodeScannerReceiver.clearData()
                        findNavController().navigate(
                            R.id.action_desktopFragment_to_tableScanFragment,
                            args
                        )
                    }
                })

            binding.optionsMenuExpandableList.setAdapter(adapter)

        }

//        AppAuth.getInstance().authStateFlow.observe(viewLifecycleOwner) {authState ->
//            if (authState.userGUID == "") findNavController().navigateUp()
//        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {
            if (it.first == "" && it.second == "") return@observe
            BarcodeScannerReceiver.clearData()
        }

        return binding.root
    }

    override fun onBackPressed() {
        //Toast.makeText(requireContext(), "Back Btn pressed", Toast.LENGTH_LONG).show()
        //dialog?.dismiss()
        DialogScreen.getDialog()?.dismiss()
        BarcodeScannerReceiver.setEnabled(false)
        //dialog =
            DialogScreen.showDialog(requireContext(), DialogScreen.IDD_QUESTION,
            message = resources.getString(R.string.question_exit_session_text),
            title = resources.getString(R.string.question_exit_session_title),
            onDialogsInteractionListener = object : OnDialogsInteractionListener {
                override fun onPositiveClickButton() {
                    //exitProcess(0)
                    //BarcodeScannerReceiver.setEnabled()
                    findNavController().navigateUp()
                }

                override fun onNegativeClickButton() {
                    //BarcodeScannerReceiver.setEnabled()
                }
            })
    }

//    override fun onStop() {
//        dialog?.dismiss()
//        super.onStop()
//    }

    override fun onDestroyView() {
        //dialog?.dismiss()
        DialogScreen.getDialog(DialogScreen.IDD_PROGRESS)?.dismiss()
        DialogScreen.getDialog()?.dismiss()
        BarcodeScannerReceiver.setEnabled()
        super.onDestroyView()
    }

}
