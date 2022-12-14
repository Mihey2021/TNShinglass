package ru.tn.shinglass.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleExpandableListAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.tn.shinglass.R
import ru.tn.shinglass.adapters.OnOptionsInteractionListener
import ru.tn.shinglass.adapters.OptionsMenuExpListAdapter
import ru.tn.shinglass.databinding.FragmentDesktopBinding
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.viewmodel.DesktopViewModel

private const val ATTR_OPTION_GROUP = "option"
private const val ATTR_SUB_OPTION_GROUP = "subOption"

class DesktopFragment : Fragment() {

    private lateinit var binding: FragmentDesktopBinding
    private val desktopViewModel: DesktopViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDesktopBinding.inflate(inflater, container, false)

        val user1C = arguments?.getSerializable("userData") as User1C

        setFragmentResult("requestUserData", bundleOf("userData" to user1C))

        desktopViewModel.optionsData.observe(viewLifecycleOwner) { options ->

            val groupData = ArrayList<Map<Option, ArrayList<Option>>>()

            options
                .filter { option -> option.subOptionId == 0L }
                .forEach { groupOption ->
                    val childDataItem = ArrayList<Option>()
                    options
                        .filter { subOption -> subOption.subOptionId == groupOption.id }
                        .forEach { subOption -> childDataItem.add(subOption) }
                        .also { groupData.add(hashMapOf(groupOption to childDataItem)) }
                }

            val adapter = OptionsMenuExpListAdapter(
                requireContext(),
                groupData,
                object : OnOptionsInteractionListener {
                    override fun selectOption(option: Option) {
                        val args = Bundle()
                        args.putSerializable("userData", user1C)
                        args.putSerializable("selectedOption", option)
                        findNavController().navigate(
                            R.id.action_desktopFragment_to_tableScanFragment,
                            args
                        )
                    }
                })

            binding.optionsMenuExpandableList.setAdapter(adapter)

        }

        return binding.root
    }
}