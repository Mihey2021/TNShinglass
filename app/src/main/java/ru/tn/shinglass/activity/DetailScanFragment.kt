package ru.tn.shinglass.activity

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import ru.tn.shinglass.R
import ru.tn.shinglass.databinding.FragmentDetailScanBinding
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.models.Option
import ru.tn.shinglass.viewmodel.DetailScanViewModel

class DetailScanFragment : Fragment() {

//    companion object {
//        fun newInstance() = DetailScanFragment()
//    }

    private lateinit var viewModel: DetailScanViewModel

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
            divisionTextView.text =
                "${getString(R.string.division_title)}Подразделение из настроек"
            warehouseTextView.text =
                "${getString(R.string.warehouse_title)}Склад из натроек"
            itemTextView.text = itemBarCode
            countEditText.setText("1.0")
            itemMeasureOfUnitTitleTextView.text = "шт."
            workwearDisposableCheckBox.isChecked = true
            purposeOfUseTextView.text =
                "${getString(R.string.purpose_of_use_title)}Выбранное назначение использования"
            purposeOfUseTextView.setOnClickListener {
                //TODO: Обработка выбора назначения использования
                Toast.makeText(requireContext(), "Клик по ссылке", Toast.LENGTH_SHORT).show()
            }

            phisicalPersonTextView.text =
                "${getString(R.string.physical_person_title)}Выбранное физическое лицо"
            phisicalPersonTextView.setOnClickListener {
                //TODO: Обработка выбора физ.лица
            }

            ownerTextView.text = "${getString(R.string.owner_title)} ${user1C.getUser1C()}"
        }


        return binding.root
    }




}