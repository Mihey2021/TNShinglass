package ru.tn.shinglass.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.auth.AppAuth
import ru.tn.shinglass.auth.AuthState
import ru.tn.shinglass.databinding.FragmentMainMenuBinding
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.Option

class MainMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        requireParentFragment().setFragmentResultListener("requestSelectedOption") { requestKey, bundle ->
            val param = bundle.getSerializable("selectedOption") as Option
            binding.operationTextView.text = param.docType?.title ?: ""
        }
        //binding.operationTextView.text = arguments?.optionObj?.title ?: ""//"[ UNDEFINED ]"
        //binding.operationTextView.text = optionsViewModel.getSelectedOption()?.title ?: "[ UNDEFINED ]"

        binding.userDescriptionText.setOnClickListener {
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.apply {
                inflate(R.menu.desktop_menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_exit -> {
                            //findNavController().navigate(R.id.authFragment)
                            //.navigate(R.id.action_tableScanFragment_to_authFragment)
                            AppAuth.getInstance().clearAuthData()
                            true
                        }
                        R.id.menu_settings -> {
//                            val intent = Intent(requireContext(), SettingsActivity::class.java)
//                            startActivity(intent)
                            findNavController().navigate(R.id.action_global_settingsFragment)
                            true
                        }
                        R.id.menu_barcode_parsing_tn -> {
                            findNavController().navigate(R.id.barcodeParsingTN)
                            true
                        }
                        R.id.menu_stocks -> {
                            findNavController().navigate(R.id.action_global_stocksFragment)
                            true
                        }
                        R.id.menu_print_barcode -> {
                            findNavController().navigate(R.id.action_global_barcodePrintFragment)
                            true
                        }
                        else -> false
                    }
                }
            }
            //popupMenu.menu[R.id.menu_print_barcode].isVisible = true
            popupMenu.menu.forEach { menuItem ->
                if (menuItem.itemId == R.id.menu_print_barcode) menuItem.isVisible = true
            }
            popupMenu.show()
        }

//        requireParentFragment().setFragmentResultListener("requestUserData") { requestKey, bundle ->
//            val user1C = bundle.getSerializable("userData") as User1C
//            if (user1C.getUserGUID().isBlank()) {
//                findNavController().navigate(R.id.authFragment)
//            } else {
//                binding.userDescriptionText.text = user1C.getUser1C()
//            }
//        }

        AppAuth.getInstance().authStateFlow.observe(viewLifecycleOwner) { authState ->
            val authData = authState.user1C.getUser1C()
            if (authData.isBlank()) {
                findNavController().navigate(R.id.authFragment)
            } else {
                binding.userDescriptionText.text =
                    authState.user1C.getUser1C() ?: "[Не авторизован]"
            }
        }

        return binding.root
    }

}