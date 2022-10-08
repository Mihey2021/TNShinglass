package ru.tn.shinglass.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.tn.shinglass.R
import ru.tn.shinglass.activity.utilites.scanner.BarcodeScannerReceiver
import ru.tn.shinglass.api.ApiUtils
import ru.tn.shinglass.data.api.ApiService
import ru.tn.shinglass.databinding.FragmentAuthBinding
import ru.tn.shinglass.dto.models.RequestLogin
import ru.tn.shinglass.dto.models.User1C
import ru.tn.shinglass.viewmodel.SettingsViewModel
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

const val DOMAIN_NAME = "@tn.ru"

class AuthFragment : Fragment() {

    private lateinit var binding: FragmentAuthBinding

    private val settingsViewModel: SettingsViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var apiService: ApiService? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        apiService = ApiUtils.getApiService(settingsViewModel.getBasicPreferences())

        binding = FragmentAuthBinding.inflate(inflater, container, false)

        binding.passwordInputEditText.doAfterTextChanged { text ->
            if (!text.isNullOrBlank()) binding.passwordTextInputLayout.error = null
        }

        binding.loginInputEditText.doAfterTextChanged { text ->
            if (!text.isNullOrBlank()) binding.loginTextInputLayout.error = null
        }

        clearForm()
        with(binding.deviceInfoTextView) {
            setTextColor(resources.getColor(R.color.light_blue_900, requireContext().theme))
            text = "${Build.MANUFACTURER} ${Build.MODEL}"
            setOnClickListener {
                val intent = Intent(requireContext(), SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        BarcodeScannerReceiver.dataScan.observe(viewLifecycleOwner) {

            clearForm()

            val dataScanPair = BarcodeScannerReceiver.dataScan.value
            val dataScanBarcode = dataScanPair?.first ?: ""
            val dataScanBarcodeType = dataScanPair?.second ?: ""

            if (dataScanBarcode == "") return@observe

            val authStruct = dataScanBarcode.split("|").toMutableList()
            if (authStruct.size == 0 || authStruct.size == 1) {
                showToast(
                    "${getString(R.string.err_barcode_format)}: [$dataScanBarcode]\n${
                        getString(
                            R.string.err_barcode_format_description
                        )
                    }", Toast.LENGTH_LONG
                )
                return@observe
            }

            binding.loginInputEditText.setText(authStruct[0])
            binding.passwordInputEditText.setText(authStruct[1])
        }

        binding.btnEnter.isEnabled = (apiService != null)

        binding.btnEnter.setOnClickListener {

            val clearLogin = binding.loginInputEditText.text.toString()
            val login = completeLogin(clearLogin)
            //if (login != DOMAIN_NAME) binding.loginTextInputLayout.setText(login)
            val pswdTxt = binding.passwordInputEditText.text.toString()
            val password = if (isBase64(pswdTxt)) pswdTxt else Base64.getEncoder()
                .encodeToString(pswdTxt.toByteArray())

//            if (login.isNullOrBlank() || password.isNullOrBlank()) {
//                showToast(getString(R.string.err_emty_login_or_password), Toast.LENGTH_SHORT)
//                return@setOnClickListener
//            }
            var thereAreErrors = false

            if (clearLogin.isBlank()) {
                thereAreErrors = true
                binding.loginTextInputLayout.error = getString(R.string.empty_login_error_text)
            }

            if (password.isNullOrBlank()) {
                thereAreErrors = true
                binding.passwordTextInputLayout.error = getString(R.string.empty_password_error_text)
            }

            if (thereAreErrors) return@setOnClickListener


            binding.btnEnter.isEnabled = false

            loginAttempt(login, password)
        }

        settingsViewModel.basicPrefs.observe(viewLifecycleOwner) {
            //TODO: Добавить re-build для retrofit при изменении настроек!

        }

        return binding.root
    }

    private fun completeLogin(login: String) =
        if ("@" in login) login else "$login$DOMAIN_NAME"

    private fun loginAttempt(login: String, password: String) {

        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.errorGroup.visibility = View.GONE

        if(apiService == null) return

        val user1C = apiService?.authorization(RequestLogin(login, password))

        user1C?.enqueue(object : Callback<User1C?> {
            override fun onResponse(call: Call<User1C?>, response: Response<User1C?>) {
                binding.loadingProgressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val user1C: User1C? = response.body()
                    if (user1C != null) {
                        //serviceViewModel.setAuthData(user1C)
                        BarcodeScannerReceiver.clearData()
                        val args = Bundle()
                        args.putSerializable("userData", user1C)
                        //findNavController().navigate(R.id.action_authFragment_to_desktopFragment, Bundle().apply { userData = user1C })
                        findNavController().navigate(
                            R.id.action_authFragment_to_desktopFragment,
                            args
                        )
                    }
                } else {
                    when (response.code()) {
                        401 -> showToast(
                            "${getString(R.string.err_an_error_has_occured)}:\n${
                                response.errorBody()!!.string()
                            }(${getString(R.string.text_code)}: ${response.code()})",
                            Toast.LENGTH_SHORT
                        )
                        else -> showToast(
                            "${getString(R.string.err_an_unknown_error_has_occured)}:\n${
                                response.errorBody()!!.string()
                            }(${getString(R.string.text_code)}: ${response.code()})",
                            Toast.LENGTH_SHORT
                        )
                    }
                    binding.btnEnter.isEnabled = true
                }
            }

            override fun onFailure(call: Call<User1C?>, t: Throwable) {
                binding.loadingProgressBar.visibility = View.GONE
                binding.errorGroup.visibility = View.VISIBLE
                binding.retryTitle.setTextColor(Color.BLACK)
                binding.errorTextView.setTextColor(Color.RED)
                binding.errorTextView.text = t.message
                binding.errorGroup.elevation = 0.1f
                binding.retryTitle.elevation = 1.0f
                binding.errorTextView.elevation = 1.0f
                binding.retryButton.setOnClickListener {
                    loginAttempt(login, password)
                }
                binding.btnEnter.isEnabled = true
            }
        })
    }

    private fun isBase64(s: String?): Boolean {
        val pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$"
        val r: Pattern = Pattern.compile(pattern)
        val m: Matcher = r.matcher(s)
        return m.find()
    }

    private fun showToast(message: String, duration: Int) {
        Toast.makeText(
            requireContext(),
            message,
            duration
        ).show()
    }

    private fun clearForm() {
        binding.loginInputEditText.setText("")
        binding.passwordInputEditText.setText("")
    }

    override fun onResume() {
        clearForm()
        super.onResume()
    }
}
