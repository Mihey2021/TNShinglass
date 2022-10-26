package ru.tn.shinglass.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
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
            PopupMenu(it.context, it).apply {
                inflate(R.menu.desktop_menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_exit -> {
                            findNavController().navigate(R.id.authFragment)
                            //.navigate(R.id.action_tableScanFragment_to_authFragment)
                            true
                        }
                        R.id.menu_settings -> {
                            val intent = Intent(requireContext(), SettingsActivity::class.java)
                            startActivity(intent)
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }

        requireParentFragment().setFragmentResultListener("requestUserData") { requestKey, bundle ->
            val user1C = bundle.getSerializable("userData") as User1C
            if (user1C.getUserGUID().isBlank()) {
                findNavController().navigate(R.id.authFragment)
            } else {
                binding.userDescriptionText.text = user1C.getUser1C()
            }
        }

        return binding.root
    }

}